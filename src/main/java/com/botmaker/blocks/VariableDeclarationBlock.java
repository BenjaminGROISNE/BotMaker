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
        this.initializer = null;
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

    // ENHANCEMENT for VariableDeclarationBlock.java

// In the createUINode method, after displaying the type and name,
// check if initializer is an ArrayInitializer and display it specially:

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("variable-declaration-block");

        // Type label
        Label typeLabel = new Label(getDisplayTypeName(variableType));
        typeLabel.getStyleClass().add("type-label");
        typeLabel.setCursor(Cursor.HAND);
        typeLabel.setOnMouseClicked(e -> requestTypeSuggestions(typeLabel, context));
        container.getChildren().add(typeLabel);

        // Name field
        TextField nameField = new TextField(variableName);
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String newName = nameField.getText();
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) this.astNode).fragments().get(0);
                String currentName = fragment.getName().getIdentifier();

                if (!newName.equals(currentName)) {
                    context.codeEditor().replaceSimpleName(fragment.getName(), newName);
                }
            }
        });
        container.getChildren().add(nameField);

        Label equalsLabel = new Label("=");
        equalsLabel.getStyleClass().add("keyword-label");
        container.getChildren().add(equalsLabel);

        // Initializer display
        if (initializer != null) {
            // Check if it's a list/array initializer
            if (initializer.getAstNode() instanceof org.eclipse.jdt.core.dom.ArrayInitializer) {
                // Display list specially
                HBox listDisplay = createListDisplay(context);
                container.getChildren().add(listDisplay);
            } else {
                // Normal expression
                container.getChildren().add(initializer.getUINode(context));
            }
        } else {
            container.getChildren().add(createExpressionDropZone(context));
        }

        // + Button for changing initializer expression
        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");
        addButton.setOnAction(e -> showExpressionMenu(addButton, context));
        container.getChildren().add(addButton);

        // Delete button
        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    // Helper method to display list nicely
    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label openBracket = new Label("[");
        openBracket.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        listBox.getChildren().add(openBracket);

        // Show "empty" or element count
        if (initializer instanceof ListBlock) {
            ListBlock listBlock = (ListBlock) initializer;
            if (listBlock.getElements().isEmpty()) {
                Label emptyLabel = new Label("empty");
                emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999;");
                listBox.getChildren().add(emptyLabel);
            } else {
                Label countLabel = new Label(listBlock.getElements().size() + " items");
                countLabel.setStyle("-fx-font-style: italic;");
                listBox.getChildren().add(countLabel);
            }
        } else {
            // Fallback: just show the initializer
            listBox.getChildren().add(initializer.getUINode(context));
        }

        Label closeBracket = new Label("]");
        closeBracket.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        listBox.getChildren().add(closeBracket);

        return listBox;
    }

    // Helper to get friendly type name
    private String getDisplayTypeName(Type type) {
        String typeName = type.toString();

        // Handle arrays
        if (typeName.endsWith("[]")) {
            String baseType = typeName.substring(0, typeName.length() - 2);
            switch (baseType) {
                case "int": return "int list";
                case "double": return "double list";
                case "String": return "text list";
                case "boolean": return "boolean list";
                default: return typeName;
            }
        }

        return typeName;
    }

    private void showExpressionMenu(Button button, CompletionContext context) {
        ContextMenu menu = new ContextMenu();

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.values()) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                if (initializer != null) {
                    org.eclipse.jdt.core.dom.Expression toReplace = (org.eclipse.jdt.core.dom.Expression) initializer.getAstNode();
                    context.codeEditor().replaceExpression(toReplace, type);
                }
            });
            menu.getItems().add(menuItem);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void requestTypeSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.variableType.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();

                    for (String typeName : TypeManager.getFundamentalTypeNames()) {
                        CompletionItem dummyItem = new CompletionItem(typeName);
                        MenuItem mi = new MenuItem(typeName);
                        mi.setOnAction(event -> applyTypeSuggestion(dummyItem, context));
                        menu.getItems().add(mi);
                    }
                    menu.getItems().add(new SeparatorMenuItem());

                    if (result != null && (result.isRight() || (result.isLeft() && !result.getLeft().isEmpty()))) {
                        List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();
                        for (CompletionItem item : items) {
                            CompletionItemKind kind = item.getKind();
                            if (kind == CompletionItemKind.Class || kind == CompletionItemKind.Interface) {
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
        if (offset == 0) {
            return new Position(0, 0);
        }
        for (int i = 0; i < offset; i++) {
            if (i >= code.length()) {
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