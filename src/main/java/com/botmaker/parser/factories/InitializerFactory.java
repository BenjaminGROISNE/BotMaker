package com.botmaker.parser.factories;

import com.botmaker.util.TypeInfo;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * Factory for creating initializer expressions for variables and fields.
 * UPDATED: Uses TypeInfo exclusively for type logic.
 */
public class InitializerFactory {

    /**
     * Creates a default initializer for a given type.
     */
    public Expression createDefaultInitializer(AST ast, TypeInfo type) {
        // Handle arrays
        if (type.isArray()) {
            return createArrayInitializer(ast, type, java.util.Collections.emptyList());
        }

        // Handle Primitives and common types
        if (type.isNumeric()) {
            // Distinguish float/double vs int/long
            String leaf = type.getLeafType().getTypeName();
            if (leaf.equals("double") || leaf.equals("float") || leaf.equals("Double") || leaf.equals("Float")) {
                return ast.newNumberLiteral("0.0");
            }
            return ast.newNumberLiteral("0");
        }

        if (type.isBoolean()) {
            return ast.newBooleanLiteral(false);
        }

        if (type.isString()) {
            if (type.getTypeName().equals("char") || type.getTypeName().equals("Character")) {
                CharacterLiteral literal = ast.newCharacterLiteral();
                literal.setCharValue('a');
                return literal;
            }
            StringLiteral str = ast.newStringLiteral();
            str.setLiteralValue("");
            return str;
        }

        return ast.newNullLiteral();
    }

    /**
     * Overload for backward compatibility / convenience (auto-converts string to TypeInfo)
     */
    public Expression createDefaultInitializer(AST ast, String typeName) {
        return createDefaultInitializer(ast, TypeInfo.from(typeName));
    }

    /**
     * Creates an array initializer (e.g., new int[] {0}, new String[][] {{"a"}})
     * Preserves values if provided.
     */
    public Expression createArrayInitializer(AST ast, TypeInfo type, List<Expression> valuesToPreserve) {
        int dimensions = type.getArrayDimensions();
        TypeInfo leafType = type.getLeafType();

        if (dimensions == 0) {
            // Not an array, return default for base type
            return createDefaultInitializer(ast, leafType);
        }

        // Create array creation
        ArrayCreation arrayCreation = ast.newArrayCreation();
        Type elementType = TypeManager.createTypeNode(ast, type.getTypeName());
        arrayCreation.setType((ArrayType) elementType);

        // Create initializer
        ArrayInitializer initializer = createNestedArrayInitializer(ast, leafType, dimensions, valuesToPreserve);
        arrayCreation.setInitializer(initializer);

        return arrayCreation;
    }

    // Helper for overload
    public Expression createArrayInitializer(AST ast, String typeName, List<Expression> valuesToPreserve) {
        return createArrayInitializer(ast, TypeInfo.from(typeName), valuesToPreserve);
    }

    /**
     * Recursively creates nested array initializers
     */
    private ArrayInitializer createNestedArrayInitializer(AST ast, TypeInfo leafType, int dimensions, List<Expression> valuesToPreserve) {
        ArrayInitializer initializer = ast.newArrayInitializer();

        // 1. If valuesToPreserve is explicitly empty (not null), create an empty array {}
        if (valuesToPreserve != null && valuesToPreserve.isEmpty()) {
            return initializer;
        }

        // 2. Leaf dimension
        if (dimensions == 1) {
            if (valuesToPreserve != null) {
                for (Expression value : valuesToPreserve) {
                    initializer.expressions().add((Expression) ASTNode.copySubtree(ast, value));
                }
            } else {
                // Null valuesToPreserve -> Create ONE default element { default }
                initializer.expressions().add(createDefaultInitializer(ast, leafType));
            }
        }
        // 3. Nested dimension
        else {
            // Recursively create sub-array
            ArrayInitializer subArray = createNestedArrayInitializer(ast, leafType, dimensions - 1, valuesToPreserve);
            initializer.expressions().add(subArray);
        }

        return initializer;
    }

    /**
     * DEPRECATED: No longer used with standard arrays
     */
    @Deprecated
    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     org.eclipse.jdt.core.dom.rewrite.ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        // Just delegate to array logic using TypeInfo
        return createArrayInitializer(ast, convertArrayListToArray(typeName), leavesToPreserve);
    }

    private String convertArrayListToArray(String arrayListType) {
        if (!arrayListType.startsWith("ArrayList<")) {
            return arrayListType;
        }
        String inner = arrayListType.substring(10, arrayListType.length() - 1);
        if (inner.startsWith("ArrayList<")) {
            return convertArrayListToArray(inner) + "[]";
        } else {
            return inner + "[]";
        }
    }
}