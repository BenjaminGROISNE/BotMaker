package com.botmaker.parser;

import com.botmaker.state.*;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeInfo;
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

    private String getCurrentCode() { return state.getCurrentCode(); }
    private CompilationUnit getCompilationUnit() { return state.getCompilationUnit().orElse(null); }

    /**
     * Gatekeeper Method: Checks if the current file is writable.
     * If not, it sends a status message and forces a UI refresh to revert visual changes.
     */
    private boolean canModify() {
        if (state.getActiveFile() != null) {
            String path = state.getActiveFile().getPath().toString().replace("\\", "/");
            if (path.contains("com/botmaker/library")) {
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("⚠️ Cannot edit library files (Read-Only)"));
                // Force UI reset to revert local visual changes (like text field inputs snapping back)
                eventBus.publish(new CoreApplicationEvents.UIRefreshRequestedEvent(state.getCurrentCode()));
                return false;
            }
        }
        return true;
    }

    private void triggerUpdate(String newCode) {
        String previousCode = getCurrentCode();
        eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(newCode, previousCode));
    }

    public void pasteCode(BodyBlock targetBody, int index, String codeToPaste) {
        if (!canModify()) return;
        // Use AstRewriter to inject the raw string
        String newCode = astRewriter.pasteCodeString(
                getCompilationUnit(),
                getCurrentCode(),
                targetBody,
                index,
                codeToPaste
        );
        triggerUpdate(newCode);
    }

    public void updateMethodInvocation(MethodInvocation mi, String newScope, String newMethodName, List<String> newParamTypes) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.updateMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, newScope, newMethodName, newParamTypes);
        triggerUpdate(newCode);
    }

    public void addArgumentToMethodInvocation(MethodInvocation mi, AddableExpression type) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addArgumentToMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, type);
        triggerUpdate(newCode);
    }

    /**
     * Wraps a bare ArrayInitializer in ArrayCreation when needed.
     */
    private Expression wrapArrayInitializerIfNeeded(Expression initializer, Type varType, AST ast) {
        if (initializer instanceof ArrayInitializer && varType.isArrayType()) {
            ArrayType arrayType = (ArrayType) varType;
            ArrayCreation arrayCreation = ast.newArrayCreation();
            arrayCreation.setType((ArrayType) ASTNode.copySubtree(ast, arrayType));
            arrayCreation.setInitializer((ArrayInitializer) ASTNode.copySubtree(ast, initializer));
            return arrayCreation;
        }
        return initializer;
    }

    public void addArgumentToMethodInvocation(MethodInvocation mi, Expression expr) {
        if (!canModify()) return;
        String newCode = astRewriter.addArgumentToMethodInvocation(getCompilationUnit(), getCurrentCode(), mi, expr);
        triggerUpdate(newCode);
    }

    public void moveStatement(StatementBlock blockToMove, BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        if (!canModify()) return;
        String newCode = astRewriter.moveStatement(getCompilationUnit(), getCurrentCode(), blockToMove, sourceBody, targetBody, targetIndex);
        triggerUpdate(newCode);
    }

    public void replaceLiteralValue(Expression toReplace, String newLiteralValue) {
        if (!canModify()) return;
        String newCode = astRewriter.replaceLiteral(getCompilationUnit(), getCurrentCode(), toReplace, newLiteralValue);
        triggerUpdate(newCode);
    }

    public void renameEnum(EnumDeclaration enumNode, String newName) {
        if (!canModify()) return;
        String newCode = astRewriter.renameEnum(getCompilationUnit(), getCurrentCode(), enumNode, newName);
        triggerUpdate(newCode);
    }

    public void addEnumConstant(EnumDeclaration enumNode, String constantName) {
        if (!canModify()) return;
        String newCode = astRewriter.addEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, constantName);
        triggerUpdate(newCode);
    }

    public void deleteEnumConstant(EnumDeclaration enumNode, int index) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, index);
        triggerUpdate(newCode);
    }

    public void renameEnumConstant(EnumDeclaration enumNode, int index, String newName) {
        if (!canModify()) return;
        String newCode = astRewriter.renameEnumConstant(getCompilationUnit(), getCurrentCode(), enumNode, index, newName);
        triggerUpdate(newCode);
    }

    public void addMethodToClass(TypeDeclaration typeDecl, String methodName, String returnType, int index) {
        if (!canModify()) return;
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
        if (!canModify()) return;
        String newCode = astRewriter.deleteMethodFromClass(
                getCompilationUnit(),
                getCurrentCode(),
                method
        );
        triggerUpdate(newCode);
    }

    public void addStringArgumentToMethodInvocation(MethodInvocation mi, String text) {
        if (!canModify()) return;
        CompilationUnit cu = getCompilationUnit();
        if (cu == null) return;
        AST ast = cu.getAST();
        StringLiteral newArg = ast.newStringLiteral();
        newArg.setLiteralValue(text);
        String newCode = astRewriter.addArgumentToMethodInvocation(cu, getCurrentCode(), mi, newArg);
        triggerUpdate(newCode);
    }

    public void renameMethodParameter(MethodDeclaration method, int index, String newName) {
        if (!canModify()) return;
        String newCode = astRewriter.renameMethodParameter(getCompilationUnit(), getCurrentCode(), method, index, newName);
        triggerUpdate(newCode);
    }

    public void setMethodReturnType(MethodDeclaration method, String newTypeName) {
        if (!canModify()) return;
        String newCode = astRewriter.setMethodReturnType(getCompilationUnit(), getCurrentCode(), method, newTypeName);
        triggerUpdate(newCode);
    }

    public void addParameterToMethod(MethodDeclaration method, String typeName, String paramName) {
        if (!canModify()) return;
        String newCode = astRewriter.addParameterToMethod(getCompilationUnit(), getCurrentCode(), method, typeName, paramName);
        triggerUpdate(newCode);
    }

    public void deleteParameterFromMethod(MethodDeclaration method, int index) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteParameterFromMethod(getCompilationUnit(), getCurrentCode(), method, index);
        triggerUpdate(newCode);
    }

    public void renameMethod(MethodDeclaration method, String newName) {
        if (!canModify()) return;
        String newCode = astRewriter.renameMethod(getCompilationUnit(), getCurrentCode(), method, newName);
        triggerUpdate(newCode);
    }

    public void moveBodyDeclaration(BodyDeclaration decl, TypeDeclaration targetType, int index) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.moveBodyDeclaration(getCompilationUnit(), getCurrentCode(), decl, targetType, index);
        triggerUpdate(newCode);
    }

    public void setReturnExpression(ReturnStatement returnStmt, AddableExpression type) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.setReturnExpression(getCompilationUnit(), getCurrentCode(), returnStmt, type);
        triggerUpdate(newCode);
    }

    public void addElementToList(ASTNode listNode, AddableExpression type, int insertIndex) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addElementToList(getCompilationUnit(), getCurrentCode(), listNode, type, insertIndex);
        triggerUpdate(newCode);
    }

    public void addEnumToClass(TypeDeclaration typeDecl, String enumName, int index) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addEnumToClass(getCompilationUnit(), getCurrentCode(), typeDecl, enumName, index);
        triggerUpdate(newCode);
    }

    public void deleteEnumFromClass(EnumDeclaration enumDecl) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteEnumFromClass(getCompilationUnit(), getCurrentCode(), enumDecl);
        triggerUpdate(newCode);
    }

    public void deleteElementFromList(ASTNode listNode, int elementIndex) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteElementFromList(getCompilationUnit(), getCurrentCode(), listNode, elementIndex);
        triggerUpdate(newCode);
    }

    public void updateComment(Comment commentNode, String newText) {
        if (!canModify()) return;
        String newCode = astRewriter.updateComment(getCurrentCode(), commentNode, newText);
        triggerUpdate(newCode);
    }

    public void deleteComment(Comment commentNode) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteComment(getCurrentCode(), commentNode);
        triggerUpdate(newCode);
    }

    public void setVariableInitializer(VariableDeclarationStatement varDecl, AddableExpression type) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.setVariableInitializer(getCompilationUnit(), getCurrentCode(), varDecl, type);
        triggerUpdate(newCode);
    }

    public void setFieldInitializer(FieldDeclaration fieldDecl, AddableExpression type) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.setFieldInitializer(getCompilationUnit(), getCurrentCode(), fieldDecl, type);
        triggerUpdate(newCode);
    }

    public void setFieldInitializerToDefault(FieldDeclaration fieldDecl, String uiTargetType) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);

        // Determine which AddableExpression to create based on type
        AddableExpression defaultType;

        switch (uiTargetType) {
            case "number":
                defaultType = AddableExpression.NUMBER;
                break;
            case "boolean":
                defaultType = AddableExpression.FALSE;
                break;
            case "String":
                defaultType = AddableExpression.TEXT;
                break;
            case "list":
                defaultType = AddableExpression.LIST;
                break;
            case "enum":
                defaultType = AddableExpression.ENUM_CONSTANT;
                break;
            default:
                // For unknown types, use a variable reference
                defaultType = AddableExpression.VARIABLE;
                break;
        }

        String newCode = astRewriter.setFieldInitializer(
                getCompilationUnit(),
                getCurrentCode(),
                fieldDecl,
                defaultType
        );
        triggerUpdate(newCode);
    }

    /**
     * TypeInfo overload for setFieldInitializerToDefault
     */
    public void setFieldInitializerToDefault(FieldDeclaration fieldDecl, TypeInfo fieldType) {
        if (!canModify()) return;
        // Convert TypeInfo to string and call existing method
        setFieldInitializerToDefault(fieldDecl, fieldType.getTypeName());
    }

    public void replaceExpression(Expression toReplace, AddableExpression type) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.replaceExpression(getCompilationUnit(), getCurrentCode(), toReplace, type);
        triggerUpdate(newCode);
    }

    public void addStatement(BodyBlock targetBody, AddableBlock type, int index) {
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addStatement(getCompilationUnit(), getCurrentCode(), targetBody, type, index);
        triggerUpdate(newCode);

        // FIRE EVENT: Block Added
        eventBus.publish(new CoreApplicationEvents.BlockAddedEvent(type));
    }

    public void deleteElseFromIfStatement(IfStatement ifStmt) {
        if (!canModify()) return;
        String newCode = astRewriter.deleteElseFromIfStatement(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void convertElseToElseIf(IfStatement ifStmt) {
        if (!canModify()) return;
        String newCode = astRewriter.convertElseToElseIf(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void addElseToIfStatement(IfStatement ifStmt) {
        if (!canModify()) return;
        String newCode = astRewriter.addElseToIfStatement(getCompilationUnit(), getCurrentCode(), ifStmt);
        triggerUpdate(newCode);
    }

    public void replaceSimpleName(SimpleName toReplace, String newName) {
        if (!canModify()) return;
        String newCode = astRewriter.replaceSimpleName(getCompilationUnit(), getCurrentCode(), toReplace, newName);
        triggerUpdate(newCode);
    }

    public void deleteStatement(Statement toDelete) {
        if (!canModify()) return;
        // Use the smart delete logic in AstRewriter
        String newCode = astRewriter.deleteStatement(getCompilationUnit(), getCurrentCode(), toDelete);
        triggerUpdate(newCode);
    }

    public void replaceVariableType(VariableDeclarationStatement toReplace, String newTypeName) {
        if (!canModify()) return;
        String newCode = astRewriter.replaceVariableType(getCompilationUnit(), getCurrentCode(), toReplace, newTypeName);
        triggerUpdate(newCode);
    }

    public void replaceFieldType(FieldDeclaration fieldDecl, String newTypeName) {
        if (!canModify()) return;
        String newCode = astRewriter.replaceFieldType(getCompilationUnit(), getCurrentCode(), fieldDecl, newTypeName);
        triggerUpdate(newCode);
    }

    public void updateAssignmentOperator(ASTNode node, String newOperatorSymbol) {
        if (!canModify()) return;
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
        if (!canModify()) return;
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
        if (!canModify()) return;
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addCaseToSwitch(getCompilationUnit(), getCurrentCode(), switchStmt);
        triggerUpdate(newCode);
    }

    public void moveSwitchCase(SwitchCase caseNode, boolean moveUp) {
        if (!canModify()) return;
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