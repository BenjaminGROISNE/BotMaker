package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class AbstractCodeBlock implements CodeBlock {
    protected final String id;
    protected final ASTNode astNode;
    protected Node uiNode;
    private javafx.scene.control.Tooltip errorTooltip;

    protected boolean isBreakpoint = false;
    private static final String BREAKPOINT_STYLE_CLASS = "breakpoint-enabled";

    public AbstractCodeBlock(String id, ASTNode astNode) {
        this.id = id;
        this.astNode = astNode;
    }

    @Override
    public String getId() { return id; }

    @Override
    public ASTNode getAstNode() { return astNode; }

    @Override
    public Node getUINode(CompletionContext context) {
        if (uiNode == null) {
            this.uiNode = createUINode(context);
            setupBreakpointInteraction();

            // --- FIX: Add discovery tooltip ---
            Tooltip tip = new Tooltip("Right-click to toggle breakpoint");
            Tooltip.install(uiNode, tip);
        }
        updateBreakpointVisuals();
        return uiNode;
    }

    private void setupBreakpointInteraction() {
        if (uiNode == null) return;

        uiNode.setOnContextMenuRequested(e -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem toggleBpItem = new MenuItem(isBreakpoint ? "Remove Breakpoint 🔴" : "Add Breakpoint ⚪");
            toggleBpItem.setOnAction(ev -> toggleBreakpoint());
            contextMenu.getItems().add(toggleBpItem);
            contextMenu.show(uiNode, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    @Override
    public Node getUINode() { return uiNode; }

    @Override
    public void highlight() {
        if (uiNode != null && !uiNode.getStyleClass().contains("highlighted")) {
            uiNode.getStyleClass().add("highlighted");
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
                errorTooltip = new Tooltip(message);
                Tooltip.install(uiNode, errorTooltip);
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
                Tooltip.uninstall(uiNode, errorTooltip);
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
    public CodeBlock getHighlightTarget() { return this; }

    @Override
    public String getDetails() {
        return this.getClass().getSimpleName() + " (ID: " + this.getId() + ")";
    }

    @Override
    public boolean isBreakpoint() { return isBreakpoint; }

    @Override
    public void setBreakpoint(boolean enabled) {
        this.isBreakpoint = enabled;
        updateBreakpointVisuals();
    }

    @Override
    public void toggleBreakpoint() {
        setBreakpoint(!isBreakpoint);
        if (uiNode != null) {
            uiNode.fireEvent(new BlockEvent.BreakpointToggleEvent(this, isBreakpoint));
        }
    }

    private void updateBreakpointVisuals() {
        if (uiNode != null) {
            if (isBreakpoint) {
                if (!uiNode.getStyleClass().contains(BREAKPOINT_STYLE_CLASS)) {
                    uiNode.getStyleClass().add(BREAKPOINT_STYLE_CLASS);
                }
                // --- FIX: Force style immediately ---
                uiNode.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-style: solid; -fx-effect: dropshadow(three-pass-box, red, 5, 0, 0, 0);");
            } else {
                uiNode.getStyleClass().remove(BREAKPOINT_STYLE_CLASS);
                // --- FIX: Clear specific breakpoint style ---
                uiNode.setStyle("");
            }
        }
    }

    protected Node createExpressionDropZone(CompletionContext context) {
        Region dropZone = new Region();
        context.dragAndDropManager().addExpressionDropHandlers(dropZone);
        return dropZone;
    }

    protected abstract Node createUINode(CompletionContext context);
}