package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class NotOperatorBlock extends AbstractExpressionBlock {

    private ExpressionBlock operand;

    public NotOperatorBlock(String id, PrefixExpression astNode) {
        super(id, astNode);
    }

    public void setOperand(ExpressionBlock operand) {
        this.operand = operand;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        // "!" [Expression] [Change]
        var sentence = BlockLayout.sentence()
                .addLabel("!")
                .addExpressionSlot(operand, context, "boolean")
                .addNode(BlockUIComponents.createChangeButton(e ->
                        showExpressionMenuAndReplace((Button)e.getSource(), context, "boolean",
                                operand != null ? (Expression) operand.getAstNode() : null)
                ));

        Node root = sentence.build();
        root.getStyleClass().add("logic-expression-block"); // Reuse logic styling
        return root;
    }
}