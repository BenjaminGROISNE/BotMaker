package com.botmaker.parser.factories;

import com.botmaker.ui.AddableBlock;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeInfo;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Collections;

public class StatementFactory {

    private final InitializerFactory initializerFactory;

    public StatementFactory(InitializerFactory initializerFactory) {
        this.initializerFactory = initializerFactory;
    }

    public Statement createDefaultStatement(AST ast, AddableBlock type, CompilationUnit cu,
                                            ASTRewrite rewriter) {
        switch (type) {
            case PRINT: return createPrintStatement(ast);
            case DECLARE_INT: return createVariableDeclaration(ast, DefaultNames.DEFAULT_INT, "0", PrimitiveType.INT);
            case DECLARE_DOUBLE: return createVariableDeclaration(ast, DefaultNames.DEFAULT_DOUBLE, "0.0", PrimitiveType.DOUBLE);
            case DECLARE_BOOLEAN: return createVariableDeclaration(ast, DefaultNames.DEFAULT_BOOLEAN, false, PrimitiveType.BOOLEAN);
            case DECLARE_STRING: return createStringDeclaration(ast);
            case DECLARE_ARRAY: return createArrayDeclaration(ast, cu, rewriter);
            case IF: return createIfStatement(ast);
            case WHILE: return createWhileStatement(ast);
            case FOR: return createForStatement(ast);
            case DO_WHILE: return createDoWhileStatement(ast);
            case FUNCTION_CALL: return createFunctionCallStatement(ast);
            case BREAK: return ast.newBreakStatement();
            case CONTINUE: return ast.newContinueStatement();
            case RETURN: return ast.newReturnStatement();
            case COMMENT: return ast.newEmptyStatement();
            case DECLARE_ENUM: return createEnumDeclaration(ast);
            case ASSIGNMENT: return createAssignmentStatement(ast);
            case READ_LINE: return createScannerCall(ast, "input", "nextLine", "String");
            case READ_INT: return createScannerCall(ast, "num", "nextInt", PrimitiveType.INT);
            case READ_DOUBLE: return createScannerCall(ast, "num", "nextDouble", PrimitiveType.DOUBLE);
            case SWITCH: return createSwitchStatement(ast);
            case WAIT: return createWaitStatement(ast);
            default: return null;
        }
    }

    // ... (Keep existing methods: createPrintStatement, createVariableDeclaration, etc.) ...

    // [ABBREVIATED FOR BREVITY - SAME AS ORIGINAL EXCEPT createArrayDeclaration]

    private Statement createPrintStatement(AST ast) {
        MethodInvocation println = ast.newMethodInvocation();
        println.setExpression(ast.newQualifiedName(
                ast.newSimpleName("System"),
                ast.newSimpleName("out")
        ));
        println.setName(ast.newSimpleName("println"));
        StringLiteral emptyString = ast.newStringLiteral();
        emptyString.setLiteralValue("");
        println.arguments().add(emptyString);
        return ast.newExpressionStatement(println);
    }

    private Statement createVariableDeclaration(AST ast, String name, String val, PrimitiveType.Code type) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(name));
        fragment.setInitializer(ast.newNumberLiteral(val));
        VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
        varDecl.setType(ast.newPrimitiveType(type));
        return varDecl;
    }

    private Statement createVariableDeclaration(AST ast, String name, boolean val, PrimitiveType.Code type) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(name));
        fragment.setInitializer(ast.newBooleanLiteral(val));
        VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
        varDecl.setType(ast.newPrimitiveType(type));
        return varDecl;
    }

    private Statement createStringDeclaration(AST ast) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(DefaultNames.DEFAULT_STRING));
        fragment.setInitializer(ast.newStringLiteral());
        VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
        varDecl.setType(TypeManager.createTypeNode(ast, "String"));
        return varDecl;
    }

    /**
     * UPDATED: Uses TypeInfo.from("int[]")
     */
    private Statement createArrayDeclaration(AST ast, CompilationUnit cu, ASTRewrite rewriter) {
        VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
        frag.setName(ast.newSimpleName("myList"));

        // Use TypeInfo for robust array creation
        frag.setInitializer(
                initializerFactory.createArrayInitializer(ast, TypeInfo.from("int[]"), Collections.emptyList())
        );

        VariableDeclarationStatement listDecl = ast.newVariableDeclarationStatement(frag);
        listDecl.setType(TypeManager.createTypeNode(ast, "int[]"));
        return listDecl;
    }

    // ... (Keep remaining methods: createIfStatement, createWhileStatement, etc. as they were) ...
    private Statement createIfStatement(AST ast) {
        IfStatement ifStatement = ast.newIfStatement();
        ifStatement.setExpression(ast.newBooleanLiteral(true));
        ifStatement.setThenStatement(ast.newBlock());
        return ifStatement;
    }

    private Statement createWhileStatement(AST ast) {
        WhileStatement whileStatement = ast.newWhileStatement();
        whileStatement.setExpression(ast.newBooleanLiteral(true));
        whileStatement.setBody(ast.newBlock());
        return whileStatement;
    }

    private Statement createForStatement(AST ast) {
        EnhancedForStatement enhancedFor = ast.newEnhancedForStatement();
        SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
        parameter.setType(TypeManager.createTypeNode(ast, "String"));
        parameter.setName(ast.newSimpleName("item"));
        enhancedFor.setParameter(parameter);
        enhancedFor.setExpression(ast.newSimpleName("array"));
        enhancedFor.setBody(ast.newBlock());
        return enhancedFor;
    }

    private Statement createDoWhileStatement(AST ast) {
        DoStatement doStatement = ast.newDoStatement();
        doStatement.setExpression(ast.newBooleanLiteral(true));
        doStatement.setBody(ast.newBlock());
        return doStatement;
    }

    private Statement createFunctionCallStatement(AST ast) {
        MethodInvocation methodCall = ast.newMethodInvocation();
        methodCall.setName(ast.newSimpleName("selectMethod"));
        return ast.newExpressionStatement(methodCall);
    }

    private Statement createEnumDeclaration(AST ast) {
        TypeDeclarationStatement typeDeclStmt = ast.newTypeDeclarationStatement(ast.newEnumDeclaration());
        EnumDeclaration enumDecl = (EnumDeclaration) typeDeclStmt.getDeclaration();
        enumDecl.setName(ast.newSimpleName("MyEnum"));
        EnumConstantDeclaration const1 = ast.newEnumConstantDeclaration();
        const1.setName(ast.newSimpleName("OPTION_A"));
        enumDecl.enumConstants().add(const1);
        EnumConstantDeclaration const2 = ast.newEnumConstantDeclaration();
        const2.setName(ast.newSimpleName("OPTION_B"));
        enumDecl.enumConstants().add(const2);
        return typeDeclStmt;
    }

    private Statement createAssignmentStatement(AST ast) {
        Assignment assignment = ast.newAssignment();
        assignment.setLeftHandSide(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
        assignment.setOperator(Assignment.Operator.ASSIGN);
        assignment.setRightHandSide(ast.newNumberLiteral("0"));
        return ast.newExpressionStatement(assignment);
    }

    private Statement createScannerCall(AST ast, String varName, String methodName, Object typeObj) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(varName));
        MethodInvocation scannerCall = ast.newMethodInvocation();
        scannerCall.setExpression(ast.newSimpleName("scanner"));
        scannerCall.setName(ast.newSimpleName(methodName));
        fragment.setInitializer(scannerCall);
        VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
        if (typeObj instanceof String) varDecl.setType(TypeManager.createTypeNode(ast, (String) typeObj));
        else varDecl.setType(ast.newPrimitiveType((PrimitiveType.Code) typeObj));
        return varDecl;
    }

    private Statement createSwitchStatement(AST ast) {
        SwitchStatement switchStmt = ast.newSwitchStatement();
        switchStmt.setExpression(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
        SwitchCase defaultCase = ast.newSwitchCase();
        switchStmt.statements().add(defaultCase);
        switchStmt.statements().add(ast.newBreakStatement());
        return switchStmt;
    }

    private Statement createWaitStatement(AST ast) {
        TryStatement tryStmt = ast.newTryStatement();
        Block tryBody = ast.newBlock();
        MethodInvocation sleepCall = ast.newMethodInvocation();
        sleepCall.setExpression(ast.newSimpleName("Thread"));
        sleepCall.setName(ast.newSimpleName("sleep"));
        sleepCall.arguments().add(ast.newNumberLiteral("1000"));
        tryBody.statements().add(ast.newExpressionStatement(sleepCall));
        tryStmt.setBody(tryBody);
        CatchClause catchClause = ast.newCatchClause();
        SingleVariableDeclaration exceptionDecl = ast.newSingleVariableDeclaration();
        exceptionDecl.setType(ast.newSimpleType(ast.newSimpleName("InterruptedException")));
        exceptionDecl.setName(ast.newSimpleName("e"));
        catchClause.setException(exceptionDecl);
        Block catchBody = ast.newBlock();
        MethodInvocation printStackTrace = ast.newMethodInvocation();
        printStackTrace.setExpression(ast.newSimpleName("e"));
        printStackTrace.setName(ast.newSimpleName("printStackTrace"));
        catchBody.statements().add(ast.newExpressionStatement(printStackTrace));
        catchClause.setBody(catchBody);
        tryStmt.catchClauses().add(catchClause);
        return tryStmt;
    }
}