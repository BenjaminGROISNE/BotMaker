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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.ArrayList;
import java.util.List;

public class WhileBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock condition;
    private BodyBlock body;
    private final BlockDragAndDropManager dragAndDropManager;

    public WhileBlock(String id, WhileStatement astNode, BlockDragAndDropManager dragAndDropManager) {
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
        if (condition != null) children.add(condition);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("while-block");

        // Header row with delete button
        HBox headerRow = new HBox(5);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // While condition part
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("while-header");

        Label whileLabel = new Label("while");
        whileLabel.getStyleClass().add("keyword-label");
        header.getChildren().add(whileLabel);

        if (condition != null) {
            header.getChildren().add(condition.getUINode(context));
        }

        // + Button for changing condition
        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");
        addButton.setOnAction(e -> showExpressionMenu(addButton, context));
        header.getChildren().add(addButton);

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
            bodyContainer.getStyleClass().add("while-body");
            bodyContainer.setPadding(new Insets(5, 0, 0, 20));
            bodyContainer.getChildren().add(body.getUINode(context));
            mainContainer.getChildren().add(bodyContainer);
        }

        return mainContainer;
    }

    private void showExpressionMenu(Button button, CompletionContext context) {
        ContextMenu menu = new ContextMenu();

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.values()) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                if (condition != null) {
                    org.eclipse.jdt.core.dom.Expression toReplace = (org.eclipse.jdt.core.dom.Expression) condition.getAstNode();
                    context.codeEditor().replaceExpression(toReplace, type);
                }
            });
            menu.getItems().add(menuItem);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}