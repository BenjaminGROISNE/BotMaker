package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles modifying existing Java code via AST Rewriting.
 * Delegates node creation to NodeCreator.
 */
public class AstRewriter {

    private final NodeCreator nodeCreator;

    public AstRewriter(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    public String moveStatement(CompilationUnit cu, String originalCode, StatementBlock blockToMove, BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement statement = (Statement) blockToMove.getAstNode();
        Block sourceBlock = (Block) sourceBody.getAstNode();
        Block targetBlock = (Block) targetBody.getAstNode();

        ListRewrite sourceListRewrite = rewriter.getListRewrite(sourceBlock, Block.STATEMENTS_PROPERTY);
        ListRewrite targetListRewrite = rewriter.getListRewrite(targetBlock, Block.STATEMENTS_PROPERTY);

        if (sourceBody == targetBody) {
            int currentIndex = sourceBlock.statements().indexOf(statement);
            if (currentIndex == targetIndex) return originalCode;
        }

        Statement copiedStatement = (Statement) ASTNode.copySubtree(ast, statement);
        sourceListRewrite.remove(statement, null);
        targetListRewrite.insertAt(copiedStatement, targetIndex, null);

        return applyRewrite(rewriter, originalCode);
    }

    public String addStatement(CompilationUnit cu, String originalCode, BodyBlock targetBody, AddableBlock type, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement newStatement;
        if (type == AddableBlock.COMMENT) {
            newStatement = (Statement) rewriter.createStringPlaceholder("// Comment", ASTNode.EMPTY_STATEMENT);
        } else {
            newStatement = nodeCreator.createDefaultStatement(ast, type, cu, rewriter);
        }

        if (newStatement == null) return originalCode;

        Block targetAstBlock = (Block) targetBody.getAstNode();
        ListRewrite listRewrite = rewriter.getListRewrite(targetAstBlock, Block.STATEMENTS_PROPERTY);
        listRewrite.insertAt(newStatement, index, null);

        return applyRewrite(rewriter, originalCode);
    }

    public String replaceExpression(CompilationUnit cu, String originalCode, Expression toReplace, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newExpression = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newExpression == null) return originalCode;

        rewriter.replace(toReplace, newExpression, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceLiteral(CompilationUnit cu, String originalCode, Expression toReplace, String newLiteralValue) {
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
        return applyRewrite(rewriter, originalCode);
    }

    public String updateComment(String originalCode, Comment commentNode, String newText) {
        try {
            IDocument document = new Document(originalCode);
            String replacement = newText.contains("\n") ? "/* " + newText + " */" : "// " + newText;
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), replacement);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String deleteComment(String originalCode, Comment commentNode) {
        try {
            IDocument document = new Document(originalCode);
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), "");
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, Expression newArgument) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        listRewrite.insertLast(newArgument, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceSimpleName(CompilationUnit cu, String originalCode, SimpleName toReplace, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(toReplace, ast.newSimpleName(newName), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteNode(CompilationUnit cu, String originalCode, ASTNode toDelete) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(toDelete, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceVariableType(CompilationUnit cu, String originalCode, VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (newTypeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = null;

            List<Expression> valuesToPreserve = new ArrayList<>();
            String oldLeaf = TypeManager.getLeafType(varDecl.getType().toString());
            String newLeaf = TypeManager.getLeafType(newTypeName);

            if (oldLeaf.equals(newLeaf) && currentInitializer != null) {
                collectLeafValues(currentInitializer, valuesToPreserve);
            }

            if (newTypeName.startsWith("ArrayList<")) {
                newInitializer = nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, valuesToPreserve);
            } else if (newTypeName.endsWith("[]")) {
                ArrayCreation creation = ast.newArrayCreation();
                creation.setType((ArrayType) TypeManager.createTypeNode(ast, newTypeName));
                ArrayInitializer ai = ast.newArrayInitializer();
                if (!valuesToPreserve.isEmpty()) {
                    for(Expression val : valuesToPreserve) ai.expressions().add(ASTNode.copySubtree(ast, val));
                }
                creation.setInitializer(ai);
                newInitializer = creation;
            } else {
                newInitializer = !valuesToPreserve.isEmpty() ?
                        (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0)) :
                        nodeCreator.createDefaultInitializer(ast, newTypeName);
            }

            if (newInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String addElementToList(CompilationUnit cu, String originalCode, ASTNode listNode, AddableExpression type, int insertIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newElement = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newElement == null) return originalCode;

        if (listNode instanceof ArrayInitializer) {
            rewriter.getListRewrite(listNode, ArrayInitializer.EXPRESSIONS_PROPERTY).insertAt(newElement, insertIndex, null);
        } else if (listNode instanceof MethodInvocation) {
            rewriter.getListRewrite(listNode, MethodInvocation.ARGUMENTS_PROPERTY).insertAt(newElement, insertIndex, null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteElementFromList(CompilationUnit cu, String originalCode, ASTNode listNode, int elementIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<?> expressions;
        ChildListPropertyDescriptor property;

        if (listNode instanceof ArrayInitializer) {
            expressions = ((ArrayInitializer) listNode).expressions();
            property = ArrayInitializer.EXPRESSIONS_PROPERTY;
        } else if (listNode instanceof MethodInvocation) {
            expressions = ((MethodInvocation) listNode).arguments();
            property = MethodInvocation.ARGUMENTS_PROPERTY;
        } else {
            return originalCode;
        }

        if (elementIndex >= 0 && elementIndex < expressions.size()) {
            rewriter.getListRewrite(listNode, property).remove((ASTNode) expressions.get(elementIndex), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

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
        return applyRewrite(rewriter, originalCode);
    }

    public String addElseToIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        if (ifStatement.getElseStatement() == null) {
            rewriter.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, ast.newBlock(), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteElseFromIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        if (ifStatement.getElseStatement() != null) {
            rewriter.remove(ifStatement.getElseStatement(), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    // Assignment Operators
    public String replaceAssignmentOperator(CompilationUnit cu, String originalCode, Assignment assignment, Assignment.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Assignment newAssignment = ast.newAssignment();
        newAssignment.setLeftHandSide((Expression) ASTNode.copySubtree(ast, assignment.getLeftHandSide()));
        newAssignment.setRightHandSide((Expression) ASTNode.copySubtree(ast, assignment.getRightHandSide()));
        newAssignment.setOperator(newOp);
        rewriter.replace(assignment, newAssignment, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replacePrefixOperator(CompilationUnit cu, String originalCode, PrefixExpression prefix, PrefixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        PrefixExpression newPrefix = ast.newPrefixExpression();
        newPrefix.setOperand((Expression) ASTNode.copySubtree(ast, prefix.getOperand()));
        newPrefix.setOperator(newOp);
        rewriter.replace(prefix, newPrefix, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replacePostfixOperator(CompilationUnit cu, String originalCode, PostfixExpression postfix, PostfixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        PostfixExpression newPostfix = ast.newPostfixExpression();
        newPostfix.setOperand((Expression) ASTNode.copySubtree(ast, postfix.getOperand()));
        newPostfix.setOperator(newOp);
        rewriter.replace(postfix, newPostfix, null);
        return applyRewrite(rewriter, originalCode);
    }

    // Private helpers
    private String applyRewrite(ASTRewrite rewriter, String originalCode) {
        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    // Add this method to AstRewriter.java

    public String renameMethodParameter(CompilationUnit cu, String originalCode, MethodDeclaration method, int index, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<?> params = method.parameters();

        if (index >= 0 && index < params.size()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(index);
            SimpleName newNameNode = ast.newSimpleName(newName);
            rewriter.replace(param.getName(), newNameNode, null);
        }

        return applyRewrite(rewriter, originalCode);
    }

    public String setMethodReturnType(CompilationUnit cu, String originalCode, MethodDeclaration method, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Type newType;
        if ("void".equals(newTypeName)) {
            newType = ast.newPrimitiveType(PrimitiveType.VOID);
        } else {
            newType = TypeManager.createTypeNode(ast, newTypeName);
        }

        rewriter.replace(method.getReturnType2(), newType, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String addParameterToMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, String typeName, String paramName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);

        SingleVariableDeclaration newParam = ast.newSingleVariableDeclaration();
        newParam.setType(TypeManager.createTypeNode(ast, typeName));
        newParam.setName(ast.newSimpleName(paramName));

        listRewrite.insertLast(newParam, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteParameterFromMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
        List<?> params = method.parameters();

        if (index >= 0 && index < params.size()) {
            listRewrite.remove((ASTNode) params.get(index), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String setReturnExpression(CompilationUnit cu, String originalCode, ReturnStatement returnStmt, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newExpr == null) return originalCode;

        if (returnStmt.getExpression() == null) {
            rewriter.set(returnStmt, ReturnStatement.EXPRESSION_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(returnStmt.getExpression(), newExpr, null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    private void collectLeafValues(Expression expr, List<Expression> accumulator) {
        if (expr == null) return;
        boolean isContainer = false;

        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            if (cic.getType().toString().startsWith("ArrayList") && !cic.arguments().isEmpty()) {
                isContainer = true;
                collectLeafValues((Expression) cic.arguments().get(0), accumulator);
            }
        } else if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            String name = mi.getName().getIdentifier();
            if (name.equals("asList") || name.equals("of")) {
                isContainer = true;
                for (Object arg : mi.arguments()) collectLeafValues((Expression) arg, accumulator);
            }
        } else if (expr instanceof ArrayInitializer) {
            isContainer = true;
            for (Object e : ((ArrayInitializer) expr).expressions()) collectLeafValues((Expression) e, accumulator);
        } else if (expr instanceof ArrayCreation) {
            isContainer = true;
            if (((ArrayCreation) expr).getInitializer() != null) collectLeafValues(((ArrayCreation) expr).getInitializer(), accumulator);
        }

        if (!isContainer) accumulator.add(expr);
    }
}