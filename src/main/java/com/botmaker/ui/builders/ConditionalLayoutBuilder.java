package com.botmaker.ui.builders;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ConditionalLayoutBuilder {
    private ExpressionBlock condition;
    private BodyBlock thenBody;
    private Node elseNode; // Could be another IfBlock or BodyBlock
    private CompletionContext context;
    private Runnable onDelete;
    private Runnable onAddElse;
    private Runnable onConditionChange;

    public ConditionalLayoutBuilder withCondition(
            ExpressionBlock condition,
            CompletionContext context) {
        this.condition = condition;
        this.context = context;
        return this;
    }

    public ConditionalLayoutBuilder withThenBody(BodyBlock body) {
        this.thenBody = body;
        return this;
    }

    public ConditionalLayoutBuilder withElse(Node elseNode) {
        this.elseNode = elseNode;
        return this;
    }

    public ConditionalLayoutBuilder withAddElseButton(Runnable onAddElse) {
        this.onAddElse = onAddElse;
        return this;
    }

    public ConditionalLayoutBuilder withDeleteButton(Runnable onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public ConditionalLayoutBuilder withConditionChangeHandler(Runnable onChange) {
        this.onConditionChange = onChange;
        return this;
    }

    public VBox build() {
        VBox container = new VBox(5);
        container.getStyleClass().add("if-block");

        // Header
        HeaderLayoutBuilder headerBuilder = BlockLayout.header()
                .withKeyword("If")
                .withExpressionSlot(condition, context, "boolean");

        if (onConditionChange != null) {
            headerBuilder.withAddButton(onConditionChange);
        }

        if (onDelete != null) {
            headerBuilder.withDeleteButton(onDelete);
        }

        container.getChildren().add(headerBuilder.build());

        // Then body
        if (thenBody != null) {
            VBox bodyContainer = new VBox();
            bodyContainer.setPadding(new Insets(5, 0, 0, 20));
            bodyContainer.getChildren().add(thenBody.getUINode(context));
            container.getChildren().add(bodyContainer);
        }

        // Else
        if (elseNode != null) {
            container.getChildren().add(elseNode);
        } else if (onAddElse != null) {
            Button addElseBtn = new Button("+");
            addElseBtn.getStyleClass().add("expression-add-button");
            addElseBtn.setOnAction(e -> onAddElse.run());
            container.getChildren().add(addElseBtn);
        }

        return container;
    }
}