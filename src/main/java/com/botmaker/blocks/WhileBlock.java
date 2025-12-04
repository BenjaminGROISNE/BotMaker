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
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
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
        VBox container = new VBox(5);

        // 1. Create the Add/Change Button
        // We do this manually to capture the event source (the button itself)
        Button changeBtn = createAddButton(e ->
                showExpressionMenuAndReplace(
                        (Button) e.getSource(),
                        context,
                        "boolean",
                        condition != null ? (Expression) condition.getAstNode() : null
                )
        );

        // 2. Build Header: "while [condition] [+]"
        Node headerContent = BlockLayout.sentence()
                .addKeyword("while")
                .addExpressionSlot(condition, context, "boolean")
                .addNode(changeBtn)
                .build();

        container.getChildren().add(BlockLayout.header()
                .withCustomNode(headerContent)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((Statement) this.astNode))
                .build());

        // 3. Body
        container.getChildren().add(createIndentedBody(body, context, "loop-body"));

        return container;
    }
}