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

            // Determine expected type using AST bindings
            ITypeBinding expectedTypeBinding = determineExpectedTypeBinding();
            String expectedTypeStr = expectedTypeBinding != null ?
                    expectedTypeBinding.getQualifiedName() : "any";

            System.out.println("[Debug] Expected Type Binding: " + expectedTypeStr);
            if (expectedTypeBinding != null) {
                System.out.println("[Debug] Is Array: " + expectedTypeBinding.isArray());
                System.out.println("[Debug] Is Enum: " + expectedTypeBinding.isEnum());
                if (expectedTypeBinding.isArray()) {
                    System.out.println("[Debug] Array Dimensions: " + TypeManager.getArrayDimensions(expectedTypeBinding));
                    ITypeBinding leafType = TypeManager.getLeafTypeBinding(expectedTypeBinding);
                    System.out.println("[Debug] Leaf Type: " + (leafType != null ? leafType.getQualifiedName() : "null"));
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

                    List<CompletionItem> filteredItems = items.stream()
                            .filter(item -> item.getKind() == CompletionItemKind.Variable ||
                                    item.getKind() == CompletionItemKind.Field)
                            .filter(item -> TypeManager.isUserVariable(item.getLabel()))
                            .filter(item -> isTypeCompatibleWithCompletion(item, expectedTypeBinding))
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
     * Normalizes type names for comparison
     */
    private String normalizeTypeName(String typeName) {
        if (typeName == null) return "";

        // Remove java.lang prefix
        if (typeName.startsWith("java.lang.")) {
            typeName = typeName.substring(10);
        }

        // Convert wrapper to primitive for comparison
        typeName = TypeManager.toPrimitiveType(typeName);

        return typeName;
    }

    /**
     * Checks if two type names are compatible
     */
    private boolean areTypesCompatible(String type1, String type2) {
        if (type1.equals(type2)) return true;

        // Check primitive/wrapper compatibility
        String prim1 = TypeManager.toPrimitiveType(type1);
        String prim2 = TypeManager.toPrimitiveType(type2);

        return prim1.equals(prim2);
    }

    /**
     * NEW: Determines expected type using ITypeBinding from AST
     */
    private ITypeBinding determineExpectedTypeBinding() {
        if (this.astNode == null) return null;

        SimpleName simpleName = (SimpleName) this.astNode;
        ASTNode child = simpleName;
        ASTNode parent = simpleName.getParent();

        // Skip parentheses
        while (parent instanceof ParenthesizedExpression) {
            child = parent;
            parent = parent.getParent();
        }

        if (parent == null) return null;

        // Try to get type from context
        ITypeBinding contextType = getTypeBindingFromContext(child, parent);

        if (contextType != null) {
            return contextType;
        }

        return null;
    }


    /**
     * Gets type binding from the parent context
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
                    ITypeBinding binding = type.resolveBinding();
                    System.out.println("[Debug] VariableDeclarationStatement type: " +
                            (binding != null ? binding.getQualifiedName() : "null"));
                    return binding;
                } else if (grandParent instanceof FieldDeclaration) {
                    Type type = ((FieldDeclaration) grandParent).getType();
                    ITypeBinding binding = type.resolveBinding();
                    System.out.println("[Debug] FieldDeclaration type: " +
                            (binding != null ? binding.getQualifiedName() : "null"));
                    return binding;
                }
            }
        }

        // Assignment
        if (parent instanceof Assignment) {
            Assignment assignment = (Assignment) parent;
            if (assignment.getRightHandSide() == child) {
                Expression lhs = assignment.getLeftHandSide();
                ITypeBinding binding = lhs.resolveTypeBinding();
                System.out.println("[Debug] Assignment LHS type: " +
                        (binding != null ? binding.getQualifiedName() : "null"));
                return binding;
            }
        }

        // Array Initializer - find the array type
        if (parent instanceof ArrayInitializer) {
            ITypeBinding arrayType = findArrayTypeForInitializer((ArrayInitializer) parent, (Expression) child);
            System.out.println("[Debug] ArrayInitializer expected type: " +
                    (arrayType != null ? arrayType.getQualifiedName() : "null"));
            return arrayType;
        }

        // Method Invocation Argument
        if (parent instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) parent;
            int argIndex = mi.arguments().indexOf(child);
            if (argIndex >= 0) {
                IMethodBinding methodBinding = mi.resolveMethodBinding();
                if (methodBinding != null && argIndex < methodBinding.getParameterTypes().length) {
                    ITypeBinding paramType = methodBinding.getParameterTypes()[argIndex];
                    System.out.println("[Debug] Method parameter type: " +
                            (paramType != null ? paramType.getQualifiedName() : "null"));
                    return paramType;
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
                    ITypeBinding binding = returnType.resolveBinding();
                    System.out.println("[Debug] Return type: " +
                            (binding != null ? binding.getQualifiedName() : "null"));
                    return binding;
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
                    ITypeBinding arrayType = elementType.createArrayType(1);
                    System.out.println("[Debug] For loop array type: " +
                            (arrayType != null ? arrayType.getQualifiedName() : "null"));
                    return arrayType;
                }
            }
        }

        System.out.println("[Debug] No type binding found for this context");
        return null;
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

    /**
     * NEW: Type compatibility check using CompletionItem against expected type binding
     */
    private boolean isTypeCompatibleWithCompletion(CompletionItem item, ITypeBinding expectedType) {
        // If no expected type, show everything
        if (expectedType == null) {
            System.out.println("[Debug] No expected type - accepting: " + item.getLabel());
            return true;
        }

        // Extract type information from completion item
        String typeInfo = item.getDetail();
        if (typeInfo == null || typeInfo.isBlank()) {
            if (item.getLabel().contains(" : ")) {
                String[] parts = item.getLabel().split(" : ");
                if (parts.length > 1) typeInfo = parts[1].trim();
            }
        }

        if (typeInfo == null || typeInfo.isBlank()) {
            System.out.println("[Debug] No type info for: " + item.getLabel() + " - accepting by default");
            return true; // Can't filter without type info
        }

        System.out.println("[Debug] Checking compatibility: " + item.getLabel() +
                " Type: '" + typeInfo + "'" +
                " Expected: '" + expectedType.getQualifiedName() + "'" +
                " IsArray: " + expectedType.isArray() +
                " Dimensions: " + TypeManager.getArrayDimensions(expectedType));

        // Parse the type string and compare with expected binding
        boolean compatible = isTypeStringCompatibleWithBinding(typeInfo, expectedType);
        System.out.println("[Debug] Result: " + (compatible ? "ACCEPTED" : "REJECTED"));
        return compatible;
    }

    /**
     * Compares a type string (from LSP) with an ITypeBinding
     */
    private boolean isTypeStringCompatibleWithBinding(String typeStr, ITypeBinding expectedBinding) {
        if (typeStr == null || expectedBinding == null) return true;

        // Normalize the type string (remove whitespace)
        typeStr = typeStr.trim();

        // Count array dimensions in type string
        int typeStrDimensions = 0;
        String baseTypeStr = typeStr;
        while (baseTypeStr.endsWith("[]")) {
            typeStrDimensions++;
            baseTypeStr = baseTypeStr.substring(0, baseTypeStr.length() - 2).trim();
        }

        // Get expected dimensions
        int expectedDimensions = TypeManager.getArrayDimensions(expectedBinding);

        System.out.println("[Debug Dimensions] Type String: '" + typeStr + "' Dims: " + typeStrDimensions +
                " | Expected Dims: " + expectedDimensions);

        // Dimensions must match exactly for arrays
        if (typeStrDimensions != expectedDimensions) {
            System.out.println("[Debug] Dimension mismatch - REJECTED");
            return false;
        }

        // Get base types for comparison
        String expectedBaseType;
        if (expectedBinding.isArray()) {
            ITypeBinding leafBinding = TypeManager.getLeafTypeBinding(expectedBinding);
            expectedBaseType = leafBinding != null ? leafBinding.getQualifiedName() : "";
        } else {
            expectedBaseType = expectedBinding.getQualifiedName();
        }

        // Normalize type names for comparison
        String normalizedExpected = normalizeTypeName(expectedBaseType);
        String normalizedActual = normalizeTypeName(baseTypeStr);

        System.out.println("[Debug Base Types] Actual: '" + normalizedActual + "' vs Expected: '" + normalizedExpected + "'");

        // Check base type compatibility
        if (normalizedActual.equals(normalizedExpected)) {
            System.out.println("[Debug] Exact match - ACCEPTED");
            return true;
        }

        // Check if they're compatible types (e.g., int vs Integer)
        if (areTypesCompatible(normalizedActual, normalizedExpected)) {
            System.out.println("[Debug] Compatible types - ACCEPTED");
            return true;
        }

        // Check category compatibility (number, boolean, string)
        if (areCategoriesCompatible(normalizedActual, normalizedExpected)) {
            System.out.println("[Debug] Compatible categories - ACCEPTED");
            return true;
        }

        System.out.println("[Debug] No match - REJECTED");
        return false;
    }

    /**
     * Checks if two types belong to compatible categories
     */
    private boolean areCategoriesCompatible(String type1, String type2) {
        // Both are numbers
        if (isNumberType(type1) && isNumberType(type2)) return true;

        // Both are booleans
        if (isBooleanType(type1) && isBooleanType(type2)) return true;

        // Both are strings
        if (isStringType(type1) && isStringType(type2)) return true;

        return false;
    }

    private boolean isNumberType(String type) {
        return type.equals("int") || type.equals("double") || type.equals("float") ||
                type.equals("long") || type.equals("short") || type.equals("byte") ||
                type.equals("Integer") || type.equals("Double") || type.equals("Float") ||
                type.equals("Long") || type.equals("Short") || type.equals("Byte");
    }

    private boolean isBooleanType(String type) {
        return type.equals("boolean") || type.equals("Boolean");
    }

    private boolean isStringType(String type) {
        return type.equals("String") || type.equals("char") || type.equals("Character");
    }

    /**
     * Finds the expected type for an element inside an array initializer
     */
    private ITypeBinding findArrayTypeForInitializer(ArrayInitializer initializer, Expression element) {
        System.out.println("[Debug findArrayType] ===========================================");
        System.out.println("[Debug] Finding type for element: " + element.getClass().getSimpleName());
        System.out.println("[Debug] Inside initializer: " + initializer.getClass().getSimpleName());

        // Count how many ArrayInitializers we're nested inside
        ASTNode current = element.getParent(); // Start from element's parent (the initializer containing us)
        int depth = 0;
        ASTNode rootDefinition = null;

        while (current != null) {
            System.out.println("[Debug] Visiting: " + current.getClass().getSimpleName());

            // Count each ArrayInitializer we're inside
            if (current instanceof ArrayInitializer) {
                depth++;
                System.out.println("[Debug] Inside ArrayInitializer #" + depth);
            }
            // Also count list factory methods
            else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                String methodName = mi.getName().getIdentifier();
                if ("asList".equals(methodName) || "of".equals(methodName)) {
                    depth++;
                    System.out.println("[Debug] Inside " + methodName + " #" + depth);
                }
            }

            // Check for declaration
            ASTNode parent = current.getParent();

            if (parent instanceof VariableDeclarationFragment) {
                rootDefinition = parent;
                System.out.println("[Debug] Found VariableDeclarationFragment");
                break;
            }
            if (parent instanceof VariableDeclarationStatement) {
                rootDefinition = parent;
                System.out.println("[Debug] Found VariableDeclarationStatement");
                break;
            }
            if (parent instanceof FieldDeclaration) {
                rootDefinition = parent;
                System.out.println("[Debug] Found FieldDeclaration");
                break;
            }
            if (parent instanceof ArrayCreation) {
                rootDefinition = parent;
                System.out.println("[Debug] Found ArrayCreation");
                break;
            }

            current = parent;
        }

        if (rootDefinition == null) {
            System.out.println("[Debug] No root definition found");
            return null;
        }

        // Get the declared type binding
        ITypeBinding declaredTypeBinding = null;

        if (rootDefinition instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) rootDefinition;
            ASTNode grandParent = frag.getParent();

            if (grandParent instanceof VariableDeclarationStatement) {
                Type type = ((VariableDeclarationStatement) grandParent).getType();
                declaredTypeBinding = type.resolveBinding();
            } else if (grandParent instanceof FieldDeclaration) {
                Type type = ((FieldDeclaration) grandParent).getType();
                declaredTypeBinding = type.resolveBinding();
            }
        } else if (rootDefinition instanceof VariableDeclarationStatement) {
            Type type = ((VariableDeclarationStatement) rootDefinition).getType();
            declaredTypeBinding = type.resolveBinding();
        } else if (rootDefinition instanceof FieldDeclaration) {
            Type type = ((FieldDeclaration) rootDefinition).getType();
            declaredTypeBinding = type.resolveBinding();
        } else if (rootDefinition instanceof ArrayCreation) {
            ArrayType type = ((ArrayCreation) rootDefinition).getType();
            declaredTypeBinding = type.resolveBinding();
        }

        if (declaredTypeBinding == null) {
            System.out.println("[Debug] Could not resolve type binding");
            return null;
        }

        return calculateExpectedTypeAtDepth(declaredTypeBinding, depth);
    }

    /**
     * Calculates what type is expected at a given nesting depth
     */
    private ITypeBinding calculateExpectedTypeAtDepth(ITypeBinding rootType, int depth) {
        if (rootType == null) {
            System.out.println("[Debug calculateExpectedType] rootType is null");
            return null;
        }

        int totalDimensions = TypeManager.getArrayDimensions(rootType);
        ITypeBinding leafType = TypeManager.getLeafTypeBinding(rootType);

        System.out.println("[Debug calculateExpectedType SUMMARY]");
        System.out.println("  Root Type:        " + rootType.getQualifiedName());
        System.out.println("  Total Dimensions: " + totalDimensions);
        System.out.println("  Current Depth:    " + depth);
        System.out.println("  Leaf Type:        " + (leafType != null ? leafType.getQualifiedName() : "null"));

        // If we're at depth N inside totalDimensions D, we need (D - N) dimensions
        // Example: int[][][], depth 1 -> need int[][] (2 dimensions)
        // Example: int[][][], depth 2 -> need int[] (1 dimension)
        // Example: int[][][], depth 3 -> need int (0 dimensions)

        int neededDimensions = totalDimensions - depth;

        System.out.println("  Needed Dims:      " + neededDimensions + " (total - depth)");

        if (neededDimensions > 0) {
            if (leafType == null) {
                System.out.println("[Debug] ERROR: Leaf type is null");
                return null;
            }

            // Use the new incremental creation method
            ITypeBinding result = TypeManager.createArrayTypeWithDimensions(leafType, neededDimensions);
            System.out.println("  -> Returning:     " + (result != null ? result.getQualifiedName() : "null"));
            return result;
        } else if (neededDimensions == 0) {
            System.out.println("  -> Returning:     " + (leafType != null ? leafType.getQualifiedName() : "null") + " (leaf)");
            return leafType;
        } else {
            System.out.println("  -> ERROR: Negative dimensions needed!");
            return null;
        }
    }



}