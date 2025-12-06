package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeInfo;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UPDATED: Uses TypeInfo for all type operations
 */
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
        Text text = new Text(identifier);
        HBox container = new HBox(text);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.getStyleClass().add("identifier-block");

        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

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

            // ✨ NEW: Determine expected type using TypeInfo
            TypeInfo expectedType = determineExpectedType();

            System.out.println("[Debug] Expected TypeInfo: " + expectedType);
            if (expectedType.hasBinding()) {
                System.out.println("[Debug] Binding available: " + expectedType.getQualifiedName());
                System.out.println("[Debug] Is Array: " + expectedType.isArray());
                System.out.println("[Debug] Is Enum: " + expectedType.isEnum());
                if (expectedType.isArray()) {
                    System.out.println("[Debug] Array Dimensions: " + expectedType.getArrayDimensions());
                    System.out.println("[Debug] Leaf Type: " + expectedType.getLeafType().getDisplayName());
                }
            }

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    menu.setStyle("-fx-control-inner-background: white;");

                    // ✨ NEW: Simplified filtering using TypeInfo
                    List<CompletionItem> filteredItems = items.stream()
                            .filter(item -> item.getKind() == CompletionItemKind.Variable ||
                                    item.getKind() == CompletionItemKind.Field)
                            .filter(item -> isUserVariable(item.getLabel()))
                            .filter(item -> isTypeCompatible(item, expectedType))
                            .collect(Collectors.toList());

                    if (filteredItems.isEmpty()) {
                        MenuItem noVars = new MenuItem("(No compatible variables found)");
                        noVars.setDisable(true);
                        noVars.setStyle("-fx-text-fill: #999;");
                        menu.getItems().add(noVars);
                    } else {
                        for (CompletionItem item : filteredItems) {
                            String display = formatCompletionItem(item);
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
     * ✨ NEW: Determines expected type using TypeInfo wrapper
     * Returns TypeInfo (which may have binding or just string)
     */
    private TypeInfo determineExpectedType() {
        if (this.astNode == null) return TypeInfo.UNKNOWN;

        SimpleName simpleName = (SimpleName) this.astNode;
        ASTNode child = simpleName;
        ASTNode parent = simpleName.getParent();

        // Skip parentheses
        while (parent instanceof ParenthesizedExpression) {
            child = parent;
            parent = parent.getParent();
        }

        if (parent == null) return TypeInfo.UNKNOWN;

        // ✨ Get ITypeBinding from context and wrap in TypeInfo
        ITypeBinding contextBinding = getTypeBindingFromContext(child, parent);
        return contextBinding != null ? TypeInfo.from(contextBinding) : TypeInfo.UNKNOWN;
    }

    /**
     * Gets type binding from the parent context
     * (Same logic as before, just returns ITypeBinding)
     */
    private ITypeBinding getTypeBindingFromContext(ASTNode child, ASTNode parent) {
        System.out.println("[Debug getTypeBindingFromContext] Child: " + child.getClass().getSimpleName() +
                " Parent: " + parent.getClass().getSimpleName());

        // Variable Declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getInitializer() == child) {
                ASTNode grandParent = frag.getParent();
                if (grandParent instanceof VariableDeclarationStatement) {
                    Type type = ((VariableDeclarationStatement) grandParent).getType();
                    return type.resolveBinding();
                } else if (grandParent instanceof FieldDeclaration) {
                    Type type = ((FieldDeclaration) grandParent).getType();
                    return type.resolveBinding();
                }
            }
        }

        // Assignment
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                return lhs.resolveTypeBinding();
            }
        }

        // Array Initializer
        if (parent instanceof ArrayInitializer) {
            return findArrayTypeForInitializer((ArrayInitializer) parent, (Expression) child);
        }

        // Method Invocation Argument
        if (parent instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) parent;

            // FIX: Relax type checking for System.out.print/println to allow any variable
            if (isSystemOutPrint(mi)) {
                return null; // Return null effectively means "Unknown/Any" type allowed
            }

            int argIndex = mi.arguments().indexOf(child);
            if (argIndex >= 0) {
                IMethodBinding methodBinding = mi.resolveMethodBinding();
                if (methodBinding != null && argIndex < methodBinding.getParameterTypes().length) {
                    return methodBinding.getParameterTypes()[argIndex];
                }
            }
        }

        // Return Statement
        if (parent instanceof ReturnStatement) {
            ASTNode current = parent;
            while (current != null && !(current instanceof MethodDeclaration)) {
                current = current.getParent();
            }
            if (current instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) current;
                Type returnType = method.getReturnType2();
                if (returnType != null) {
                    return returnType.resolveBinding();
                }
            }
        }

        // For Loop
        if (parent instanceof EnhancedForStatement) {
            EnhancedForStatement forStmt = (EnhancedForStatement) parent;
            if (forStmt.getExpression() == child) {
                SingleVariableDeclaration param = forStmt.getParameter();
                ITypeBinding elementType = param.getType().resolveBinding();
                if (elementType != null) {
                    return elementType.createArrayType(1);
                }
            }
        }

        return null;
    }

    /**
     * Checks if the method invocation is System.out.print or System.out.println
     */
    private boolean isSystemOutPrint(MethodInvocation mi) {
        String name = mi.getName().getIdentifier();
        if ("println".equals(name) || "print".equals(name)) {
            Expression expr = mi.getExpression();
            // Checking toString() handles "System.out" which might be QualifiedName or FieldAccess
            if (expr != null && "System.out".equals(expr.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ✨ NEW: Simplified type compatibility check using TypeInfo
     */
    private boolean isTypeCompatible(CompletionItem item, TypeInfo expectedType) {
        // If no expected type, show everything
        if (expectedType == null || expectedType.isUnknown()) {
            return true;
        }

        // Extract type from completion item
        String typeStr = extractTypeFromItem(item);

        // Use TypeInfo to wrap the suggestion type
        TypeInfo actualType = TypeInfo.from(typeStr);

        // --- FILTERING FIX ---
        // If strict filtering is needed: An unknown/Object type cannot satisfy a specific requirement (like String).
        // This filters out default variables like "variable (Object)" from "String s = ..."
        if (!expectedType.isUnknown() && !expectedType.getTypeName().equals("Object")) {
            // If expected is specific, but actual is generic Object or Unknown, reject it.
            if (actualType.isUnknown() || actualType.getTypeName().equals("Object")) {
                return false;
            }
        }
        // ---------------------

        boolean compatible = actualType.isCompatibleWith(expectedType);

        System.out.println("[Debug] Checking: " + item.getLabel() +
                " | Actual: " + actualType +
                " | Expected: " + expectedType +
                " | Result: " + (compatible ? "✓" : "✗"));

        return compatible;
    }

    /**
     * Helper to extract type string from CompletionItem
     */
    private String extractTypeFromItem(CompletionItem item) {
        String typeInfo = item.getDetail();
        if (typeInfo == null || typeInfo.isBlank()) {
            if (item.getLabel().contains(" : ")) {
                String[] parts = item.getLabel().split(" : ");
                if (parts.length > 1) typeInfo = parts[1].trim();
            }
        }
        return typeInfo;
    }

    /**
     * Format completion item for display
     */
    private String formatCompletionItem(CompletionItem item) {
        String label = item.getLabel();
        String detail = item.getDetail();

        if (detail == null && label.contains(" : ")) {
            detail = label.split(" : ")[1].trim();
        }

        if (!label.contains(":") && detail != null && !detail.isEmpty()) {
            TypeInfo type = TypeInfo.from(detail);
            return label + " (" + type.getDisplayName() + ")";
        }

        return label;
    }

    /**
     * Helper to check if variable should be shown (filters system variables)
     */
    private boolean isUserVariable(String label) {
        String cleanName = label.split(" ")[0].split(":")[0].trim();
        return !List.of("args", "this", "super", "scanner", "class").contains(cleanName) &&
                !cleanName.startsWith("_");
    }

    /**
     * Finds the expected type for an element inside an array initializer
     * Returns ITypeBinding which will be wrapped in TypeInfo by caller
     */
    private ITypeBinding findArrayTypeForInitializer(ArrayInitializer initializer, Expression element) {
        System.out.println("[Debug findArrayType] Finding type for nested array element");

        // Count nesting depth
        ASTNode current = element.getParent();
        int depth = 0;
        ASTNode rootDefinition = null;

        while (current != null) {
            if (current instanceof ArrayInitializer) {
                depth++;
            } else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                String methodName = mi.getName().getIdentifier();
                if ("asList".equals(methodName) || "of".equals(methodName)) {
                    depth++;
                }
            }

            ASTNode parent = current.getParent();
            if (parent instanceof VariableDeclarationFragment ||
                    parent instanceof VariableDeclarationStatement ||
                    parent instanceof FieldDeclaration ||
                    parent instanceof ArrayCreation) {
                rootDefinition = parent;
                break;
            }
            current = parent;
        }

        if (rootDefinition == null) return null;

        // Get declared type
        ITypeBinding declaredTypeBinding = null;
        if (rootDefinition instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) rootDefinition;
            ASTNode grandParent = frag.getParent();
            if (grandParent instanceof VariableDeclarationStatement) {
                declaredTypeBinding = ((VariableDeclarationStatement) grandParent).getType().resolveBinding();
            } else if (grandParent instanceof FieldDeclaration) {
                declaredTypeBinding = ((FieldDeclaration) grandParent).getType().resolveBinding();
            }
        } else if (rootDefinition instanceof ArrayCreation) {
            declaredTypeBinding = ((ArrayCreation) rootDefinition).getType().resolveBinding();
        }

        if (declaredTypeBinding == null) return null;

        // ✨ Use TypeInfo to calculate expected type at depth
        TypeInfo rootType = TypeInfo.from(declaredTypeBinding);
        int neededDimensions = rootType.getArrayDimensions() - depth;

        System.out.println("[Debug] Root: " + rootType +
                " | Depth: " + depth +
                " | Needed Dims: " + neededDimensions);

        if (neededDimensions > 0) {
            TypeInfo expectedType = rootType.getLeafType().asArray(neededDimensions);
            return expectedType.getBinding();
        } else if (neededDimensions == 0) {
            return rootType.getLeafType().getBinding();
        }

        return null;
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