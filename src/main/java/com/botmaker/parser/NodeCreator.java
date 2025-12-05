// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\parser\NodeCreator.java
package com.botmaker.parser;

import com.botmaker.parser.factories.ExpressionFactory;
import com.botmaker.parser.factories.InitializerFactory;
import com.botmaker.parser.factories.StatementFactory;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeInfo;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

public class NodeCreator {

    private final InitializerFactory initializerFactory;
    private final ExpressionFactory expressionFactory;
    private final StatementFactory statementFactory;

    public NodeCreator() {
        this.initializerFactory = new InitializerFactory();
        this.expressionFactory = new ExpressionFactory(initializerFactory);
        this.statementFactory = new StatementFactory(initializerFactory);
    }

    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter, String contextTypeName) {
        return expressionFactory.createDefaultExpression(ast, type, cu, rewriter, contextTypeName);
    }

    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu,
                                              ASTRewrite rewriter) {
        return expressionFactory.createDefaultExpression(ast, type, cu, rewriter, null);
    }

    public Statement createDefaultStatement(AST ast, AddableBlock type, CompilationUnit cu,
                                            ASTRewrite rewriter) {
        return statementFactory.createDefaultStatement(ast, type, cu, rewriter);
    }

    public Expression createDefaultInitializer(AST ast, String typeName) {
        // Fix: Convert String to TypeInfo for safety, though overload exists
        return initializerFactory.createDefaultInitializer(ast, TypeInfo.from(typeName));
    }

    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu,
                                                     ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        // Fixed: Call delegated correctly to InitializerFactory
        return initializerFactory.createRecursiveListInitializer(ast, typeName, cu, rewriter, leavesToPreserve);
    }
}