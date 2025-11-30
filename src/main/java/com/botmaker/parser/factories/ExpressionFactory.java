package com.botmaker.parser.factories;

import com.botmaker.parser.ImportManager;
import com.botmaker.parser.factories.InitializerFactory;
import com.botmaker.parser.helpers.EnumNodeHelper;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Factory for creating expression nodes.
 */
public class ExpressionFactory {

    private final InitializerFactory initializerFactory;

    public ExpressionFactory(InitializerFactory initializerFactory) {
        this.initializerFactory = initializerFactory;
    }

    /**
     * Creates a default expression for a given type.
     * @param contextTypeName Optional context type (e.g., for enum constants)
     */
    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter, String contextTypeName) {
        switch (type) {
            case TEXT:
                return createStringLiteral(ast, "text");

            case FUNCTION_CALL:
                return createMethodInvocation(ast, "selectMethod");

            case NUMBER:
                return ast.newNumberLiteral("0");

            case TRUE:
                return ast.newBooleanLiteral(true);

            case FALSE:
                return ast.newBooleanLiteral(false);

            case VARIABLE:
                return ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE);

            case LIST:
                return createListExpression(ast, cu, rewriter);

            case ENUM_CONSTANT:
                return createEnumConstantExpression(ast, cu, contextTypeName);

            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                return createInfixExpression(ast, type);

            default:
                return null;
        }
    }

    /**
     * Convenience overload without context type.
     */
    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter) {
        return createDefaultExpression(ast, type, cu, rewriter, null);
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private StringLiteral createStringLiteral(AST ast, String value) {
        StringLiteral literal = ast.newStringLiteral();
        literal.setLiteralValue(value);
        return literal;
    }

    private MethodInvocation createMethodInvocation(AST ast, String methodName) {
        MethodInvocation call = ast.newMethodInvocation();
        call.setName(ast.newSimpleName(methodName));
        return call;
    }

    private Expression createListExpression(AST ast, CompilationUnit cu, ASTRewrite rewriter) {
        ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
        ImportManager.addImport(cu, rewriter, "java.util.Arrays");

        // Creates: new ArrayList<>(Arrays.asList())
        ClassInstanceCreation creation = ast.newClassInstanceCreation();

        // Use generic ArrayList (diamond operator)
        ParameterizedType paramType = ast.newParameterizedType(
                ast.newSimpleType(ast.newName("ArrayList"))
        );
        creation.setType(paramType);

        MethodInvocation asList = ast.newMethodInvocation();
        asList.setExpression(ast.newSimpleName("Arrays"));
        asList.setName(ast.newSimpleName("asList"));

        creation.arguments().add(asList);
        return creation;
    }

    private Expression createEnumConstantExpression(AST ast, CompilationUnit cu, String contextTypeName) {
        String enumTypeName = contextTypeName != null ? contextTypeName : "MyEnum";

        String firstConstant = EnumNodeHelper.findFirstEnumConstant(cu, enumTypeName);
        if (firstConstant == null) {
            firstConstant = "VALUE";
        }

        QualifiedName qn = ast.newQualifiedName(
                ast.newSimpleName(enumTypeName),
                ast.newSimpleName(firstConstant)
        );
        return qn;
    }

    private Expression createInfixExpression(AST ast, AddableExpression type) {
        InfixExpression infixExpr = ast.newInfixExpression();
        infixExpr.setLeftOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
        infixExpr.setRightOperand(ast.newNumberLiteral("0"));

        switch (type) {
            case ADD:
                infixExpr.setOperator(InfixExpression.Operator.PLUS);
                break;
            case SUBTRACT:
                infixExpr.setOperator(InfixExpression.Operator.MINUS);
                break;
            case MULTIPLY:
                infixExpr.setOperator(InfixExpression.Operator.TIMES);
                break;
            case DIVIDE:
                infixExpr.setOperator(InfixExpression.Operator.DIVIDE);
                break;
            case MODULO:
                infixExpr.setOperator(InfixExpression.Operator.REMAINDER);
                break;
        }

        return infixExpr;
    }
}