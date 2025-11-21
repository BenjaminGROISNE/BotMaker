package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.validation.TypeValidator;
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

    public String getIdentifier() {
        return identifier;
    }

    public boolean isUnedited() {
        return isUnedited;
    }

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

        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

        container.setCursor(Cursor.HAND);

        String tooltipText = isUnedited
                ? "⚠️ Default variable name - Click to choose a variable"
                : "Click to change variable";

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(container, tooltip);

        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                requestSuggestions(container, context);
            }
        });

        if (isUnedited) {
            Platform.runLater(() -> autoPopulateWithSuggestion(container, context));
        }

        return container;
    }

    private void autoPopulateWithSuggestion(Node uiNode, CompletionContext context) {
        // Logic for auto-populating if needed
    }

    private void requestSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            // 1. Determine expected type based on AST context
            String expectedType = determineExpectedType();

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    menu.setStyle("-fx-control-inner-background: white;");

                    // 2. Filter items
                    List<CompletionItem> filteredItems = items.stream()
                            // FIX: Use Variable and Field (Parameters are treated as Variables in JDTLS)
                            .filter(item -> item.getKind() == CompletionItemKind.Variable || item.getKind() == CompletionItemKind.Field)
                            // HIDE system vars like 'args'
                            .filter(item -> TypeValidator.isUserVariable(item.getLabel()))
                            // FILTER by Context Type (e.g. only show booleans if inside a While)
                            .filter(item -> TypeValidator.isTypeCompatible(item.getDetail(), expectedType))
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        MenuItem noVars = new MenuItem("(No " + (expectedType.equals("any") ? "" : expectedType + " ") + "variables found)");
                        noVars.setDisable(true);
                        noVars.setStyle("-fx-text-fill: #999;");
                        menu.getItems().add(noVars);
                    } else {
                        for (CompletionItem item : filteredItems) {
                            // Clean label for display
                            String label = item.getLabel();
                            String detail = item.getDetail(); // "int", "String", etc.

                            String display = label;
                            if (detail != null && !detail.isEmpty()) {
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
     * Analyzes the AST parent to guess what type is required here.
     */
    private String determineExpectedType() {
        if (this.astNode == null || this.astNode.getParent() == null) return "any";

        ASTNode parent = this.astNode.getParent();

        // 1. Boolean Contexts
        if (parent instanceof IfStatement) {
            if (((IfStatement) parent).getExpression() == this.astNode) return "boolean";
        }
        if (parent instanceof WhileStatement) {
            if (((WhileStatement) parent).getExpression() == this.astNode) return "boolean";
        }
        if (parent instanceof DoStatement) {
            if (((DoStatement) parent).getExpression() == this.astNode) return "boolean";
        }

        // 2. Math Contexts
        if (parent instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) parent;
            // If operator is math (+, -, *, /, %), expect numbers
            if (infix.getOperator() != InfixExpression.Operator.EQUALS &&
                    infix.getOperator() != InfixExpression.Operator.NOT_EQUALS &&
                    infix.getOperator() != InfixExpression.Operator.CONDITIONAL_AND &&
                    infix.getOperator() != InfixExpression.Operator.CONDITIONAL_OR) {
                return "number";
            }
        }

        // 3. Assignments (Check left side to filter right side)
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            // If we are the Right Hand Side, check the Left Hand Side type
            if (assignment.getRightHandSide() == this.astNode) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    if ("boolean".equals(binding.getName())) return "boolean";
                    if ("int".equals(binding.getName()) || "double".equals(binding.getName())) return "number";
                    if ("String".equals(binding.getName())) return "String";
                }
            }
        }

        return "any";
    }

    private String getSimpleTypeName(String detail) {
        if (detail == null) return "";
        if (detail.equals("int") || detail.equals("double")) return "number";
        if (detail.equals("boolean")) return "bool";
        return detail;
    }

    private void applySuggestion(CompletionItem item, CompletionContext context) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
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