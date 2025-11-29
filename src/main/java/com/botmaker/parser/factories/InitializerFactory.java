package com.botmaker.parser.factories;

import com.botmaker.parser.ImportManager;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

import static com.botmaker.util.TypeManager.toWrapperType;

/**
 * Factory for creating initializer expressions for variables and fields.
 */
public class InitializerFactory {

    /**
     * Creates a default initializer for a given type.
     */
    public Expression createDefaultInitializer(AST ast, String typeName) {
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
     * Creates a recursive ArrayList initializer (e.g., ArrayList<ArrayList<Integer>>).
     * Preserves values if provided.
     */
    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
        ImportManager.addImport(cu, rewriter, "java.util.Arrays");

        ClassInstanceCreation creation = ast.newClassInstanceCreation();
        String innerTypeStr = extractArrayListElementType(typeName);
        String wrapperInnerType = toWrapperType(innerTypeStr);

        ParameterizedType paramType = ast.newParameterizedType(
                ast.newSimpleType(ast.newName("ArrayList"))
        );

        if (!innerTypeStr.equals("Object")) {
            paramType.typeArguments().add(TypeManager.createTypeNode(ast, wrapperInnerType));
        }
        creation.setType(paramType);

        MethodInvocation asList = ast.newMethodInvocation();
        asList.setExpression(ast.newSimpleName("Arrays"));
        asList.setName(ast.newSimpleName("asList"));

        if (innerTypeStr.startsWith("ArrayList<")) {
            // Nested list
            Expression innerList = createRecursiveListInitializer(ast, innerTypeStr, cu, rewriter, leavesToPreserve);
            asList.arguments().add(innerList);
        } else {
            // Leaf type
            if (leavesToPreserve != null && !leavesToPreserve.isEmpty()) {
                for (Expression leaf : leavesToPreserve) {
                    asList.arguments().add((Expression) ASTNode.copySubtree(ast, leaf));
                }
            } else {
                asList.arguments().add(createDefaultInitializer(ast, innerTypeStr));
            }
        }

        creation.arguments().add(asList);
        return creation;
    }

    /**
     * Extracts the element type from ArrayList<T>.
     * E.g., "ArrayList<Integer>" -> "Integer"
     */
    public String extractArrayListElementType(String arrayListType) {
        if (arrayListType.contains("<") && arrayListType.contains(">")) {
            int start = arrayListType.indexOf("<") + 1;
            int end = arrayListType.lastIndexOf(">");
            return arrayListType.substring(start, end);
        }
        return "Object";
    }
}