package com.botmaker.parser.handlers;

import com.botmaker.parser.NodeCreator;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

/**
 * Handles operations on lists and arrays.
 */
public class ListManipulationHandler {

    private final NodeCreator nodeCreator;

    public ListManipulationHandler(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    /**
     * Adds an element to a list structure at the specified index.
     */
    public String addElementToList(CompilationUnit cu, String originalCode,
                                   ASTNode listNode, AddableExpression type, int insertIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newElement = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newElement == null) return originalCode;

        if (listNode instanceof ArrayInitializer) {
            rewriter.getListRewrite(listNode, ArrayInitializer.EXPRESSIONS_PROPERTY)
                    .insertAt(newElement, insertIndex, null);
        } else if (listNode instanceof MethodInvocation) {
            rewriter.getListRewrite(listNode, MethodInvocation.ARGUMENTS_PROPERTY)
                    .insertAt(newElement, insertIndex, null);
        } else if (listNode instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) listNode;
            if (!cic.arguments().isEmpty() && cic.arguments().get(0) instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) cic.arguments().get(0);
                rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY)
                        .insertAt(newElement, insertIndex, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes an element from a list structure at the specified index.
     */
    public String deleteElementFromList(CompilationUnit cu, String originalCode,
                                        ASTNode listNode, int elementIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        List<?> expressions;
        ChildListPropertyDescriptor property;
        ASTNode targetNode = listNode;

        if (listNode instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) listNode;
            if (!cic.arguments().isEmpty() && cic.arguments().get(0) instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) cic.arguments().get(0);
                targetNode = mi;
                expressions = mi.arguments();
                property = MethodInvocation.ARGUMENTS_PROPERTY;
            } else {
                return originalCode;
            }
        } else if (listNode instanceof ArrayInitializer) {
            expressions = ((ArrayInitializer) listNode).expressions();
            property = ArrayInitializer.EXPRESSIONS_PROPERTY;
        } else if (listNode instanceof MethodInvocation) {
            expressions = ((MethodInvocation) listNode).arguments();
            property = MethodInvocation.ARGUMENTS_PROPERTY;
        } else {
            return originalCode;
        }

        if (elementIndex >= 0 && elementIndex < expressions.size()) {
            rewriter.getListRewrite(targetNode, property)
                    .remove((ASTNode) expressions.get(elementIndex), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }
}