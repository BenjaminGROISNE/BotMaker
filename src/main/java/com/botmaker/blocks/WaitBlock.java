package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
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
        Node content = createSentence(
                createKeywordLabel("Wait"),
                getOrDropZone(duration, context),
                createKeywordLabel("ms")
        );

        Node container = createStandardHeader(context, content);
        container.getStyleClass().add("wait-block");

        return container;
    }

    @Override
    public String getDetails() {
        return "Wait: " + (duration != null ? duration.getDetails() : "...");
    }
}