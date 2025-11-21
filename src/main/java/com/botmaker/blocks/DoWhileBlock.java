package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.DoStatement;

import java.util.ArrayList;
import java.util.List;

public class DoWhileBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock condition;
    private BodyBlock body;
    private final BlockDragAndDropManager dragAndDropManager;

    public DoWhileBlock(String id, DoStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
        this.dragAndDropManager = dragAndDropManager;
    }

    public ExpressionBlock getCondition() {
        return condition;
    }

    public void setCondition(ExpressionBlock condition) {
        this.condition = condition;
    }

    public BodyBlock getBody() {
        return body;
    }

    public void setBody(BodyBlock body) {
        this.body = body;
    }

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

        // Do header with delete button
        HBox doHeaderRow = new HBox(5);
        doHeaderRow.setAlignment(Pos.CENTER_LEFT);

        Label doLabel = new Label("do");
        doLabel.getStyleClass().add("keyword-label");

        // Add spacer and delete button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        doHeaderRow.getChildren().addAll(doLabel, spacer, deleteButton);
        mainContainer.getChildren().add(doHeaderRow);

        // Body
        if (body != null) {
            VBox bodyContainer = new VBox();
            bodyContainer.getStyleClass().add("do-while-body");
            bodyContainer.setPadding(new Insets(5, 0, 0, 20));
            bodyContainer.getChildren().add(body.getUINode(context));
            mainContainer.getChildren().add(bodyContainer);
        }

        // While condition
        HBox whileCondition = new HBox(5);
        whileCondition.setAlignment(Pos.CENTER_LEFT);
        whileCondition.getStyleClass().add("do-while-condition");

        Label whileLabel = new Label("while");
        whileLabel.getStyleClass().add("keyword-label");

        whileCondition.getChildren().add(whileLabel);
        if (condition != null) {
            whileCondition.getChildren().add(condition.getUINode(context));
        }

        mainContainer.getChildren().add(whileCondition);

        return mainContainer;
    }
}