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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.ForStatement;

import java.util.ArrayList;
import java.util.List;

public class ForBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock initialization;
    private ExpressionBlock condition;
    private ExpressionBlock update;
    private BodyBlock body;
    private final BlockDragAndDropManager dragAndDropManager;

    public ForBlock(String id, ForStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
        this.dragAndDropManager = dragAndDropManager;
    }

    public ExpressionBlock getInitialization() {
        return initialization;
    }

    public void setInitialization(ExpressionBlock initialization) {
        this.initialization = initialization;
    }

    public ExpressionBlock getCondition() {
        return condition;
    }

    public void setCondition(ExpressionBlock condition) {
        this.condition = condition;
    }

    public ExpressionBlock getUpdate() {
        return update;
    }

    public void setUpdate(ExpressionBlock update) {
        this.update = update;
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
        if (initialization != null) children.add(initialization);
        if (condition != null) children.add(condition);
        if (update != null) children.add(update);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("for-block");

        // Header row with delete button
        HBox headerRow = new HBox(5);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // For loop parts
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("for-header");

        Label forLabel = new Label("for");
        forLabel.getStyleClass().add("keyword-label");

        header.getChildren().add(forLabel);

        if (initialization != null) {
            header.getChildren().add(initialization.getUINode(context));
        }

        Label toLabel = new Label("to");
        header.getChildren().add(toLabel);

        if (condition != null) {
            header.getChildren().add(condition.getUINode(context));
        }

        Label stepLabel = new Label("step");
        header.getChildren().add(stepLabel);

        if (update != null) {
            header.getChildren().add(update.getUINode(context));
        }

        // Add spacer and delete button
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        headerRow.getChildren().addAll(header, spacer, deleteButton);
        mainContainer.getChildren().add(headerRow);

        // Body
        if (body != null) {
            VBox bodyContainer = new VBox();
            bodyContainer.getStyleClass().add("for-body");
            bodyContainer.setPadding(new Insets(5, 0, 0, 20));
            bodyContainer.getChildren().add(body.getUINode(context));
            mainContainer.getChildren().add(bodyContainer);
        }

        return mainContainer;
    }
}