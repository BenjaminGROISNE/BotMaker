package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents field access expressions like "this.score" or "obj.field"
 */
public class FieldAccessBlock extends AbstractExpressionBlock {

    private final String qualifier;  // "this", "super", or object name
    private final String fieldName;
    private boolean isUnedited = false;
    private static final String UNEDITED_STYLE_CLASS = "unedited-identifier";

    public FieldAccessBlock(String id, FieldAccess astNode) {
        this(id, astNode, false);
    }

    public FieldAccessBlock(String id, FieldAccess astNode, boolean markAsUnedited) {
        super(id, astNode);
        Expression expr = astNode.getExpression();
        this.qualifier = expr != null ? expr.toString() : "";
        this.fieldName = astNode.getName().getIdentifier();
        this.isUnedited = markAsUnedited;
    }

    // Constructor for QualifiedName (used for this.field)
    public FieldAccessBlock(String id, QualifiedName astNode, boolean markAsUnedited) {
        super(id, astNode);
        this.qualifier = astNode.getQualifier().toString();
        this.fieldName = astNode.getName().getIdentifier();
        this.isUnedited = markAsUnedited;
    }

    public String getQualifier() { return qualifier; }
    public String getFieldName() { return fieldName; }
    public boolean isUnedited() { return isUnedited; }

    public void markAsEdited() {
        this.isUnedited = false;
        if (uiNode != null) {
            uiNode.getStyleClass().remove(UNEDITED_STYLE_CLASS);
        }
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(3);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("field-access-block");

        // Qualifier text (e.g., "this")
        Text qualifierText = new Text(qualifier + ".");
        qualifierText.setStyle("-fx-fill: #8E44AD; -fx-font-weight: bold;");

        // Field name text
        Text fieldText = new Text(fieldName);
        fieldText.setStyle("-fx-fill: #2C3E50;");

        container.getChildren().addAll(qualifierText, fieldText);

        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

        container.setCursor(Cursor.HAND);
        String tooltipText = isUnedited ?
                "⚠️ Default field name - Click to choose" :
                "Click to change field";
        Tooltip.install(container, new Tooltip(tooltipText));

        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                requestSuggestions(container, context);
            }
        });

        return container;
    }

    private void requestSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(
                    new TextDocumentIdentifier(context.docUri()),
                    pos
            );

            String expectedType = determineExpectedType();

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ?
                        result.getLeft() :
                        result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    menu.setStyle("-fx-control-inner-background: white;");

                    List<CompletionItem> filteredItems = items.stream()
                            .filter(item -> item.getKind() == CompletionItemKind.Field ||
                                    item.getKind() == CompletionItemKind.Variable)
                            .filter(item -> {
                                String typeInfo = item.getDetail();
                                if (typeInfo == null || typeInfo.isBlank()) {
                                    if (item.getLabel().contains(" : ")) {
                                        String[] parts = item.getLabel().split(" : ");
                                        if (parts.length > 1) typeInfo = parts[1].trim();
                                    }
                                }
                                return com.botmaker.util.TypeManager.isCompatible(typeInfo, expectedType);
                            })
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        MenuItem noFields = new MenuItem("(No compatible fields found)");
                        noFields.setDisable(true);
                        noFields.setStyle("-fx-text-fill: #999;");
                        menu.getItems().add(noFields);
                    } else {
                        for (CompletionItem item : filteredItems) {
                            String label = item.getLabel();
                            String detail = item.getDetail();
                            if (detail == null && label.contains(" : ")) {
                                detail = label.split(" : ")[1].trim();
                            }

                            String display = label;
                            if (!label.contains(":") && detail != null && !detail.isEmpty()) {
                                display += " (" + getSimpleTypeName(detail) + ")";
                            }

                            MenuItem mi = new MenuItem(display);
                            mi.setStyle("-fx-text-fill: black;");
                            mi.setOnAction(event -> {
                                applySuggestion(item, context);
                                markAsEdited();
                            });
                            menu.getItems().add(mi);
                        }
                    }
                    menu.show(uiNode, javafx.geometry.Side.BOTTOM, 0, 0);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String determineExpectedType() {
        // Similar logic to IdentifierBlock
        if (this.astNode == null) return com.botmaker.util.TypeManager.UI_TYPE_ANY;

        ASTNode child = this.astNode;
        ASTNode parent = this.astNode.getParent();

        while (parent instanceof ParenthesizedExpression) {
            child = parent;
            parent = parent.getParent();
        }

        if (parent == null) return com.botmaker.util.TypeManager.UI_TYPE_ANY;

        // Assignment context
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    return com.botmaker.util.TypeManager.determineUiType(binding.getName());
                }
            }
        }

        // Variable declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getInitializer() == child) {
                ASTNode grandParent = frag.getParent();
                if (grandParent instanceof VariableDeclarationStatement) {
                    Type type = ((VariableDeclarationStatement) grandParent).getType();
                    return com.botmaker.util.TypeManager.determineUiType(type.toString());
                } else if (grandParent instanceof FieldDeclaration) {
                    Type type = ((FieldDeclaration) grandParent).getType();
                    return com.botmaker.util.TypeManager.determineUiType(type.toString());
                }
            }
        }

        return com.botmaker.util.TypeManager.UI_TYPE_ANY;
    }

    private String getSimpleTypeName(String detail) {
        if (detail == null) return "";
        if (com.botmaker.util.TypeManager.isCompatible(detail, com.botmaker.util.TypeManager.UI_TYPE_NUMBER)) return "number";
        if (com.botmaker.util.TypeManager.isCompatible(detail, com.botmaker.util.TypeManager.UI_TYPE_BOOLEAN)) return "bool";
        return detail;
    }

    private void applySuggestion(CompletionItem item, CompletionContext context) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            if (insertText.contains(" : ")) {
                insertText = insertText.split(" : ")[0];
            }

            // Replace just the field name, keeping the qualifier
            if (astNode instanceof FieldAccess) {
                FieldAccess fa = (FieldAccess) astNode;
                context.codeEditor().replaceSimpleName(fa.getName(), insertText);
            } else if (astNode instanceof QualifiedName) {
                QualifiedName qn = (QualifiedName) astNode;
                context.codeEditor().replaceSimpleName(qn.getName(), insertText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Position getPositionFromOffset(String code, int offset) {
        int line = 0;
        int lastNewline = -1;
        for (int i = 0; i < offset; i++) {
            if (code.charAt(i) == '\n') {
                line++;
                lastNewline = i;
            }
        }
        int character = offset - lastNewline - 1;
        return new Position(line, character);
    }
}