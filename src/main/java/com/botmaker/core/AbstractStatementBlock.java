package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

    protected void showExpressionMenuAndReplace(Button button, CompletionContext context, String targetType, Expression toReplace) {
        BlockUIComponents.createExpressionTypeMenu(targetType, type -> {
            if (toReplace != null) {
                context.codeEditor().replaceExpression(toReplace, type);
            } else if (this instanceof com.botmaker.blocks.ReturnBlock) {
                // Special case for ReturnBlock which might accept null
                // Handled by specific implementations usually, but good for generic replacement
            }
        }).show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}