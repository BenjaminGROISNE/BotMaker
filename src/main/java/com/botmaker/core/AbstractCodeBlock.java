package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class AbstractCodeBlock implements CodeBlock {
    protected final String id;
    protected final ASTNode astNode;

    protected Node uiNode;
    private javafx.scene.control.Tooltip errorTooltip;

    // Breakpoint State
    protected boolean isBreakpoint = false;
    private Circle breakpointCircle;

    // Constants
    private static final double GUTTER_PADDING = 12.0; // Space reserved on the left
    private static final double CIRCLE_RADIUS = 4.0;

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
            // 1. Create the standard UI
            this.uiNode = createUINode(context);

            // 2. Inject the visual Gutter (Padding + Circle)
            if (uiNode instanceof Region) {
                Region region = (Region) uiNode;

                // Add left padding to make room for the circle
                Insets existing = region.getPadding();
                region.setPadding(new Insets(
                        existing.getTop(),
                        existing.getRight(),
                        existing.getBottom(),
                        existing.getLeft() + GUTTER_PADDING
                ));

                // If the UI node allows children (Pane), add the floating circle
                if (uiNode instanceof Pane) {
                    Pane pane = (Pane) uiNode;

                    breakpointCircle = new Circle(CIRCLE_RADIUS, Color.RED);
                    breakpointCircle.setManaged(false); // Don't affect flow layout
                    breakpointCircle.setVisible(false); // Hidden by default

                    // Position: Center vertically, placed inside the left padding
                    // x = (Padding / 2) roughly centers it in the gutter
                    breakpointCircle.setLayoutX(GUTTER_PADDING / 2 + existing.getLeft());
                    breakpointCircle.centerYProperty().bind(pane.heightProperty().divide(2));

                    pane.getChildren().add(breakpointCircle);
                }
            }

            // 3. Setup Interaction
            setupBreakpointInteraction();

            // 4. Discovery Tooltip
            Tooltip tip = new Tooltip("Right-click to toggle breakpoint");
            Tooltip.install(uiNode, tip);
        }

        updateBreakpointVisuals();
        return uiNode;
    }

    private void setupBreakpointInteraction() {
        if (uiNode == null) return;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem toggleBpItem = new MenuItem("Toggle Breakpoint");
        toggleBpItem.setOnAction(ev -> toggleBreakpoint());
        contextMenu.getItems().add(toggleBpItem);

        uiNode.setOnContextMenuRequested(e -> {
            toggleBpItem.setText(isBreakpoint ? "Remove Breakpoint 🔴" : "Add Breakpoint ⚪");
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
            uiNode.fireEvent(new com.botmaker.ui.BlockEvent.BreakpointToggleEvent(this, isBreakpoint));
        }
    }

    private void updateBreakpointVisuals() {
        // Update the circle visibility
        if (breakpointCircle != null) {
            breakpointCircle.setVisible(isBreakpoint);
        }

        // Optional: Keep the red border as secondary reinforcement, or remove if circle is enough
        if (uiNode != null) {
            String style = uiNode.getStyle();
            String borderStyle = "-fx-border-color: #e74c3c; -fx-border-width: 0 0 0 2; -fx-border-style: solid;";

            if (isBreakpoint) {
                if (!style.contains("-fx-border-color: #e74c3c")) {
                    uiNode.setStyle(style + borderStyle);
                }
            } else {
                uiNode.setStyle(style.replace(borderStyle, ""));
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