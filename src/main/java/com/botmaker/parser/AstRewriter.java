package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.parser.handlers.*;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.parser.helpers.TypeConversionHelper;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates AST rewriting operations by delegating to specialized handlers.
 * Reduced from 580 lines to ~200 lines by extracting handlers.
 */
public class AstRewriter {

    private final NodeCreator nodeCreator;
    private final TypeReplacementHandler typeHandler;
    private final OperatorReplacementHandler operatorHandler;
    private final ListManipulationHandler listHandler;
    private final MethodManipulationHandler methodHandler;
    private final EnumManipulationHandler enumHandler;

    public AstRewriter(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
        this.typeHandler = new TypeReplacementHandler(nodeCreator);
        this.operatorHandler = new OperatorReplacementHandler();
        this.listHandler = new ListManipulationHandler(nodeCreator);
        this.methodHandler = new MethodManipulationHandler(nodeCreator);
        this.enumHandler = new EnumManipulationHandler();
    }

    // ========================================================================
    // STATEMENT MANIPULATION
    // ========================================================================

    public String moveStatement(CompilationUnit cu, String originalCode, StatementBlock blockToMove,
                                BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement statement = (Statement) blockToMove.getAstNode();

        ListRewrite sourceListRewrite = getListRewriteForBody(rewriter, sourceBody);
        ListRewrite targetListRewrite = getListRewriteForBody(rewriter, targetBody);

        Statement copiedStatement = (Statement) ASTNode.copySubtree(ast, statement);
        sourceListRewrite.remove(statement, null);
        insertIntoList(targetListRewrite, targetBody, copiedStatement, targetIndex);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String addStatement(CompilationUnit cu, String originalCode, BodyBlock targetBody,
                               AddableBlock type, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement newStatement;
        if (type == AddableBlock.COMMENT) {
            newStatement = (Statement) rewriter.createStringPlaceholder("// Comment", ASTNode.EMPTY_STATEMENT);
        } else {
            newStatement = nodeCreator.createDefaultStatement(ast, type, cu, rewriter);
        }

        if (newStatement == null) return originalCode;

        ListRewrite listRewrite = getListRewriteForBody(rewriter, targetBody);
        insertIntoList(listRewrite, targetBody, newStatement, index);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String deleteNode(CompilationUnit cu, String originalCode, ASTNode toDelete) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(toDelete, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Smart deletion for statements. Handles special cases like unwrapping 'else if' chains.
     */
    public String deleteStatement(CompilationUnit cu, String originalCode, Statement statement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Check if we are deleting an 'else if' block (IfStatement nested in an else property)
        if (statement instanceof IfStatement) {
            IfStatement ifStmt = (IfStatement) statement;
            if (ifStmt.getParent() instanceof IfStatement) {
                IfStatement parent = (IfStatement) ifStmt.getParent();

                // Confirm it is the 'else' child
                if (parent.getElseStatement() == ifStmt) {

                    Statement childElse = ifStmt.getElseStatement();

                    // If the node being deleted has its own else/else-if, pull it up
                    if (childElse != null) {
                        ASTNode moveTarget = rewriter.createMoveTarget(childElse);
                        rewriter.replace(ifStmt, moveTarget, null);
                        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
                    }
                }
            }
        }

        // Default behavior: just remove the node
        rewriter.remove(statement, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    // ========================================================================
    // EXPRESSION MANIPULATION
    // ========================================================================

    public String replaceExpression(CompilationUnit cu, String originalCode, Expression toReplace,
                                    AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        String contextType = TypeConversionHelper.inferContextType(toReplace);
        Expression newExpression = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, contextType);

        if (newExpression == null) return originalCode;

        rewriter.replace(toReplace, newExpression, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String replaceLiteral(CompilationUnit cu, String originalCode, Expression toReplace,
                                 String newLiteralValue) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newExpression;
        if (toReplace instanceof StringLiteral) {
            StringLiteral newString = ast.newStringLiteral();
            newString.setLiteralValue(newLiteralValue);
            newExpression = newString;
        } else if (toReplace instanceof NumberLiteral) {
            newExpression = ast.newNumberLiteral(newLiteralValue);
        } else if (toReplace instanceof BooleanLiteral) {
            newExpression = ast.newBooleanLiteral(Boolean.parseBoolean(newLiteralValue));
        } else {
            return originalCode;
        }

        rewriter.replace(toReplace, newExpression, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String replaceSimpleName(CompilationUnit cu, String originalCode, SimpleName toReplace,
                                    String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(toReplace, ast.newSimpleName(newName), null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    // ========================================================================
    // TYPE OPERATIONS - Delegated to TypeReplacementHandler
    // ========================================================================

    public String replaceVariableType(CompilationUnit cu, String originalCode,
                                      VariableDeclarationStatement varDecl, String newTypeName) {
        return typeHandler.replaceVariableType(cu, originalCode, varDecl, newTypeName);
    }

    public String replaceFieldType(CompilationUnit cu, String originalCode,
                                   FieldDeclaration fieldDecl, String newTypeName) {
        return typeHandler.replaceFieldType(cu, originalCode, fieldDecl, newTypeName);
    }

    // ========================================================================
    // OPERATOR OPERATIONS - Delegated to OperatorReplacementHandler
    // ========================================================================

    public String replaceInfixOperator(CompilationUnit cu, String originalCode,
                                       InfixExpression infix, InfixExpression.Operator newOp) {
        return operatorHandler.replaceInfixOperator(cu, originalCode, infix, newOp);
    }

    public String replaceAssignmentOperator(CompilationUnit cu, String originalCode,
                                            Assignment assignment, Assignment.Operator newOp) {
        return operatorHandler.replaceAssignmentOperator(cu, originalCode, assignment, newOp);
    }

    public String replacePrefixOperator(CompilationUnit cu, String originalCode,
                                        PrefixExpression prefix, PrefixExpression.Operator newOp) {
        return operatorHandler.replacePrefixOperator(cu, originalCode, prefix, newOp);
    }

    public String replacePostfixOperator(CompilationUnit cu, String originalCode,
                                         PostfixExpression postfix, PostfixExpression.Operator newOp) {
        return operatorHandler.replacePostfixOperator(cu, originalCode, postfix, newOp);
    }

    // ========================================================================
    // LIST OPERATIONS - Delegated to ListManipulationHandler
    // ========================================================================

    public String addElementToList(CompilationUnit cu, String originalCode, ASTNode listNode,
                                   AddableExpression type, int insertIndex) {
        return listHandler.addElementToList(cu, originalCode, listNode, type, insertIndex);
    }

    public String deleteElementFromList(CompilationUnit cu, String originalCode,
                                        ASTNode listNode, int elementIndex) {
        return listHandler.deleteElementFromList(cu, originalCode, listNode, elementIndex);
    }

    // ========================================================================
    // METHOD OPERATIONS - Delegated to MethodManipulationHandler
    // ========================================================================

    public String addMethodToClass(CompilationUnit cu, String originalCode, TypeDeclaration typeDecl,
                                   String methodName, String returnType, int index) {
        return methodHandler.addMethodToClass(cu, originalCode, typeDecl, methodName, returnType, index);
    }

    public String deleteMethodFromClass(CompilationUnit cu, String originalCode,
                                        MethodDeclaration method) {
        return methodHandler.deleteMethodFromClass(cu, originalCode, method);
    }

    public String updateMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi,
                                         String newScope, String newMethodName, List<String> newParamTypes) {
        return methodHandler.updateMethodInvocation(cu, originalCode, mi, newScope, newMethodName, newParamTypes);
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode,
                                                MethodInvocation mi, AddableExpression type) {
        return methodHandler.addArgumentToMethodInvocation(cu, originalCode, mi, type);
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode,
                                                MethodInvocation mi, Expression newArgument) {
        return methodHandler.addArgumentToMethodInvocation(cu, originalCode, mi, newArgument);
    }

    public String renameMethodParameter(CompilationUnit cu, String originalCode,
                                        MethodDeclaration method, int index, String newName) {
        return methodHandler.renameMethodParameter(cu, originalCode, method, index, newName);
    }

    public String setMethodReturnType(CompilationUnit cu, String originalCode,
                                      MethodDeclaration method, String newTypeName) {
        return methodHandler.setMethodReturnType(cu, originalCode, method, newTypeName);
    }

    public String addParameterToMethod(CompilationUnit cu, String originalCode,
                                       MethodDeclaration method, String typeName, String paramName) {
        return methodHandler.addParameterToMethod(cu, originalCode, method, typeName, paramName);
    }

    public String deleteParameterFromMethod(CompilationUnit cu, String originalCode,
                                            MethodDeclaration method, int index) {
        return methodHandler.deleteParameterFromMethod(cu, originalCode, method, index);
    }

    public String setReturnExpression(CompilationUnit cu, String originalCode,
                                      ReturnStatement returnStmt, AddableExpression type) {
        return methodHandler.setReturnExpression(cu, originalCode, returnStmt, type);
    }

    // ========================================================================
    // ENUM OPERATIONS - Delegated to EnumManipulationHandler
    // ========================================================================

    public String addEnumToClass(CompilationUnit cu, String originalCode, TypeDeclaration typeDecl,
                                 String enumName, int index) {
        return enumHandler.addEnumToClass(cu, originalCode, typeDecl, enumName, index);
    }

    public String deleteEnumFromClass(CompilationUnit cu, String originalCode,
                                      EnumDeclaration enumDecl) {
        return enumHandler.deleteEnumFromClass(cu, originalCode, enumDecl);
    }

    public String renameEnum(CompilationUnit cu, String originalCode, EnumDeclaration enumNode,
                             String newName) {
        return enumHandler.renameEnum(cu, originalCode, enumNode, newName);
    }

    public String addEnumConstant(CompilationUnit cu, String originalCode, EnumDeclaration enumNode,
                                  String constantName) {
        return enumHandler.addEnumConstant(cu, originalCode, enumNode, constantName);
    }

    public String deleteEnumConstant(CompilationUnit cu, String originalCode,
                                     EnumDeclaration enumNode, int index) {
        return enumHandler.deleteEnumConstant(cu, originalCode, enumNode, index);
    }

    public String renameEnumConstant(CompilationUnit cu, String originalCode, EnumDeclaration enumNode,
                                     int index, String newName) {
        return enumHandler.renameEnumConstant(cu, originalCode, enumNode, index, newName);
    }

    // ========================================================================
    // VARIABLE/FIELD INITIALIZERS
    // ========================================================================

    public String setVariableInitializer(CompilationUnit cu, String originalCode,
                                         VariableDeclarationStatement varDecl, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);

        String typeName = varDecl.getType().toString();
        String contextType = TypeConversionHelper.unwrapArrayListType(typeName);

        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, contextType);
        if (newExpr == null) return originalCode;

        if (fragment.getInitializer() == null) {
            rewriter.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(fragment.getInitializer(), newExpr, null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String setFieldInitializer(CompilationUnit cu, String originalCode,
                                      FieldDeclaration fieldDecl, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);

        String typeName = fieldDecl.getType().toString();
        String contextType = TypeConversionHelper.unwrapArrayListType(typeName);

        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, contextType);
        if (newExpr == null) return originalCode;

        if (fragment.getInitializer() == null) {
            rewriter.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(fragment.getInitializer(), newExpr, null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    // ========================================================================
    // IF/ELSE OPERATIONS
    // ========================================================================

    public String convertElseToElseIf(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement elseStatement = ifStatement.getElseStatement();
        if (elseStatement != null && elseStatement.getNodeType() == ASTNode.BLOCK) {
            IfStatement newElseIf = ast.newIfStatement();
            newElseIf.setExpression(ast.newBooleanLiteral(true));
            newElseIf.setThenStatement((Block) ASTNode.copySubtree(ast, elseStatement));
            rewriter.replace(elseStatement, newElseIf, null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String addElseToIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (ifStatement.getElseStatement() == null) {
            rewriter.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, ast.newBlock(), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String deleteElseFromIfStatement(CompilationUnit cu, String originalCode,
                                            IfStatement ifStatement) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());

        if (ifStatement.getElseStatement() != null) {
            rewriter.remove(ifStatement.getElseStatement(), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    // ========================================================================
    // SWITCH OPERATIONS
    // ========================================================================

    public String addCaseToSwitch(CompilationUnit cu, String originalCode, SwitchStatement switchStmt) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(switchStmt, SwitchStatement.STATEMENTS_PROPERTY);

        SwitchCase newCase = ast.newSwitchCase();
        int count = 0;
        for (Object o : switchStmt.statements()) {
            if (o instanceof SwitchCase) count++;
        }

        try {
            newCase.expressions().add(ast.newNumberLiteral(String.valueOf(count)));
        } catch (Exception ignored) {}

        listRewrite.insertLast(newCase, null);
        listRewrite.insertLast(ast.newBreakStatement(), null);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String moveSwitchCase(CompilationUnit cu, String originalCode, SwitchCase caseNode,
                                 boolean moveUp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        SwitchStatement parent = (SwitchStatement) caseNode.getParent();
        List<Statement> statements = parent.statements();

        // Group statements into chunks by case
        List<List<Statement>> chunks = new ArrayList<>();
        List<Statement> currentChunk = null;

        for (Statement stmt : statements) {
            if (stmt instanceof SwitchCase) {
                if (currentChunk != null) chunks.add(currentChunk);
                currentChunk = new ArrayList<>();
            }
            if (currentChunk != null) currentChunk.add(stmt);
        }
        if (currentChunk != null) chunks.add(currentChunk);

        // Find target chunk
        int targetIndex = -1;
        for (int i = 0; i < chunks.size(); i++) {
            if (!chunks.get(i).isEmpty() && chunks.get(i).get(0) == caseNode) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) return originalCode;

        int neighborIndex = moveUp ? targetIndex - 1 : targetIndex + 1;
        if (neighborIndex < 0 || neighborIndex >= chunks.size()) return originalCode;

        List<Statement> targetChunk = chunks.get(targetIndex);
        List<Statement> neighborChunk = chunks.get(neighborIndex);

        ListRewrite listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);

        if (moveUp) {
            ASTNode insertPoint = neighborChunk.get(0);
            for (Statement stmt : targetChunk) {
                ASTNode moveTarget = rewriter.createMoveTarget(stmt);
                listRewrite.insertBefore(moveTarget, insertPoint, null);
            }
        } else {
            ASTNode insertPoint = targetChunk.get(0);
            for (Statement stmt : neighborChunk) {
                ASTNode moveTarget = rewriter.createMoveTarget(stmt);
                listRewrite.insertBefore(moveTarget, insertPoint, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    // ========================================================================
    // COMMENT OPERATIONS
    // ========================================================================

    public String updateComment(String originalCode, Comment commentNode, String newText) {
        try {
            IDocument document = new Document(originalCode);
            String replacement = newText.contains("\n") ? "/* " + newText + " */" : "// " + newText;
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), replacement);
            return document.get();
        } catch (Exception e) {
            return originalCode;
        }
    }

    public String deleteComment(String originalCode, Comment commentNode) {
        try {
            IDocument document = new Document(originalCode);
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), "");
            return document.get();
        } catch (Exception e) {
            return originalCode;
        }
    }

    // ========================================================================
    // PRIVATE HELPER METHODS
    // ========================================================================

    private ListRewrite getListRewriteForBody(ASTRewrite rewriter, BodyBlock body) {
        ASTNode node = body.getAstNode();
        if (node instanceof Block) {
            return rewriter.getListRewrite(node, Block.STATEMENTS_PROPERTY);
        } else if (node instanceof SwitchCase) {
            return rewriter.getListRewrite(node.getParent(), SwitchStatement.STATEMENTS_PROPERTY);
        }
        throw new IllegalArgumentException("Unsupported body node type: " + node.getClass());
    }

    private void insertIntoList(ListRewrite listRewrite, BodyBlock body, Statement newStatement,
                                int relativeIndex) {
        ASTNode node = body.getAstNode();
        if (node instanceof Block) {
            listRewrite.insertAt(newStatement, relativeIndex, null);
        } else if (node instanceof SwitchCase) {
            SwitchCase caseNode = (SwitchCase) node;
            SwitchStatement parent = (SwitchStatement) caseNode.getParent();
            List<?> allStatements = parent.statements();
            int caseIndex = allStatements.indexOf(caseNode);
            int absoluteIndex = caseIndex + 1 + relativeIndex;
            listRewrite.insertAt(newStatement, absoluteIndex, null);
        }
    }
}