// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\parser\factories\InitializerFactory.java
package com.botmaker.parser.factories;

import com.botmaker.parser.helpers.EnumNodeHelper;
import com.botmaker.util.TypeInfo;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import java.util.List;

public class InitializerFactory {

    // Overload for backward compatibility and NodeCreator call
    public Expression createDefaultInitializer(AST ast, String typeName) {
        return createDefaultInitializer(ast, TypeInfo.from(typeName), null);
    }

    public Expression createDefaultInitializer(AST ast, TypeInfo type) {
        return createDefaultInitializer(ast, type, null);
    }

    /**
     * Creates a default initializer for a given type.
     * Uses CompilationUnit to find default Enum constants if available.
     */
    public Expression createDefaultInitializer(AST ast, TypeInfo type, CompilationUnit cu) {
        // Handle arrays
        if (type.isArray()) {
            return createArrayInitializer(ast, type, java.util.Collections.emptyList(), cu);
        }

        // Handle Enums (Fix for Issue 4: Auto-pick first constant)
        if (type.isEnum() && cu != null) {
            String enumName = type.getLeafType().getTypeName();
            String firstConstant = EnumNodeHelper.findFirstEnumConstant(cu, enumName);
            if (firstConstant != null) {
                return ast.newQualifiedName(
                        ast.newSimpleName(enumName),
                        ast.newSimpleName(firstConstant)
                );
            }
        }

        // Handle Primitives and common types
        if (type.isNumeric()) {
            String leaf = type.getLeafType().getTypeName();
            if (leaf.equalsIgnoreCase("double") || leaf.equalsIgnoreCase("float")) {
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

    // Overload to fix StatementFactory error (3 args)
    public Expression createArrayInitializer(AST ast, TypeInfo type, List<Expression> valuesToPreserve) {
        return createArrayInitializer(ast, type, valuesToPreserve, null);
    }

    public Expression createArrayInitializer(AST ast, String typeName, List<Expression> valuesToPreserve) {
        return createArrayInitializer(ast, TypeInfo.from(typeName), valuesToPreserve, null);
    }

    public Expression createArrayInitializer(AST ast, TypeInfo type, List<Expression> valuesToPreserve, CompilationUnit cu) {
        int dimensions = type.getArrayDimensions();
        TypeInfo leafType = type.getLeafType();

        if (dimensions == 0) {
            return createDefaultInitializer(ast, leafType, cu);
        }

        ArrayCreation arrayCreation = ast.newArrayCreation();
        Type elementType = TypeManager.createTypeNode(ast, type.getTypeName());
        arrayCreation.setType((ArrayType) elementType);

        ArrayInitializer initializer = createNestedArrayInitializer(ast, leafType, dimensions, valuesToPreserve, cu);
        arrayCreation.setInitializer(initializer);

        return arrayCreation;
    }

    private ArrayInitializer createNestedArrayInitializer(AST ast, TypeInfo leafType, int dimensions, List<Expression> valuesToPreserve, CompilationUnit cu) {
        ArrayInitializer initializer = ast.newArrayInitializer();

        if (valuesToPreserve != null && !valuesToPreserve.isEmpty()) {
            for (Expression value : valuesToPreserve) {
                initializer.expressions().add((Expression) ASTNode.copySubtree(ast, value));
            }
        } else if (dimensions == 1) {
            // Create ONE default element
            initializer.expressions().add(createDefaultInitializer(ast, leafType, cu));
        } else {
            // Recursively create sub-array
            ArrayInitializer subArray = createNestedArrayInitializer(ast, leafType, dimensions - 1, null, cu);
            initializer.expressions().add(subArray);
        }

        return initializer;
    }

    /**
     * Restore for NodeCreator compatibility, redirecting to new logic.
     */
    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     org.eclipse.jdt.core.dom.rewrite.ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        return createArrayInitializer(ast, TypeInfo.from(typeName), leavesToPreserve, cu);
    }
}