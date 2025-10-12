package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class AbstractCodeBlock implements CodeBlock {
    protected final String id;
    protected final ASTNode astNode;
    protected Node uiNode; // A cached reference to the UI node
    private String originalStyle;

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

    @Override
    public void highlight() {
        if (uiNode != null) {
            originalStyle = uiNode.getStyle();
            // Make sure to handle null or empty original style
            if (originalStyle == null || originalStyle.trim().isEmpty()) {
                uiNode.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            } else {
                uiNode.setStyle(originalStyle + "; -fx-border-color: red; -fx-border-width: 2;");
            }
        }
    }

    @Override
    public void unhighlight() {
        if (uiNode != null) {
            uiNode.setStyle(originalStyle);
        }
    }

    @Override
    public int getBreakpointLine(CompilationUnit cu) {
        if (cu == null || astNode == null) return -1;
        return cu.getLineNumber(astNode.getStartPosition());
    }

    @Override
    public CodeBlock getHighlightTarget() {
        return this; // Default behavior is to highlight the block itself.
    }

    // Abstract method for subclasses to implement their specific UI creation logic.
    protected abstract Node createUINode(CompletionContext context);
}
