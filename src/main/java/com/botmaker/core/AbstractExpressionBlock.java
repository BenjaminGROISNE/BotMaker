package com.botmaker.core;

import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.SelectorComponents;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.ASTNode;

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
}