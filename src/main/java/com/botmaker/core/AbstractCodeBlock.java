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
    public Node getUINode(CompletionContext context) {
        if (uiNode == null) {
            Node originalUINode = createUINode(context);

            if (this instanceof StatementBlock) {
                Button deleteButton = new Button("X");
                deleteButton.setOnAction(e -> {
                    context.codeEditor().deleteStatement((Statement) this.astNode);
                });

                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox wrapper = new HBox(originalUINode, spacer, deleteButton);
                wrapper.setAlignment(Pos.CENTER_LEFT);

                // Transfer style from the original node to the wrapper
                String style = originalUINode.getStyle();
                if (style != null && !style.isEmpty()) {
                    wrapper.setStyle(style);
                    originalUINode.setStyle("");
                }
                this.uiNode = wrapper;
            } else {
                this.uiNode = originalUINode;
            }
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
