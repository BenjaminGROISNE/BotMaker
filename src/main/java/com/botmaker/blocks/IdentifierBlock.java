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

    @Override
    protected Node createUINode(CompletionContext context) {
        // Create text node
        Text text = new Text(identifier);

        // Create container
        HBox container = new HBox(text);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.getStyleClass().add("identifier-block");

        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

        // Make it clickable
        container.setCursor(Cursor.HAND);
        container.setOnMouseClicked(e -> {
            requestSuggestions(container, context);
            e.consume();
        });

        return container;
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

                                // DEBUG: Print what we're checking
                                System.out.println("[Debug] Variable: " + item.getLabel() + " Type: " + typeInfo + " Expected: " + expectedType);

                                // Use STRICT type compatibility for better filtering
                                return isTypeCompatibleStrict(typeInfo, expectedType);
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
     * NEW: STRICT type compatibility checking that matches array dimensions exactly
     */
    private boolean isTypeCompatibleStrict(String variableType, String expectedType) {
        if (variableType == null || variableType.isBlank()) return true;
        if (expectedType == null || expectedType.equals(TypeManager.UI_TYPE_ANY)) return true;

        System.out.println("[Debug Strict Type Match] Checking: '" + variableType + "' vs Expected: '" + expectedType + "'");

        // For list context, we need EXACT dimension matching
        if (expectedType.equals(TypeManager.UI_TYPE_LIST)) {
            // Must be an array type
            if (!variableType.contains("[]")) {
                System.out.println("[Debug] Rejected: Not an array type");
                return false;
            }
            return true;
        }

        // For specific types (number, boolean, String), check leaf type
        if (expectedType.equals(TypeManager.UI_TYPE_NUMBER) ||
                expectedType.equals(TypeManager.UI_TYPE_BOOLEAN) ||
                expectedType.equals(TypeManager.UI_TYPE_STRING)) {

            // Get leaf types
            String varLeafType = TypeManager.getLeafType(variableType);
            String varUiType = TypeManager.determineUiType(varLeafType);

            // Variable must NOT be an array for leaf type contexts
            if (variableType.contains("[]")) {
                System.out.println("[Debug] Rejected: Array type when expecting leaf type");
                return false;
            }

            boolean compatible = TypeManager.isCompatible(varUiType, expectedType);
            System.out.println("[Debug] Leaf match: " + varLeafType + " -> " + varUiType + " = " + compatible);
            return compatible;
        }

        // For switch compatibility
        if (expectedType.equals(TypeManager.UI_TYPE_SWITCH_COMPATIBLE)) {
            if (variableType.contains("[]")) return false;
            return TypeManager.isCompatible(variableType, expectedType);
        }

        // Default compatibility check
        return TypeManager.isCompatible(variableType, expectedType);
    }

    /**
     * Walks up the AST skipping parentheses to find the true semantic parent.
     * Now properly handles nested list contexts by tracking depth.
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

        // NEW: Check if we're inside a list structure (ArrayInitializer or MethodInvocation for Arrays.asList/List.of)
        String listContextType = checkListContext(this.astNode);
        if (listContextType != null) {
            return listContextType;
        }

        // Switch Statement Expression Context
        if (parent instanceof SwitchStatement) {
            if (((SwitchStatement) parent).getExpression() == child) {
                return TypeManager.UI_TYPE_SWITCH_COMPATIBLE;
            }
        }

        // Boolean Contexts
        if (parent instanceof IfStatement) {
            if (((IfStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }
        if (parent instanceof WhileStatement) {
            if (((WhileStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }
        if (parent instanceof DoStatement) {
            if (((DoStatement) parent).getExpression() == child) return TypeManager.UI_TYPE_BOOLEAN;
        }

        // Unary Contexts
        if (parent instanceof PrefixExpression) {
            PrefixExpression.Operator op = ((PrefixExpression) parent).getOperator();
            if (op == PrefixExpression.Operator.NOT) return TypeManager.UI_TYPE_BOOLEAN;
            return TypeManager.UI_TYPE_NUMBER;
        }
        if (parent instanceof PostfixExpression) return TypeManager.UI_TYPE_NUMBER;

        // Binary Contexts
        if (parent instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) parent;
            InfixExpression.Operator op = infix.getOperator();
            if (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR) {
                return TypeManager.UI_TYPE_BOOLEAN;
            }
            return TypeManager.UI_TYPE_NUMBER;
        }

        // Assignment
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    String fullType = binding.getQualifiedName();
                    return TypeManager.determineUiType(fullType);
                }
            }
        }

        // Variable Declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getInitializer() == child) {
                ASTNode grandParent = frag.getParent();
                if (grandParent instanceof VariableDeclarationStatement) {
                    Type type = ((VariableDeclarationStatement) grandParent).getType();
                    String typeString = type.toString();
                    return TypeManager.determineUiType(typeString);
                } else if (grandParent instanceof FieldDeclaration) {
                    Type type = ((FieldDeclaration) grandParent).getType();
                    String typeString = type.toString();
                    return TypeManager.determineUiType(typeString);
                }
            }
        }

        return TypeManager.UI_TYPE_ANY;
    }

    /**
     * NEW: Checks if we're inside a list context and determines what type should go there.
     * This properly handles nested lists by calculating depth and comparing with declaration.
     */
    private String checkListContext(ASTNode node) {
        ASTNode current = node.getParent(); // Start from parent, not the node itself
        ASTNode rootDefinition = null;
        int currentDepth = 0;

        System.out.println("[Debug List Context Start] Node: " + node.getClass().getSimpleName() + " at " + node.getStartPosition());

        // Walk up to find the root definition and count depth
        while (current != null) {
            System.out.println("[Debug] Visiting: " + current.getClass().getSimpleName());

            // Count nested ArrayInitializers
            if (current instanceof ArrayInitializer) {
                currentDepth++;
                System.out.println("[Debug] Found ArrayInitializer, depth now: " + currentDepth);
            }
            // Count nested MethodInvocation (Arrays.asList/List.of)
            else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                String methodName = mi.getName().getIdentifier();
                if ("asList".equals(methodName) || "of".equals(methodName)) {
                    currentDepth++;
                    System.out.println("[Debug] Found " + methodName + ", depth now: " + currentDepth);
                }
            }

            // Found root declaration - stop here
            if (current instanceof VariableDeclarationFragment) {
                rootDefinition = current;
                System.out.println("[Debug] Found VariableDeclarationFragment");
                break;
            }
            if (current instanceof VariableDeclarationStatement) {
                rootDefinition = current;
                System.out.println("[Debug] Found VariableDeclarationStatement");
                break;
            }
            if (current instanceof FieldDeclaration) {
                rootDefinition = current;
                System.out.println("[Debug] Found FieldDeclaration");
                break;
            }

            current = current.getParent();
        }

        // If we didn't find a list-related context, return null (not in a list)
        if (currentDepth == 0) {
            System.out.println("[Debug] No list context found (depth=0)");
            return null;
        }

        if (rootDefinition == null) {
            System.out.println("[Debug] No root definition found");
            return null;
        }

        // Get the declared type
        String declaredType = null;
        if (rootDefinition instanceof VariableDeclarationStatement) {
            declaredType = ((VariableDeclarationStatement) rootDefinition).getType().toString();
        } else if (rootDefinition instanceof VariableDeclarationFragment) {
            ASTNode parent = rootDefinition.getParent();
            if (parent instanceof VariableDeclarationStatement) {
                declaredType = ((VariableDeclarationStatement) parent).getType().toString();
            } else if (parent instanceof FieldDeclaration) {
                declaredType = ((FieldDeclaration) parent).getType().toString();
            }
        } else if (rootDefinition instanceof FieldDeclaration) {
            declaredType = ((FieldDeclaration) rootDefinition).getType().toString();
        }

        if (declaredType == null) {
            System.out.println("[Debug] Could not determine declared type");
            return null;
        }

        // Calculate total dimensions and leaf type
        int totalDimensions = TypeManager.getListNestingLevel(declaredType);
        String leafType = TypeManager.getLeafType(declaredType);

        System.out.println("[Debug List Context] DeclaredType: " + declaredType +
                " | TotalDims: " + totalDimensions +
                " | CurrentDepth: " + currentDepth +
                " | LeafType: " + leafType);

        // If this isn't actually an array type, we're not in a list context
        if (totalDimensions == 0) {
            System.out.println("[Debug] Type is not an array, no list context");
            return null;
        }

        // Determine what we need at this depth
        // If we have int[][][] and we're at depth 1, we need int[][]
        // If we're at depth 2, we need int[]
        // If we're at depth 3, we need int
        int remainingDimensions = totalDimensions - currentDepth;

        System.out.println("[Debug] Remaining dimensions: " + remainingDimensions);

        if (remainingDimensions > 1) {
            // Need a nested list (e.g., int[][] when we have int[][][])
            System.out.println("[Debug] Returning TYPE_LIST");
            return TypeManager.UI_TYPE_LIST;
        } else if (remainingDimensions == 1) {
            // Need an array of leaf type (e.g., int[] when we have int[][])
            System.out.println("[Debug] Returning TYPE_LIST (one level array)");
            return TypeManager.UI_TYPE_LIST;
        } else if (remainingDimensions == 0) {
            // Need a leaf value (e.g., int when we have int[])
            String uiType = TypeManager.determineUiType(leafType);
            System.out.println("[Debug] Returning leaf type: " + uiType);
            return uiType;
        } else {
            // We're too deep - shouldn't happen
            System.out.println("[Debug] Too deep! Remaining: " + remainingDimensions);
            return null;
        }
    }

    private String getSimpleTypeName(String detail) {
        if (detail == null) return "";

        // Use leaf type for display
        String leafType = TypeManager.getLeafType(detail);

        if (TypeManager.isCompatible(leafType, TypeManager.UI_TYPE_NUMBER)) return "number";
        if (TypeManager.isCompatible(leafType, TypeManager.UI_TYPE_BOOLEAN)) return "bool";
        if (TypeManager.isCompatible(leafType, TypeManager.UI_TYPE_STRING)) return "text";

        return leafType;
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