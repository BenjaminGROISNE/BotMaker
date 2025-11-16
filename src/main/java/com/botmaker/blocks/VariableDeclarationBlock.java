package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.lsp4j.*;

import java.util.List;

public class VariableDeclarationBlock extends AbstractStatementBlock {

    private final String variableName;
    private final Type variableType;
    private ExpressionBlock initializer;

    public VariableDeclarationBlock(String id, VariableDeclarationStatement astNode) {
        super(id, astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);
        this.variableName = fragment.getName().getIdentifier();
        this.variableType = astNode.getType();
        this.initializer = null; // This will be set by the converter
    }

    public String getVariableName() {
        return variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public ExpressionBlock getInitializer() {
        return initializer;
    }

    public void setInitializer(ExpressionBlock initializer) {
        this.initializer = initializer;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("variable-declaration-block");

        Label typeLabel = new Label(variableType.toString());
        typeLabel.getStyleClass().add("type-label");
        typeLabel.setCursor(Cursor.HAND);
        typeLabel.setOnMouseClicked(e -> requestTypeSuggestions(typeLabel, context));
        container.getChildren().add(typeLabel);

        TextField nameField = new TextField(variableName);
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                String newName = nameField.getText();
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) this.astNode).fragments().get(0);
                String currentName = fragment.getName().getIdentifier();

                if (!newName.equals(currentName)) {
                    context.codeEditor().replaceSimpleName(fragment.getName(), newName);
                }
            }
        });
        container.getChildren().add(nameField);

        container.getChildren().add(new Label("="));

        if (initializer != null) {
            container.getChildren().add(initializer.getUINode(context));
        } else {
            container.getChildren().add(createExpressionDropZone(context));
        }

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    private void requestTypeSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.variableType.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();

                    // Manually add fundamental types from TypeManager
                    for (String typeName : TypeManager.getFundamentalTypeNames()) {
                        CompletionItem dummyItem = new CompletionItem(typeName);
                        MenuItem mi = new MenuItem(typeName);
                        mi.setOnAction(event -> applyTypeSuggestion(dummyItem, context));
                        menu.getItems().add(mi);
                    }
                    menu.getItems().add(new SeparatorMenuItem());

                    // Add types from language server if any
                    if (result != null && (result.isRight() || (result.isLeft() && !result.getLeft().isEmpty()))) {
                        List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();
                        for (CompletionItem item : items) {
                            CompletionItemKind kind = item.getKind();
                            // Filter for classes and interfaces suggested by the server
                            if (kind == CompletionItemKind.Class || kind == CompletionItemKind.Interface) {
                                // Avoid duplicating types we added manually
                                if (TypeManager.getFundamentalTypeNames().contains(item.getLabel())) continue;

                                MenuItem mi = new MenuItem(item.getLabel());
                                mi.setOnAction(event -> applyTypeSuggestion(item, context));
                                menu.getItems().add(mi);
                            }
                        }
                    }
                    menu.show(uiNode, javafx.geometry.Side.BOTTOM, 0, 0);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyTypeSuggestion(CompletionItem item, CompletionContext context) {
        try {
            String newTypeName = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newTypeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Position getPositionFromOffset(String code, int offset) {
        int line = 0;
        int lastNewline = -1;
        // Handle case where offset is at the beginning of the file
        if (offset == 0) {
            return new Position(0, 0);
        }
        for (int i = 0; i < offset; i++) {
            if (i >= code.length()) { // Boundary check
                return new Position(line, i - lastNewline - 1);
            }
            if (code.charAt(i) == '\n') {
                line++;
                lastNewline = i;
            }
        }
        int character = offset - lastNewline - 1;
        return new Position(line, character);
    }


    @Override
    public String getDetails() {
        String initializerText = initializer != null ? " = ..." : "";
        return "Variable Declaration: " + variableType.toString() + " " + variableName + initializerText;
    }
}