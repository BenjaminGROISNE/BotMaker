package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.BreakStatement;

public class BreakBlock extends AbstractStatementBlock {

    public BreakBlock(String id, BreakStatement astNode) {
        super(id, astNode);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = createStandardHeader(context, createKeywordLabel("break"));
        container.getStyleClass().add("break-block");
        return container;
    }
}