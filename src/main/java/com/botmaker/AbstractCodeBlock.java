package com.botmaker;

import javafx.scene.Node;
import org.eclipse.jdt.core.dom.ASTNode;

public abstract class AbstractCodeBlock implements CodeBlock {
    protected final String id;
    protected final ASTNode astNode;
    protected Node uiNode; // A cached reference to the UI node

    public AbstractCodeBlock(String id, ASTNode astNode) {
        this.id = id;
        this.astNode = astNode;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ASTNode getAstNode() {
        return astNode;
    }

    @Override
    public Node getUINode(CompletionContext context) { // Updated signature
        if (uiNode == null) {
            uiNode = createUINode(context); // Pass context to creation method
        }
        return uiNode;
    }

    // Abstract method for subclasses to implement their specific UI creation logic.
    protected abstract Node createUINode(CompletionContext context);
}
