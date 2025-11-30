package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.builders.BlockLayout;
import javafx.scene.Node;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import java.util.ArrayList;
import java.util.List;

public class ForBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock variable;
    private ExpressionBlock collection;
    private BodyBlock body;

    public ForBlock(String id, EnhancedForStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
    }

    public void setVariable(ExpressionBlock variable) { this.variable = variable; }
    public void setCollection(ExpressionBlock collection) { this.collection = collection; }
    public void setBody(BodyBlock body) { this.body = body; }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (variable != null) children.add(variable);
        if (collection != null) children.add(collection);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        // Build sentence: "for each [var] in [collection]"
        var sentence = BlockLayout.sentence()
                .addKeyword("for each")
                .addExpressionSlot(variable, context, "any")
                .addKeyword("in")
                .addExpressionSlot(collection, context, "any")
                .build();

        sentence.getStyleClass().add("for-header");

        // Build full structure with header and body
        return BlockLayout.header()
                .withCustomNode(sentence)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .andBody()
                .withContent(body, context)
                .withStyleClass("for-block")
                .build();
    }
}