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
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * ForEach loop block: for(Type variable : collection) { }
 */
public class ForBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock variable;
    private ExpressionBlock collection;
    private BodyBlock body;
    private final BlockDragAndDropManager dragAndDropManager;

    public ForBlock(String id, EnhancedForStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
        this.dragAndDropManager = dragAndDropManager;
    }

    public ExpressionBlock getVariable() {
        return variable;
    }

    public void setVariable(ExpressionBlock variable) {
        this.variable = variable;
    }

    public ExpressionBlock getCollection() {
        return collection;
    }

    public void setCollection(ExpressionBlock collection) {
        this.collection = collection;
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
        if (variable != null) children.add(variable);
        if (collection != null) children.add(collection);
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

        // For each parts
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("for-header");

        Label forLabel = new Label("for each");
        forLabel.getStyleClass().add("keyword-label");

        header.getChildren().add(forLabel);

        if (variable != null) {
            header.getChildren().add(variable.getUINode(context));
        }

        Label inLabel = new Label("in");
        header.getChildren().add(inLabel);

        if (collection != null) {
            header.getChildren().add(collection.getUINode(context));
        }

        // Add spacer and delete button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
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