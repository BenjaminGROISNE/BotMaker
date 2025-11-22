package com.botmaker.parser;

import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

import static com.botmaker.ui.AddableBlock.CALL_FUNCTION;
import static com.botmaker.util.TypeManager.toWrapperType;

/**
 * Responsible solely for creating new AST Nodes (Statements and Expressions).
 */
public class NodeCreator {

    public Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu, ASTRewrite rewriter) {
        switch (type) {
            case TEXT:
                StringLiteral newString = ast.newStringLiteral();
                newString.setLiteralValue("text");
                return newString;
            case FUNCTION_CALL:
                MethodInvocation call = ast.newMethodInvocation();
                // Changed from "MyLibrary" to "SelectFile" to indicate action needed
                call.setExpression(ast.newSimpleName("SelectFile"));
                call.setName(ast.newSimpleName("selectMethod"));
                return call;
            case NUMBER:
                return ast.newNumberLiteral("0");
            case TRUE:
                return ast.newBooleanLiteral(true);
            case FALSE:
                return ast.newBooleanLiteral(false);
            case VARIABLE:
                return ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE);
            case LIST:
                ImportManager.addImport(cu, rewriter, "java.util.Arrays");
                MethodInvocation asList = ast.newMethodInvocation();
                asList.setExpression(ast.newSimpleName("Arrays"));
                asList.setName(ast.newSimpleName("asList"));
                return asList;

            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                InfixExpression infixExpr = ast.newInfixExpression();
                infixExpr.setLeftOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                infixExpr.setRightOperand(ast.newNumberLiteral("0"));
                switch (type) {
                    case ADD: infixExpr.setOperator(InfixExpression.Operator.PLUS); break;
                    case SUBTRACT: infixExpr.setOperator(InfixExpression.Operator.MINUS); break;
                    case MULTIPLY: infixExpr.setOperator(InfixExpression.Operator.TIMES); break;
                    case DIVIDE: infixExpr.setOperator(InfixExpression.Operator.DIVIDE); break;
                    case MODULO: infixExpr.setOperator(InfixExpression.Operator.REMAINDER); break;
                }
                return infixExpr;
            default:
                return null;
        }
    }

    public Statement createDefaultStatement(AST ast, AddableBlock type, CompilationUnit cu, ASTRewrite rewriter) {
        switch (type) {
            case PRINT:
                MethodInvocation println = ast.newMethodInvocation();
                println.setExpression(ast.newQualifiedName(ast.newSimpleName("System"), ast.newSimpleName("out")));
                println.setName(ast.newSimpleName("println"));
                StringLiteral emptyString = ast.newStringLiteral();
                emptyString.setLiteralValue("");
                println.arguments().add(emptyString);
                return ast.newExpressionStatement(println);

            case DECLARE_INT:
                return createVariableDeclaration(ast, DefaultNames.DEFAULT_INT, "0", PrimitiveType.INT);
            case DECLARE_DOUBLE:
                return createVariableDeclaration(ast, DefaultNames.DEFAULT_DOUBLE, "0.0", PrimitiveType.DOUBLE);
            case DECLARE_BOOLEAN:
                return createVariableDeclaration(ast, DefaultNames.DEFAULT_BOOLEAN, false, PrimitiveType.BOOLEAN);
            case DECLARE_STRING:
                return createStringDeclaration(ast);
            case CALL_FUNCTION:
                MethodInvocation call = ast.newMethodInvocation();
                // Default to a placeholder call
                call.setExpression(ast.newSimpleName("MyLibrary"));
                call.setName(ast.newSimpleName("myFunction"));
                return ast.newExpressionStatement(call);
            case DECLARE_ARRAY:
                if (cu != null && rewriter != null) {
                    ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
                    ImportManager.addImport(cu, rewriter, "java.util.Arrays");
                }
                VariableDeclarationFragment frag = ast.newVariableDeclarationFragment();
                frag.setName(ast.newSimpleName("myList"));
                frag.setInitializer(createRecursiveListInitializer(ast, "ArrayList<Integer>", cu, rewriter, null));
                VariableDeclarationStatement listDecl = ast.newVariableDeclarationStatement(frag);
                listDecl.setType(TypeManager.createTypeNode(ast, "ArrayList<Integer>"));
                return listDecl;

            case IF:
                IfStatement ifStatement = ast.newIfStatement();
                ifStatement.setExpression(ast.newBooleanLiteral(true));
                ifStatement.setThenStatement(ast.newBlock());
                return ifStatement;

            case WHILE:
                WhileStatement whileStatement = ast.newWhileStatement();
                whileStatement.setExpression(ast.newBooleanLiteral(true));
                whileStatement.setBody(ast.newBlock());
                return whileStatement;

            case FOR:
                EnhancedForStatement enhancedFor = ast.newEnhancedForStatement();
                SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
                parameter.setType(TypeManager.createTypeNode(ast, "String"));
                parameter.setName(ast.newSimpleName("item"));
                enhancedFor.setParameter(parameter);
                enhancedFor.setExpression(ast.newSimpleName("array"));
                enhancedFor.setBody(ast.newBlock());
                return enhancedFor;

            case DO_WHILE:
                DoStatement doStatement = ast.newDoStatement();
                doStatement.setExpression(ast.newBooleanLiteral(true));
                doStatement.setBody(ast.newBlock());
                return doStatement;

            case BREAK: return ast.newBreakStatement();
            case CONTINUE: return ast.newContinueStatement();
            case RETURN: return ast.newReturnStatement();

            case COMMENT: return ast.newEmptyStatement();

            case ASSIGNMENT:
                Assignment assignment = ast.newAssignment();
                assignment.setLeftHandSide(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                assignment.setOperator(Assignment.Operator.ASSIGN);
                assignment.setRightHandSide(ast.newNumberLiteral("0"));
                return ast.newExpressionStatement(assignment);

            case READ_LINE: return createScannerCall(ast, "input", "nextLine", "String");
            case READ_INT: return createScannerCall(ast, "num", "nextInt", PrimitiveType.INT);
            case READ_DOUBLE: return createScannerCall(ast, "num", "nextDouble", PrimitiveType.DOUBLE);

            case SWITCH:
                SwitchStatement switchStmt = ast.newSwitchStatement();
                switchStmt.setExpression(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                SwitchCase defaultCase = ast.newSwitchCase();
                switchStmt.statements().add(defaultCase);
                switchStmt.statements().add(ast.newBreakStatement());
                return switchStmt;

            case CASE:
                SwitchCase switchCase = ast.newSwitchCase();
                try { switchCase.expressions().add(ast.newNumberLiteral("0")); } catch (Exception e) {}
                return switchCase;

            case WAIT:
                return createWaitStatement(ast);

            default:
                return null;
        }
    }

    // Helpers for creation logic
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

    private Statement createScannerCall(AST ast, String varName, String methodName, Object typeObj) {
        VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(varName));
        MethodInvocation scannerCall = ast.newMethodInvocation();
        scannerCall.setExpression(ast.newSimpleName("scanner"));
        scannerCall.setName(ast.newSimpleName(methodName));
        fragment.setInitializer(scannerCall);
        VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
        if(typeObj instanceof String) varDecl.setType(TypeManager.createTypeNode(ast, (String)typeObj));
        else varDecl.setType(ast.newPrimitiveType((PrimitiveType.Code)typeObj));
        return varDecl;
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

    public Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu, ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
        ImportManager.addImport(cu, rewriter, "java.util.Arrays");

        ClassInstanceCreation creation = ast.newClassInstanceCreation();
        String innerTypeStr = extractArrayListElementType(typeName);
        String wrapperInnerType = toWrapperType(innerTypeStr);
        ParameterizedType paramType = ast.newParameterizedType(ast.newSimpleType(ast.newName("ArrayList")));

        if (!innerTypeStr.equals("Object")) {
            paramType.typeArguments().add(TypeManager.createTypeNode(ast, wrapperInnerType));
        }
        creation.setType(paramType);

        MethodInvocation asList = ast.newMethodInvocation();
        asList.setExpression(ast.newSimpleName("Arrays"));
        asList.setName(ast.newSimpleName("asList"));

        if (innerTypeStr.startsWith("ArrayList<")) {
            Expression innerList = createRecursiveListInitializer(ast, innerTypeStr, cu, rewriter, leavesToPreserve);
            asList.arguments().add(innerList);
        } else {
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

    public Expression createDefaultInitializer(AST ast, String typeName) {
        switch (typeName) {
            case "int": case "long": case "short": case "byte": return ast.newNumberLiteral("0");
            case "double": case "float": return ast.newNumberLiteral("0.0");
            case "boolean": return ast.newBooleanLiteral(false);
            case "char":
                CharacterLiteral literal = ast.newCharacterLiteral();
                literal.setCharValue('a');
                return literal;
            case "String":
                StringLiteral str = ast.newStringLiteral();
                str.setLiteralValue("");
                return str;
            default: return ast.newNullLiteral();
        }
    }

    private String extractArrayListElementType(String arrayListType) {
        if (arrayListType.contains("<") && arrayListType.contains(">")) {
            int start = arrayListType.indexOf("<") + 1;
            int end = arrayListType.lastIndexOf(">");
            return arrayListType.substring(start, end);
        }
        return "Object";
    }
}