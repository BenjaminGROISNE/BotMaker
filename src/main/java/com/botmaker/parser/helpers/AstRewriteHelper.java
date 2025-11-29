package com.botmaker.parser.helpers;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Common utilities for AST rewriting operations.
 */
public class AstRewriteHelper {

    /**
     * Applies an ASTRewrite to source code and returns the modified code.
     * @param rewriter The ASTRewrite to apply
     * @param originalCode The original source code
     * @return The modified code, or original code if rewrite fails
     */
    public static String applyRewrite(ASTRewrite rewriter, String originalCode) {
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

    /**
     * Replaces one AST node with another.
     * @param rewriter The ASTRewrite instance
     * @param oldNode The node to replace
     * @param newNode The replacement node
     */
    public static void replaceNode(ASTRewrite rewriter, ASTNode oldNode, ASTNode newNode) {
        rewriter.replace(oldNode, newNode, null);
    }

    /**
     * Removes an AST node.
     * @param rewriter The ASTRewrite instance
     * @param node The node to remove
     */
    public static void removeNode(ASTRewrite rewriter, ASTNode node) {
        rewriter.remove(node, null);
    }

    /**
     * Sets a property on an AST node.
     * @param rewriter The ASTRewrite instance
     * @param node The node to modify
     * @param property The property to set
     * @param value The new value
     */
    public static void setProperty(ASTRewrite rewriter, ASTNode node,
                                   org.eclipse.jdt.core.dom.ChildPropertyDescriptor property,
                                   Object value) {
        rewriter.set(node, property, value, null);
    }
}