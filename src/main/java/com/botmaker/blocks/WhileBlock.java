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
import org.eclipse.jdt.core.dom.WhileStatement;
import java.util.ArrayList;
import java.util.List;

public class WhileBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock condition;
    private BodyBlock body;

    public WhileBlock(String id, WhileStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
    }

    public void setCondition(ExpressionBlock condition) { this.condition = condition; }
    public void setBody(BodyBlock body) { this.body = body; }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (condition != null) children.add(condition);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        return BlockLayout.loop()
                .withKeyword("while")
                .withCondition(condition, context, "boolean")
                .withConditionChangeHandler(() -> {
                    javafx.scene.control.Button btn = new javafx.scene.control.Button();
                    showExpressionMenuAndReplace(btn, context, "boolean",
                            condition != null ? (org.eclipse.jdt.core.dom.Expression) condition.getAstNode() : null);
                })
                .withBody(body, context)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build();
    }
}