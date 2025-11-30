package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.theme.StyleBuilder;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class ReturnBlock extends AbstractStatementBlock {

    private ExpressionBlock expression;

    public ReturnBlock(String id, ReturnStatement astNode) {
        super(id, astNode);
    }

    public void setExpression(ExpressionBlock expression) {
        this.expression = expression;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        String methodReturnType = findParentMethodReturnType();
        boolean isVoid = "void".equals(methodReturnType);

        var sentenceBuilder = BlockLayout.sentence().addKeyword("return");

        if (expression != null) {
            sentenceBuilder
                    .addNode(expression.getUINode(context))
                    .addNode(BlockUIComponents.createChangeButton(e ->
                            showExpressionMenuAndReplace((Button)e.getSource(), context, methodReturnType,
                                    (org.eclipse.jdt.core.dom.Expression)expression.getAstNode())
                    ));
        } else if (!isVoid) {
            sentenceBuilder.addNode(createAddButton(e ->
                    BlockUIComponents.createExpressionTypeMenu(methodReturnType, type ->
                            context.codeEditor().setReturnExpression((ReturnStatement) this.astNode, type)
                    ).show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0)
            ));
        } else {
            Label voidLabel = new Label("(void)");
            StyleBuilder.create()
                    .textColor("#aaa")
                    .fontSize(10)
                    .build();
            sentenceBuilder.addNode(voidLabel);
        }

        return BlockLayout.header()
                .withCustomNode(sentenceBuilder.build())
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build();
    }

    private String findParentMethodReturnType() {
        ASTNode current = this.astNode.getParent();
        while (current != null) {
            if (current instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) current;
                if (md.getReturnType2() != null) {
                    return md.getReturnType2().toString();
                }
                return "void";
            }
            current = current.getParent();
        }
        return "void";
    }
}