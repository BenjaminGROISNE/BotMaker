package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

public abstract class AbstractCodeBlock implements CodeBlock {
    protected final String id;
    protected final ASTNode astNode;
    protected Node uiNode; // A cached reference to the UI node
    private javafx.scene.control.Tooltip errorTooltip;

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
    public Node getUINode(CompletionContext context) {
        if (uiNode == null) {
            this.uiNode = createUINode(context);
        }
        return uiNode;
    }

    @Override
    public Node getUINode() {
        return uiNode;
    }


    @Override
    public void highlight() {
        if (uiNode != null) {
            if (!uiNode.getStyleClass().contains("highlighted")) {
                uiNode.getStyleClass().add("highlighted");
            }
        }
    }

    @Override
    public void unhighlight() {
        if (uiNode != null) {
            uiNode.getStyleClass().remove("highlighted");
        }
    }

    @Override
    public void setError(String message) {
        if (uiNode != null) {
            if (!uiNode.getStyleClass().contains("error-block")) {
                uiNode.getStyleClass().add("error-block");
            }
            if (errorTooltip == null) {
                errorTooltip = new javafx.scene.control.Tooltip(message);
                javafx.scene.control.Tooltip.install(uiNode, errorTooltip);
            } else {
                errorTooltip.setText(message);
            }
        }
    }

    @Override
    public void clearError() {
        if (uiNode != null) {
            uiNode.getStyleClass().remove("error-block");
            if (errorTooltip != null) {
                javafx.scene.control.Tooltip.uninstall(uiNode, errorTooltip);
                errorTooltip = null;
            }
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

    @Override
    public String getDetails() {
        return this.getClass().getSimpleName() + " (ID: " + this.getId() + ")";
    }

    /**
     * Creates a standard placeholder for a missing expression, which acts as a drop target.
     * @param context The completion context containing the drag-and-drop manager.
     * @return A Node representing the drop zone.
     */
    protected Node createExpressionDropZone(CompletionContext context) {
        Region dropZone = new Region();
        context.dragAndDropManager().addExpressionDropHandlers(dropZone);
        return dropZone;
    }

    // Abstract method for subclasses to implement their specific UI creation logic.
    protected abstract Node createUINode(CompletionContext context);
}
