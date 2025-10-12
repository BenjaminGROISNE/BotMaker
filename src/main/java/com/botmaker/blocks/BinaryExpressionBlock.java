package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;

public class BinaryExpressionBlock extends AbstractExpressionBlock {

    private ExpressionBlock leftOperand;
    private ExpressionBlock rightOperand;
    private final String operator;
    private final ITypeBinding returnType;

    public BinaryExpressionBlock(String id, InfixExpression astNode) {
        super(id, astNode);
        this.operator = astNode.getOperator().toString();
        this.returnType = astNode.resolveTypeBinding();
    }

    public ExpressionBlock getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(ExpressionBlock leftOperand) {
        this.leftOperand = leftOperand;
    }

    public ExpressionBlock getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(ExpressionBlock rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String getOperator() {
        return operator;
    }

    public ITypeBinding getReturnType() {
        return returnType;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: #f9f0ff; -fx-border-color: #d3adf7; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");

        HBox expressionBox = new HBox(5);
        expressionBox.setAlignment(Pos.CENTER_LEFT);
        if (leftOperand != null) {
            expressionBox.getChildren().add(leftOperand.getUINode(context));
        }
        expressionBox.getChildren().add(new Label(operator));
        if (rightOperand != null) {
            expressionBox.getChildren().add(rightOperand.getUINode(context));
        }

        container.getChildren().add(expressionBox);

        String typeName = "unknown";
        if (returnType != null) {
            typeName = returnType.getName();
        }
        Label typeLabel = new Label("-> " + typeName);
        typeLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        container.getChildren().add(typeLabel);


        return container;
    }
}
