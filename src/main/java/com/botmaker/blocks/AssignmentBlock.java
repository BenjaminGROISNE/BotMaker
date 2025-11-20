package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class AssignmentBlock extends AbstractStatementBlock {

    private ExpressionBlock leftHandSide;
    private ExpressionBlock rightHandSide;
    private final String operator;

    public AssignmentBlock(String id, ExpressionStatement astNode) {
        super(id, astNode);
        Assignment assignment = (Assignment) astNode.getExpression();
        this.operator = assignment.getOperator().toString();
    }

    public ExpressionBlock getLeftHandSide() {
        return leftHandSide;
    }

    public void setLeftHandSide(ExpressionBlock leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    public ExpressionBlock getRightHandSide() {
        return rightHandSide;
    }

    public void setRightHandSide(ExpressionBlock rightHandSide) {
        this.rightHandSide = rightHandSide;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("assignment-block");

        if (leftHandSide != null) {
            container.getChildren().add(leftHandSide.getUINode(context));
        }

        Label operatorLabel = new Label(operator);
        operatorLabel.getStyleClass().add("operator-label");
        container.getChildren().add(operatorLabel);

        if (rightHandSide != null) {
            container.getChildren().add(rightHandSide.getUINode(context));
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