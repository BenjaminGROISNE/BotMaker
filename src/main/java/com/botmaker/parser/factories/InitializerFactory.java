package com.botmaker.parser.factories;

import com.botmaker.parser.ImportManager;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

/**
 * Factory for creating initializer expressions for variables and fields.
 * UPDATED: Now uses standard arrays (int[], String[]) instead of ArrayList
 */
public class InitializerFactory {

    /**
     * Creates a default initializer for a given type.
     */
    public Expression createDefaultInitializer(AST ast, String typeName) {
        // Handle arrays
        if (typeName.endsWith("[]")) {
            return createArrayInitializer(ast, typeName, null);
        }

        switch (typeName) {
            case "int":
            case "long":
            case "short":
            case "byte":
                return ast.newNumberLiteral("0");
            case "double":
            case "float":
                return ast.newNumberLiteral("0.0");
            case "boolean":
                return ast.newBooleanLiteral(false);
            case "char":
                CharacterLiteral literal = ast.newCharacterLiteral();
                literal.setCharValue('a');
                return literal;
            case "String":
                StringLiteral str = ast.newStringLiteral();
                str.setLiteralValue("");
                return str;
            default:
                return ast.newNullLiteral();
        }
    }

    /**
     * Creates an array initializer (e.g., new int[] {0}, new String[][] {{"a"}})
     * Preserves values if provided.
     */
    public Expression createArrayInitializer(AST ast, String typeName, List<Expression> valuesToPreserve) {
        // Parse array type
        int dimensions = TypeManager.getListNestingLevel(typeName);
        String baseType = TypeManager.getLeafType(typeName);

        if (dimensions == 0) {
            // Not an array, return default for base type
            return createDefaultInitializer(ast, baseType);
        }

        // Create array creation
        ArrayCreation arrayCreation = ast.newArrayCreation();
        Type elementType = TypeManager.createTypeNode(ast, typeName);
        arrayCreation.setType((ArrayType) elementType);

        // Create initializer
        ArrayInitializer initializer = createNestedArrayInitializer(ast, baseType, dimensions, valuesToPreserve);
        arrayCreation.setInitializer(initializer);

        return arrayCreation;
    }

    /**
     * Recursively creates nested array initializers
     */
    private ArrayInitializer createNestedArrayInitializer(AST ast, String baseType, int dimensions, List<Expression> valuesToPreserve) {
        ArrayInitializer initializer = ast.newArrayInitializer();

        if (dimensions == 1) {
            // Leaf level - add actual values
            if (valuesToPreserve != null && !valuesToPreserve.isEmpty()) {
                for (Expression value : valuesToPreserve) {
                    initializer.expressions().add((Expression) ASTNode.copySubtree(ast, value));
                }
            } else {
                // Add one default value
                initializer.expressions().add(createDefaultInitializer(ast, baseType));
            }
        } else {
            // Nested level - create sub-array
            ArrayInitializer subArray = createNestedArrayInitializer(ast, baseType, dimensions - 1, valuesToPreserve);
            initializer.expressions().add(subArray);
        }

        return initializer;
    }

    /**
     * DEPRECATED: No longer used with standard arrays
     */
    @Deprecated
    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        // Convert ArrayList syntax to array syntax and delegate
        String arrayType = convertArrayListToArray(typeName);
        return createArrayInitializer(ast, arrayType, leavesToPreserve);
    }

    /**
     * Helper to convert ArrayList<T> to T[]
     */
    private String convertArrayListToArray(String arrayListType) {
        if (!arrayListType.startsWith("ArrayList<")) {
            return arrayListType;
        }

        String inner = arrayListType.substring(10, arrayListType.length() - 1);

        // Check if nested
        if (inner.startsWith("ArrayList<")) {
            return convertArrayListToArray(inner) + "[]";
        } else {
            return inner + "[]";
        }
    }

    /**
     * DEPRECATED: Extracts the element type from ArrayList<T>.
     */
    @Deprecated
    public String extractArrayListElementType(String arrayListType) {
        if (arrayListType.contains("<") && arrayListType.contains(">")) {
            int start = arrayListType.indexOf("<") + 1;
            int end = arrayListType.lastIndexOf(">");
            return arrayListType.substring(start, end);
        }
        return "Object";
    }
}