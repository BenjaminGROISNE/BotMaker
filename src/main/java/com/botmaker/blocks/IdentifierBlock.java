package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.util.TypeManager;
import javafx.application.Platform;
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

public class IdentifierBlock extends AbstractExpressionBlock {
    private final String identifier;
    private boolean isUnedited = false;
    private static final String UNEDITED_STYLE_CLASS = "unedited-identifier";

    public IdentifierBlock(String id, SimpleName astNode) {
        this(id, astNode, false);
    }

    public IdentifierBlock(String id, SimpleName astNode, boolean markAsUnedited) {
        super(id, astNode);
        this.identifier = astNode.getIdentifier();
        this.isUnedited = markAsUnedited;
    }

    public String getIdentifier() { return identifier; }
    public boolean isUnedited() { return isUnedited; }

    public void markAsEdited() {
        this.isUnedited = false;
        if (uiNode != null) {
            uiNode.getStyleClass().remove(UNEDITED_STYLE_CLASS);
        }
    }

    // IdentifierBlock.java
    @Override
    protected Node createUINode(CompletionContext context) {
        return BlockLayout.expression()
                .identifier()
                .withIdentifierText(identifier, isUnedited)
                .withClickHandler(() -> requestSuggestions(uiNode, context))
                .build();
    }

    private void requestSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            String expectedType = determineExpectedType();
            System.out.println("[Debug] Suggestion Context -> Expected Type: " + expectedType);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    menu.setStyle("-fx-control-inner-background: white;");

                    List<CompletionItem> filteredItems = items.stream()
                            .filter(item -> item.getKind() == CompletionItemKind.Variable || item.getKind() == CompletionItemKind.Field)
                            .filter(item -> TypeManager.isUserVariable(item.getLabel()))
                            .filter(item -> {
                                String typeInfo = item.getDetail();
                                if (typeInfo == null || typeInfo.isBlank()) {
                                    if (item.getLabel().contains(" : ")) {
                                        String[] parts = item.getLabel().split(" : ");
                                        if (parts.length > 1) typeInfo = parts[1].trim();
                                    }
                                }
                                // Checks validity using TypeManager (now handles SWITCH_COMPATIBLE)
                                return TypeManager.isCompatible(typeInfo, expectedType);
                            })
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        MenuItem noVars = new MenuItem("(No compatible variables found)");
                        noVars.setDisable(true);
                        noVars.setStyle("-fx-text-fill: #999;");
                        menu.getItems().add(noVars);
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

    /**
     * Walks up the AST skipping parentheses to find the true semantic parent.
     */
    private String determineExpectedType() {
        if (this.astNode == null) return TypeManager.UI_TYPE_ANY;

        ASTNode child = this.astNode;
        ASTNode parent = this.astNode.getParent();

        while (parent instanceof ParenthesizedExpression) {
            child = parent;
            parent = parent.getParent();
        }

        if (parent == null) return TypeManager.UI_TYPE_ANY;

        // NEW: Switch Statement Expression Context
        if (parent instanceof SwitchStatement) {
            if (((SwitchStatement) parent).getExpression() == child) {
                return TypeManager.UI_TYPE_SWITCH_COMPATIBLE;
            }
        }

        // 2. Boolean Contexts
        if (parent instanceof IfStatement) {
            if (((IfStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }
        if (parent instanceof WhileStatement) {
            if (((WhileStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }
        if (parent instanceof DoStatement) {
            if (((DoStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }

        // 3. Unary Contexts
        if (parent instanceof PrefixExpression) {
            PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
            if (op == PrefixExpression.Operator.NOT) return TypeManager.UI_TYPE_BOOLEAN;
            return TypeManager.UI_TYPE_NUMBER;
        }
        if (parent instanceof PostfixExpression) return TypeManager.UI_TYPE_NUMBER;

        // 4. Binary Contexts
        if (parent instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) parent;
            InfixExpression.Operator op = infix.getOperator();
            if (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR) {
                return TypeManager.UI_TYPE_BOOLEAN;
            }
            return TypeManager.UI_TYPE_NUMBER;
        }

        // 5. Assignment
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    return TypeManager.determineUiType(binding.getName());
                }
            }
        }

        // 6. Variable Declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getInitializer() == child) {
                ASTNode grandParent = frag.getParent();
                if (grandParent instanceof VariableDeclarationStatement) {
                    Type type = ((VariableDeclarationStatement) grandParent).getType();
                    return TypeManager.determineUiType(type.toString());
                }
            }
        }

        return TypeManager.UI_TYPE_ANY;
    }

    private String getSimpleTypeName(String detail) {
        if (detail == null) return "";
        if (TypeManager.isCompatible(detail, TypeManager.UI_TYPE_NUMBER)) return "number";
        if (TypeManager.isCompatible(detail, TypeManager.UI_TYPE_BOOLEAN)) return "bool";
        return detail;
    }

    private void applySuggestion(CompletionItem item, CompletionContext context) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            if (insertText.contains(" : ")) {
                insertText = insertText.split(" : ")[0];
            }
            context.codeEditor().replaceSimpleName((SimpleName) this.astNode, insertText);
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