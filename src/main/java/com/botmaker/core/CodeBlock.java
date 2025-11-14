package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.core.dom.CompilationUnit;

public interface CodeBlock {
    String getId();
    ASTNode getAstNode();
    Node getUINode(CompletionContext context);
    Node getUINode();
    void highlight();
    void unhighlight();
    int getBreakpointLine(CompilationUnit cu);
    CodeBlock getHighlightTarget();
    String getDetails();
    void setError(String message);
    void clearError();
}
