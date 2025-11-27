package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.DoStatement;

import java.util.ArrayList;
import java.util.List;

public class DoWhileBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock condition;
    private BodyBlock body;

    public DoWhileBlock(String id, DoStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
    }

    public void setCondition(ExpressionBlock condition) { this.condition = condition; }
    public void setBody(BodyBlock body) { this.body = body; }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (body != null) children.add(body);
        if (condition != null) children.add(condition);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("do-while-block");

        // 1. Header: "do" + Delete Button
        mainContainer.getChildren().add(createStandardHeader(context, createKeywordLabel("do")));

        // 2. Body
        VBox bodyNode = createIndentedBody(body, context, "do-while-body");
        if (bodyNode != null) mainContainer.getChildren().add(bodyNode);

        // 3. Footer: "while [condition]"
        // Note: Do-While footer doesn't typically have a delete button itself, as it's part of the block
        HBox whileCondition = createSentence(
                createKeywordLabel("while"),
                condition != null ? condition.getUINode(context) : createExpressionDropZone(context)
        );
        whileCondition.getStyleClass().add("do-while-condition");

        mainContainer.getChildren().add(whileCondition);

        return mainContainer;
    }
}