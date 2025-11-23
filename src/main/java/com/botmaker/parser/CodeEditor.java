package com.botmaker.parser;

import com.botmaker.state.*;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;

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

    // ... [Existing getters and triggerUpdate] ...

    private String getCurrentCode() { return state.getCurrentCode(); }
    private CompilationUnit getCompilationUnit() { return state.getCompilationUnit().orElse(null); }

    private void triggerUpdate(String newCode) {
        String previousCode = getCurrentCode();
        eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(newCode, previousCode));
    }

    // ... [Existing methods: updateMethodInvocation, addArgumentToMethodInvocation, moveStatement, replaceLiteralValue, addStringArgumentToMethodInvocation, renameMethodParameter, setMethodReturnType, addParameterToMethod, deleteParameterFromMethod, setReturnExpression, addElementToList, deleteElementFromList, updateComment, deleteComment, replaceExpression, addStatement, deleteElseFromIfStatement, convertElseToElseIf, addElseToIfStatement, replaceSimpleName, deleteStatement, replaceVariableType, updateAssignmentOperator] ...

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

    public void moveStatement(StatementBlock blockToMove, BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        String newCode = astRewriter.moveStatement(getCompilationUnit(), getCurrentCode(), blockToMove, sourceBody, targetBody, targetIndex);
        triggerUpdate(newCode);
    }

    public void replaceLiteralValue(Expression toReplace, String newLiteralValue) {
        String newCode = astRewriter.replaceLiteral(getCompilationUnit(), getCurrentCode(), toReplace, newLiteralValue);
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

    public void replaceExpression(Expression toReplace, com.botmaker.ui.AddableExpression type) {
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

    // --- NEW: Switch Case Methods ---

    public void addCaseToSwitch(SwitchStatement switchStmt) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addCaseToSwitch(getCompilationUnit(), getCurrentCode(), switchStmt);
        triggerUpdate(newCode);
    }

    public void moveSwitchCase(SwitchCase caseNode, boolean moveUp) {
        String newCode = astRewriter.moveSwitchCase(getCompilationUnit(), getCurrentCode(), caseNode, moveUp);
        triggerUpdate(newCode);
    }

    // --- Private Helpers ---
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