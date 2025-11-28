package com.botmaker.parser;

import com.botmaker.state.*;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

public class CodeEditor {

    private final ApplicationState state;
    private final EventBus eventBus;
    private final AstRewriter astRewriter;
    private final BlockFactory blockFactory;

    public CodeEditor(ApplicationState state, EventBus eventBus,
                      AstRewriter astRewriter, BlockFactory blockFactory) {
        this.state = state;
        this.eventBus = eventBus;
        this.astRewriter = astRewriter;
        this.blockFactory = blockFactory;
    }

    private String getCurrentCode() { return state.getCurrentCode(); }
    private CompilationUnit getCompilationUnit() { return state.getCompilationUnit().orElse(null); }

    private void triggerUpdate(String newCode) {
        String previousCode = getCurrentCode();
        eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(newCode, previousCode));
    }

    public void updateMethodInvocation(MethodInvocation mi, String newScope, String newMethodName, List<String> newParamTypes) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.updateMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, newScope, newMethodName, newParamTypes);
        triggerUpdate(newCode);
    }

    public void addArgumentToMethodInvocation(MethodInvocation mi, AddableExpression type) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addArgumentToMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, type);
        triggerUpdate(newCode);
    }

    public void addArgumentToMethodInvocation(MethodInvocation mi, Expression expr) {
        String newCode = astRewriter.addArgumentToMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, expr);
        triggerUpdate(newCode);
    }

    public void moveStatement(StatementBlock blockToMove, BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        String newCode = astRewriter.moveStatement(getCompilationUnit(), getCurrentCode(), blockToMove, sourceBody, targetBody, targetIndex);
        triggerUpdate(newCode);
    }

    public void replaceLiteralValue(Expression toReplace, String newLiteralValue) {
        String newCode = astRewriter.replaceLiteral(getCompilationUnit(), getCurrentCode(), toReplace, newLiteralValue);
        triggerUpdate(newCode);
    }

    public void renameEnum(EnumDeclaration enumNode, String newName) {
        String newCode = astRewriter.renameEnum(getCompilationUnit(), getCurrentCode(), enumNode, newName);
        triggerUpdate(newCode);
    }

    public void addEnumConstant(EnumDeclaration enumNode, String constantName) {
        String newCode = astRewriter.addEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, constantName);
        triggerUpdate(newCode);
    }

    public void deleteEnumConstant(EnumDeclaration enumNode, int index) {
        String newCode = astRewriter.deleteEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, index);
        triggerUpdate(newCode);
    }

    public void renameEnumConstant(EnumDeclaration enumNode, int index, String newName) {
        String newCode = astRewriter.renameEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, index, newName);
        triggerUpdate(newCode);
    }

    public void addMethodToClass(TypeDeclaration typeDecl, String methodName, String returnType, int index) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addMethodToClass(
                getCompilationUnit(),
                getCurrentCode(),
                typeDecl,
                methodName,
                returnType,
                index
        );
        triggerUpdate(newCode);
    }

    public void deleteMethod(MethodDeclaration method) {
        String newCode = astRewriter.deleteMethodFromClass(
                getCompilationUnit(),
                getCurrentCode(),
                method
        );
        triggerUpdate(newCode);
    }

    public void addStringArgumentToMethodInvocation(MethodInvocation mi, String text) {
        CompilationUnit cu = getCompilationUnit();
        if (cu == null) return;
        AST ast = cu.getAST();
        StringLiteral newArg = ast.newStringLiteral();
        newArg.setLiteralValue(text);
        String newCode = astRewriter.addArgumentToMethodInvocation(cu, getCurrentCode(), mi, newArg);
        triggerUpdate(newCode);
    }

    public void renameMethodParameter(MethodDeclaration method, int index, String newName) {
        String newCode = astRewriter.renameMethodParameter(getCompilationUnit(), getCurrentCode(), method, index, newName);
        triggerUpdate(newCode);
    }

    public void setMethodReturnType(MethodDeclaration method, String newTypeName) {
        String newCode = astRewriter.setMethodReturnType(getCompilationUnit(), getCurrentCode(), method, newTypeName);
        triggerUpdate(newCode);
    }

    public void addParameterToMethod(MethodDeclaration method, String typeName, String paramName) {
        String newCode = astRewriter.addParameterToMethod(getCompilationUnit(), getCurrentCode(), method, typeName, paramName);
        triggerUpdate(newCode);
    }

    public void deleteParameterFromMethod(MethodDeclaration method, int index) {
        String newCode = astRewriter.deleteParameterFromMethod(getCompilationUnit(), getCurrentCode(), method, index);
        triggerUpdate(newCode);
    }

    public void setReturnExpression(ReturnStatement returnStmt, AddableExpression type) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.setReturnExpression(getCompilationUnit(), getCurrentCode(), returnStmt, type);
        triggerUpdate(newCode);
    }

    public void addElementToList(ASTNode listNode, AddableExpression type, int insertIndex) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addElementToList(getCompilationUnit(), getCurrentCode(), listNode, type, insertIndex);
        triggerUpdate(newCode);
    }

    public void addEnumToClass(TypeDeclaration typeDecl, String enumName, int index) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addEnumToClass(getCompilationUnit(), getCurrentCode(), typeDecl, enumName, index);
        triggerUpdate(newCode);
    }

    public void deleteEnumFromClass(EnumDeclaration enumDecl) {
        String newCode = astRewriter.deleteEnumFromClass(getCompilationUnit(), getCurrentCode(), enumDecl);
        triggerUpdate(newCode);
    }

    public void deleteElementFromList(ASTNode listNode, int elementIndex) {
        String newCode = astRewriter.deleteElementFromList(getCompilationUnit(), getCurrentCode(), listNode, elementIndex);
        triggerUpdate(newCode);
    }

    public void updateComment(Comment commentNode, String newText) {
        String newCode = astRewriter.updateComment(getCurrentCode(), commentNode, newText);
        triggerUpdate(newCode);
    }

    public void deleteComment(Comment commentNode) {
        String newCode = astRewriter.deleteComment(getCurrentCode(), commentNode);
        triggerUpdate(newCode);
    }
    public void setVariableInitializer(VariableDeclarationStatement varDecl, AddableExpression type) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.setVariableInitializer(getCompilationUnit(), getCurrentCode(), varDecl, type);
        triggerUpdate(newCode);
    }
    public void replaceExpression(Expression toReplace, AddableExpression type) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.replaceExpression(getCompilationUnit(), getCurrentCode(), toReplace, type);
        triggerUpdate(newCode);
    }


    public void addStatement(BodyBlock targetBody, AddableBlock type, int index) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addStatement(getCompilationUnit(), getCurrentCode(), targetBody, type, index);
        triggerUpdate(newCode);
    }

    public void deleteElseFromIfStatement(IfStatement ifStmt) {
        String newCode = astRewriter.deleteElseFromIfStatement(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void convertElseToElseIf(IfStatement ifStmt) {
        String newCode = astRewriter.convertElseToElseIf(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void addElseToIfStatement(IfStatement ifStmt) {
        String newCode = astRewriter.addElseToIfStatement(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void replaceSimpleName(SimpleName toReplace, String newName) {
        String newCode = astRewriter.replaceSimpleName(getCompilationUnit(), getCurrentCode(), toReplace, newName);
        triggerUpdate(newCode);
    }

    public void deleteStatement(Statement toDelete) {
        String newCode = astRewriter.deleteNode(getCompilationUnit(), getCurrentCode(), toDelete);
        triggerUpdate(newCode);
    }

    public void replaceVariableType(VariableDeclarationStatement toReplace, String newTypeName) {
        String newCode = astRewriter.replaceVariableType(getCompilationUnit(), getCurrentCode(), toReplace, newTypeName);
        triggerUpdate(newCode);
    }

    public void updateAssignmentOperator(ASTNode node, String newOperatorSymbol) {
        String newCode = null;
        if (node instanceof Assignment) {
            Assignment.Operator op = getAssignmentOperator(newOperatorSymbol);
            if (op != null) {
                newCode = astRewriter.replaceAssignmentOperator(getCompilationUnit(), getCurrentCode(), (Assignment) node, op);
            }
        } else if (node instanceof PrefixExpression) {
            PrefixExpression.Operator op = getPrefixOperator(newOperatorSymbol);
            if (op != null) {
                newCode = astRewriter.replacePrefixOperator(getCompilationUnit(), getCurrentCode(), (org.eclipse.jdt.core.dom.PrefixExpression) node, op);
            }
        } else if (node instanceof org.eclipse.jdt.core.dom.PostfixExpression) {
            org.eclipse.jdt.core.dom.PostfixExpression.Operator op = getPostfixOperator(newOperatorSymbol);
            if (op != null) {
                newCode = astRewriter.replacePostfixOperator(getCompilationUnit(), getCurrentCode(), (org.eclipse.jdt.core.dom.PostfixExpression) node, op);
            }
        }
        if (newCode != null) {
            triggerUpdate(newCode);
        }
    }

    // --- NEW: Binary Operator Updates ---
    public void updateBinaryOperator(ASTNode node, String newOperatorSymbol) {
        if (node instanceof InfixExpression) {
            InfixExpression.Operator op = getInfixOperator(newOperatorSymbol);
            if (op != null) {
                String newCode = astRewriter.replaceInfixOperator(getCompilationUnit(), getCurrentCode(), (InfixExpression) node, op);
                triggerUpdate(newCode);
            }
        }
    }

    // --- Switch Case Methods ---
    public void addCaseToSwitch(SwitchStatement switchStmt) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addCaseToSwitch(getCompilationUnit(), getCurrentCode(), switchStmt);
        triggerUpdate(newCode);
    }

    public void moveSwitchCase(SwitchCase caseNode, boolean moveUp) {
        String newCode = astRewriter.moveSwitchCase(getCompilationUnit(), getCurrentCode(), caseNode, moveUp);
        triggerUpdate(newCode);
    }

    // --- Helpers ---
    private InfixExpression.Operator getInfixOperator(String symbol) {
        if ("+".equals(symbol)) return InfixExpression.Operator.PLUS;
        if ("-".equals(symbol)) return InfixExpression.Operator.MINUS;
        if ("*".equals(symbol)) return InfixExpression.Operator.TIMES;
        if ("/".equals(symbol)) return InfixExpression.Operator.DIVIDE;
        if ("%".equals(symbol)) return InfixExpression.Operator.REMAINDER;
        if ("==".equals(symbol)) return InfixExpression.Operator.EQUALS;
        if ("!=".equals(symbol)) return InfixExpression.Operator.NOT_EQUALS;
        if (">".equals(symbol)) return InfixExpression.Operator.GREATER;
        if (">=".equals(symbol)) return InfixExpression.Operator.GREATER_EQUALS;
        if ("<".equals(symbol)) return InfixExpression.Operator.LESS;
        if ("<=".equals(symbol)) return InfixExpression.Operator.LESS_EQUALS;
        if ("&&".equals(symbol)) return InfixExpression.Operator.CONDITIONAL_AND;
        if ("||".equals(symbol)) return InfixExpression.Operator.CONDITIONAL_OR;
        return null;
    }

    private Assignment.Operator getAssignmentOperator(String symbol) {
        if ("=".equals(symbol)) return Assignment.Operator.ASSIGN;
        if ("+=".equals(symbol)) return Assignment.Operator.PLUS_ASSIGN;
        if ("-=".equals(symbol)) return Assignment.Operator.MINUS_ASSIGN;
        if ("*=".equals(symbol)) return Assignment.Operator.TIMES_ASSIGN;
        if ("/=".equals(symbol)) return Assignment.Operator.DIVIDE_ASSIGN;
        if ("%=".equals(symbol)) return Assignment.Operator.REMAINDER_ASSIGN;
        return null;
    }

    private org.eclipse.jdt.core.dom.PrefixExpression.Operator getPrefixOperator(String symbol) {
        if ("++".equals(symbol)) return org.eclipse.jdt.core.dom.PrefixExpression.Operator.INCREMENT;
        if ("--".equals(symbol)) return org.eclipse.jdt.core.dom.PrefixExpression.Operator.DECREMENT;
        return null;
    }

    private org.eclipse.jdt.core.dom.PostfixExpression.Operator getPostfixOperator(String symbol) {
        if ("++".equals(symbol)) return org.eclipse.jdt.core.dom.PostfixExpression.Operator.INCREMENT;
        if ("--".equals(symbol)) return org.eclipse.jdt.core.dom.PostfixExpression.Operator.DECREMENT;
        return null;
    }
}