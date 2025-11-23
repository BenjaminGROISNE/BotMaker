package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ContinueStatement;

public class ContinueBlock extends AbstractStatementBlock {

    public ContinueBlock(String id, ContinueStatement astNode) {
        super(id, astNode);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = createStandardHeader(context, createKeywordLabel("continue"));
        container.getStyleClass().add("continue-block");
        return container;
    }
}