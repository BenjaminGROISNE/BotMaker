package com.botmaker;

import javafx.scene.Node;
import org.eclipse.jdt.core.dom.ASTNode;

public interface CodeBlock {
    String getId();
    ASTNode getAstNode();
    Node getUINode(CompletionContext context);
}