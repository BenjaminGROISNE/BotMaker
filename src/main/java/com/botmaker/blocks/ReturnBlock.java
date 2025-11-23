package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import static com.botmaker.ui.components.BlockUIComponents.createChangeButton;

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
        // Determine parent method return type
        String methodReturnType = findParentMethodReturnType();
        boolean isVoid = "void".equals(methodReturnType);

        Node content;

        if (expression != null) {
            Button changeBtn = createChangeButton(e ->
                    showExpressionMenuAndReplace((Button)e.getSource(), context, methodReturnType, (org.eclipse.jdt.core.dom.Expression)expression.getAstNode())
            );

            content = createSentence(
                    createKeywordLabel("return"),
                    expression.getUINode(context),
                    changeBtn
            );
        } else if (!isVoid) {
            Button addButton = createAddButton(e ->
                    BlockUIComponents.createExpressionTypeMenu(methodReturnType, type ->
                            context.codeEditor().setReturnExpression((ReturnStatement) this.astNode, type)
                    ).show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0)
            );

            content = createSentence(
                    createKeywordLabel("return"),
                    addButton
            );
        } else {
            Label voidLabel = new Label("(void)");
            voidLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px; -fx-font-style: italic;");

            content = createSentence(
                    createKeywordLabel("return"),
                    voidLabel
            );
        }

        Node container = createStandardHeader(context, content);
        container.getStyleClass().add("return-block");

        return container;
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