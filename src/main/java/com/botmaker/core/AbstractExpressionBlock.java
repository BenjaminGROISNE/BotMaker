package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.SelectorComponents;
import com.botmaker.util.TypeInfo;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import java.util.function.Consumer;

public abstract class AbstractExpressionBlock extends AbstractCodeBlock implements ExpressionBlock {
    public AbstractExpressionBlock(String id, ASTNode astNode) {
        super(id, astNode);
    }

    // --- HELPER METHODS ---

    protected Label createKeywordLabel(String text) {
        return BlockUIComponents.createKeywordLabel(text);
    }

    protected Label createOperatorLabel(String text) {
        return BlockUIComponents.createOperatorLabel(text);
    }

    protected ComboBox<String> createOperatorSelector(String[] names, String[] symbols, String currentSymbol, Consumer<String> onSymbolChange) {
        return SelectorComponents.createOperatorSelector(names, symbols, currentSymbol, onSymbolChange);
    }

    /**
     * Legacy helper: accepts String targetType.
     * Delegates to TypeInfo version.
     */
    protected void showExpressionMenuAndReplace(Button button, CompletionContext context,
                                                String targetType, Expression toReplace) {
        showExpressionMenuAndReplace(button, context, TypeInfo.from(targetType), toReplace);
    }

    /**
     * Helper to show the expression type menu and replace the current expression node upon selection.
     */
    protected void showExpressionMenuAndReplace(Button button, CompletionContext context,
                                                TypeInfo targetType, Expression toReplace) {
        ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(targetType, type -> {
            if (toReplace != null) {
                context.codeEditor().replaceExpression(toReplace, type);
            }
        });
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}