package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class IncrementDecrementBlock extends AbstractStatementBlock {

    private ExpressionBlock operand;
    private final String operator;
    private final boolean isPrefix;

    public IncrementDecrementBlock(String id, ExpressionStatement astNode, String operator, boolean isPrefix) {
        super(id, astNode);
        this.operator = operator;
        this.isPrefix = isPrefix;
    }

    public ExpressionBlock getOperand() {
        return operand;
    }

    public void setOperand(ExpressionBlock operand) {
        this.operand = operand;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("increment-decrement-block");

        if (isPrefix) {
            Label operatorLabel = new Label(operator);
            operatorLabel.getStyleClass().add("operator-label");
            container.getChildren().add(operatorLabel);

            if (operand != null) {
                container.getChildren().add(operand.getUINode(context));
            }
        } else {
            if (operand != null) {
                container.getChildren().add(operand.getUINode(context));
            }

            Label operatorLabel = new Label(operator);
            operatorLabel.getStyleClass().add("operator-label");
            container.getChildren().add(operatorLabel);
        }

        // Add spacer and delete button
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }
}