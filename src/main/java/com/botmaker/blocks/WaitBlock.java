package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import javafx.scene.Node;
import org.eclipse.jdt.core.dom.Statement;

public class WaitBlock extends AbstractStatementBlock {

    private ExpressionBlock duration;

    public WaitBlock(String id, Statement astNode) {
        super(id, astNode);
    }

    public void setDuration(ExpressionBlock duration) {
        this.duration = duration;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        var sentence = BlockLayout.sentence()
                .addKeyword("Wait")
                .addExpressionSlot(duration, context, "number")
                .addKeyword("ms")
                .build();

        return BlockLayout.header()
                .withCustomNode(sentence)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((Statement) this.astNode))
                .build();
    }

    @Override
    public String getDetails() {
        return "Wait: " + (duration != null ? duration.getDetails() : "...");
    }
}