package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
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

    @Override
    protected Node createUINode(CompletionContext context) {
        Text text = new Text(this.identifier);
        HBox container = new HBox(text);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.getStyleClass().add("identifier-block");

        if (isUnedited) container.getStyleClass().add(UNEDITED_STYLE_CLASS);

        container.setCursor(Cursor.HAND);
        String tooltipText = isUnedited ? "⚠️ Default variable name - Click to choose" : "Click to change variable";
        Tooltip.install(container, new Tooltip(tooltipText));

        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) requestSuggestions(container, context);
        });

        return container;
    }

    private void requestSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            // 1. Determine Expected Type with Parent Traversal
            String expectedType = determineExpectedType();
            System.out.println("[Debug] Suggestion Context -> Expected Type: " + expectedType);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    System.out.println("[Debug] No completion items returned.");
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    menu.setStyle("-fx-control-inner-background: white;");

                    List<CompletionItem> filteredItems = items.stream()
                            // Only Variables and Fields
                            .filter(item -> item.getKind() == CompletionItemKind.Variable || item.getKind() == CompletionItemKind.Field)
                            // Filter hidden (args, scanner)
                            .filter(item -> TypeManager.isUserVariable(item.getLabel()))
                            // Filter by Type using TypeManager
                            .filter(item -> {
                                // Extract type info: prefer detail, fallback to label parsing
                                String typeInfo = item.getDetail();
                                if (typeInfo == null || typeInfo.isBlank()) {
                                    // Try to parse "name : type" from label
                                    if (item.getLabel().contains(" : ")) {
                                        String[] parts = item.getLabel().split(" : ");
                                        if (parts.length > 1) {
                                            typeInfo = parts[1].trim();
                                        }
                                    }
                                }

                                boolean match = TypeManager.isCompatible(typeInfo, expectedType);
                                System.out.println(String.format("[Debug] Item: %-15s | Type: %-10s | Expected: %-10s | Match: %s",
                                        item.getLabel(), typeInfo, expectedType, match));
                                return match;
                            })
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        MenuItem noVars = new MenuItem("(No " + (expectedType.equals("any") ? "" : expectedType + " ") + "variables found)");
                        noVars.setDisable(true);
                        noVars.setStyle("-fx-text-fill: #999;");
                        menu.getItems().add(noVars);
                    } else {
                        for (CompletionItem item : filteredItems) {
                            String label = item.getLabel();
                            // If detail is missing, we still want to display useful info
                            String detail = item.getDetail();
                            if (detail == null && label.contains(" : ")) {
                                detail = label.split(" : ")[1].trim();
                            }

                            String display = label;
                            // Avoid duplicating info if label is "x : int" and detail is "int"
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

        // 1. Skip Parentheses ((x))
        while (parent instanceof ParenthesizedExpression) {
            child = parent;
            parent = parent.getParent();
        }

        if (parent == null) return TypeManager.UI_TYPE_ANY;

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

        // 3. Unary Contexts (Prefix/Postfix)
        if (parent instanceof PrefixExpression) {
            PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
            // Logical NOT (!) expects boolean
            if (op == PrefixExpression.Operator.NOT) {
                return TypeManager.UI_TYPE_BOOLEAN;
            }
            // Increment/Decrement/Plus/Minus (++x, -x) expect numbers
            if (op == PrefixExpression.Operator.INCREMENT || op == PrefixExpression.Operator.DECREMENT ||
                    op == PrefixExpression.Operator.PLUS || op == PrefixExpression.Operator.MINUS) {
                return TypeManager.UI_TYPE_NUMBER;
            }
        }
        if (parent instanceof PostfixExpression) {
            // x++, x-- expect numbers
            return TypeManager.UI_TYPE_NUMBER;
        }

        // 4. Binary Contexts (Infix Expressions)
        if (parent instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) parent;
            InfixExpression.Operator op = infix.getOperator();

            // Arithmetic operators (+, -, *, /, %) expect numbers
            if (op == InfixExpression.Operator.PLUS || op == InfixExpression.Operator.MINUS ||
                    op == InfixExpression.Operator.TIMES || op == InfixExpression.Operator.DIVIDE ||
                    op == InfixExpression.Operator.REMAINDER) {
                return TypeManager.UI_TYPE_NUMBER;
            }
            // Comparison operators (<, >, <=, >=) expect numbers
            if (op == InfixExpression.Operator.LESS || op == InfixExpression.Operator.GREATER ||
                    op == InfixExpression.Operator.LESS_EQUALS || op == InfixExpression.Operator.GREATER_EQUALS) {
                return TypeManager.UI_TYPE_NUMBER;
            }
            // Conditional operators (&&, ||) expect booleans
            if (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR) {
                return TypeManager.UI_TYPE_BOOLEAN;
            }
        }

        // 5. Assignment (RHS must match LHS)
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    if (TypeManager.isCompatible(binding, TypeManager.UI_TYPE_BOOLEAN)) return TypeManager.UI_TYPE_BOOLEAN;
                    if (TypeManager.isCompatible(binding, TypeManager.UI_TYPE_NUMBER)) return TypeManager.UI_TYPE_NUMBER;
                    if (TypeManager.isCompatible(binding, TypeManager.UI_TYPE_STRING)) return TypeManager.UI_TYPE_STRING;
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
            // Sometimes insertText is missing, fallback to label.
            // Also handle label formats like "myVar : int" -> we only want "myVar"
            String insertText = item.getInsertText();
            if (insertText == null) {
                insertText = item.getLabel();
                if (insertText.contains(" : ")) {
                    insertText = insertText.split(" : ")[0];
                }
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