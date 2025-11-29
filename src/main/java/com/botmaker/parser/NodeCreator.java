package com.botmaker.parser;

import com.botmaker.parser.factories.ExpressionFactory;
import com.botmaker.parser.factories.InitializerFactory;
import com.botmaker.parser.factories.StatementFactory;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

/**
 * Coordinates node creation by delegating to specialized factories.
 * Reduced from 300 lines to ~80 lines by extracting factories.
 */
public class NodeCreator {

    private final InitializerFactory initializerFactory;
    private final ExpressionFactory expressionFactory;
    private final StatementFactory statementFactory;

    public NodeCreator() {
        this.initializerFactory = new InitializerFactory();
        this.expressionFactory = new ExpressionFactory(initializerFactory);
        this.statementFactory = new StatementFactory(initializerFactory);
    }

    // ========================================================================
    // EXPRESSION CREATION - Delegated to ExpressionFactory
    // ========================================================================

    /**
     * Creates a default expression for a given type with optional context.
     */
    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter, String contextTypeName) {
        return expressionFactory.createDefaultExpression(ast, type, cu, rewriter, contextTypeName);
    }

    /**
     * Creates a default expression without context type.
     */
    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter) {
        return expressionFactory.createDefaultExpression(ast, type, cu, rewriter, null);
    }

    // ========================================================================
    // STATEMENT CREATION - Delegated to StatementFactory
    // ========================================================================

    /**
     * Creates a default statement for a given block type.
     */
    public Statement createDefaultStatement(AST ast, AddableBlock type, CompilationUnit cu,
                                            ASTRewrite rewriter) {
        return statementFactory.createDefaultStatement(ast, type, cu, rewriter);
    }

    // ========================================================================
    // INITIALIZER CREATION - Delegated to InitializerFactory
    // (These are public because other classes like AstRewriter use them)
    // ========================================================================

    /**
     * Creates a default initializer expression for a type.
     * Used by handlers and other parser components.
     */
    public Expression createDefaultInitializer(AST ast, String typeName) {
        return initializerFactory.createDefaultInitializer(ast, typeName);
    }

    /**
     * Creates a recursive ArrayList initializer (for nested lists).
     * Used by type replacement handlers.
     */
    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        return initializerFactory.createRecursiveListInitializer(ast, typeName, cu, rewriter, leavesToPreserve);
    }
}