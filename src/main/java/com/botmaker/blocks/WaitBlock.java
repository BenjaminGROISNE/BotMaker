package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.Statement;

public class WaitBlock extends AbstractStatementBlock {

    private ExpressionBlock duration;

    public WaitBlock(String id, Statement astNode) {
        super(id, astNode);
    }

    public ExpressionBlock getDuration() {
        return duration;
    }

    public void setDuration(ExpressionBlock duration) {
        this.duration = duration;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("wait-block");

        Label waitLabel = new Label("Wait");
        waitLabel.getStyleClass().add("keyword-label");

        container.getChildren().add(waitLabel);

        if (duration != null) {
            container.getChildren().add(duration.getUINode(context));
        } else {
            container.getChildren().add(createExpressionDropZone(context));
        }

        Label msLabel = new Label("ms");
        msLabel.getStyleClass().add("keyword-label");
        container.getChildren().add(msLabel);

        // Spacer and Delete Button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    @Override
    public String getDetails() {
        return "Wait: " + (duration != null ? duration.getDetails() : "...");
    }
}