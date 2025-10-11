package com.botmaker;

import javafx.scene.Node;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.core.dom.CompilationUnit;

public interface CodeBlock {
    String getId();
    ASTNode getAstNode();
    Node getUINode(CompletionContext context);
    void highlight();
    void unhighlight();
    int getBreakpointLine(CompilationUnit cu);
    CodeBlock getHighlightTarget();
}