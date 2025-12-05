package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.LayoutComponents;
import com.botmaker.ui.components.PlaceholderComponents;
import com.botmaker.util.TypeInfo;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import java.util.function.Consumer;

public abstract class AbstractStatementBlock extends AbstractCodeBlock implements StatementBlock {
    public AbstractStatementBlock(String id, ASTNode astNode) {
        super(id, astNode);
    }

    // --- HELPER METHODS ---

    protected Button createDeleteButton(CompletionContext context) {
        return BlockUIComponents.createDeleteButton(() ->
                context.codeEditor().deleteStatement((Statement) this.astNode)
        );
    }

    protected Label createKeywordLabel(String text) {
        return BlockUIComponents.createKeywordLabel(text);
    }

    protected HBox createStandardHeader(CompletionContext context, Node... content) {
        HBox container = BlockUIComponents.createHeaderRow(
                () -> context.codeEditor().deleteStatement((Statement) this.astNode),
                content
        );
        // Apply default styles that might be overridden by specific blocks
        container.getStyleClass().add("statement-block-header");
        return container;
    }

    protected Button createAddButton(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        return BlockUIComponents.createAddButton(handler);
    }

    /**
     * Creates an indented body VBox for nested statements.
     */
    protected javafx.scene.layout.VBox createIndentedBody(com.botmaker.core.BodyBlock body, CompletionContext context, String styleClass) {
        Node bodyNode = (body != null) ? body.getUINode(context) : null;
        return LayoutComponents.createIndentedBody(bodyNode, styleClass);
    }

    /**
     * Helper to render an expression or a drop zone if null.
     */
    protected Node getOrDropZone(com.botmaker.core.ExpressionBlock expr, CompletionContext context) {
        return PlaceholderComponents.createExpressionOrDropZone(
                expr,
                context,
                () -> createExpressionDropZone(context) // Defined in AbstractCodeBlock
        );
    }

    /**
     * Helper to create a sentence row (e.g. for loops).
     */
    protected javafx.scene.layout.HBox createSentence(Node... nodes) {
        return LayoutComponents.createSentenceRow(nodes);
    }

    /**
     * TypeInfo overload for showExpressionMenuAndReplace
     */
    protected void showExpressionMenuAndReplace(javafx.scene.control.Button button,
                                                CompletionContext context,
                                                TypeInfo expectedType,
                                               Expression toReplace) {
        ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(
                expectedType,
                type -> {
                    if (toReplace != null) {
                        context.codeEditor().replaceExpression(toReplace, type);
                    }
                }
        );
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}