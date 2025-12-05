// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\parser\factories\ExpressionFactory.java
package com.botmaker.parser.factories;

import com.botmaker.parser.helpers.EnumNodeHelper;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.DefaultNames;
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
                return createArrayInitializerExpression(ast);

            case ENUM_CONSTANT:
                return createEnumConstantExpression(ast, cu, contextTypeName);

            // Math
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                // Comparison
            case EQUALS:
            case NOT_EQUALS:
            case GREATER:
            case LESS:
            case GREATER_EQUALS:
            case LESS_EQUALS:
                // Logic
            case AND:
            case OR:
                return createInfixExpression(ast, type);

            default:
                return null;
        }
    }

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

    private Expression createArrayInitializerExpression(AST ast) {
        ArrayInitializer arrayInit = ast.newArrayInitializer();
        SimpleName placeholder = ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE);
        arrayInit.expressions().add(placeholder);
        return arrayInit;
    }

    private Expression createEnumConstantExpression(AST ast, CompilationUnit cu, String contextTypeName) {
        String enumTypeName = contextTypeName != null ? contextTypeName : "MyEnum";
        String firstConstant = EnumNodeHelper.findFirstEnumConstant(cu, enumTypeName);
        if (firstConstant == null) {
            firstConstant = "VALUE";
        }
        return ast.newQualifiedName(
                ast.newSimpleName(enumTypeName),
                ast.newSimpleName(firstConstant)
        );
    }

    private Expression createInfixExpression(AST ast, AddableExpression type) {
        InfixExpression infixExpr = ast.newInfixExpression();
        infixExpr.setLeftOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));

        // Set default right operand based on type (Math vs Logic)
        if (type == AddableExpression.AND || type == AddableExpression.OR) {
            infixExpr.setRightOperand(ast.newBooleanLiteral(true));
        } else {
            infixExpr.setRightOperand(ast.newNumberLiteral("0"));
        }

        switch (type) {
            // Math
            case ADD: infixExpr.setOperator(InfixExpression.Operator.PLUS); break;
            case SUBTRACT: infixExpr.setOperator(InfixExpression.Operator.MINUS); break;
            case MULTIPLY: infixExpr.setOperator(InfixExpression.Operator.TIMES); break;
            case DIVIDE: infixExpr.setOperator(InfixExpression.Operator.DIVIDE); break;
            case MODULO: infixExpr.setOperator(InfixExpression.Operator.REMAINDER); break;

            // Comparison
            case EQUALS: infixExpr.setOperator(InfixExpression.Operator.EQUALS); break;
            case NOT_EQUALS: infixExpr.setOperator(InfixExpression.Operator.NOT_EQUALS); break;
            case GREATER: infixExpr.setOperator(InfixExpression.Operator.GREATER); break;
            case LESS: infixExpr.setOperator(InfixExpression.Operator.LESS); break;
            case GREATER_EQUALS: infixExpr.setOperator(InfixExpression.Operator.GREATER_EQUALS); break;
            case LESS_EQUALS: infixExpr.setOperator(InfixExpression.Operator.LESS_EQUALS); break;

            // Logic
            case AND: infixExpr.setOperator(InfixExpression.Operator.CONDITIONAL_AND); break;
            case OR: infixExpr.setOperator(InfixExpression.Operator.CONDITIONAL_OR); break;
        }

        return infixExpr;
    }
}