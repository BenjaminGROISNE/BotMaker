package com.botmaker.ui.builders;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.ExpressionBlock;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeInfo;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoopLayoutBuilder {
    private String keyword;
    private ExpressionBlock condition;
    private BodyBlock body;
    private CompletionContext context;
    private Runnable onDelete;
    private Runnable onConditionChange;
    private String conditionType = "boolean";

    // For do-while
    private String footerKeyword;

    public LoopLayoutBuilder withKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public LoopLayoutBuilder withCondition(
            ExpressionBlock condition,
            CompletionContext context,
            String targetType) {
        this.condition = condition;
        this.context = context;
        this.conditionType = targetType;
        return this;
    }

    public LoopLayoutBuilder withBody(BodyBlock body, CompletionContext context) {
        this.body = body;
        this.context = context;
        return this;
    }

    public LoopLayoutBuilder withFooterKeyword(String keyword) {
        this.footerKeyword = keyword;
        return this;
    }

    public LoopLayoutBuilder withDeleteButton(Runnable onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public LoopLayoutBuilder withConditionChangeHandler(Runnable onChange) {
        this.onConditionChange = onChange;
        return this;
    }

    public VBox build() {
        VBox container = new VBox(5);

        // Header: keyword + condition (for while/for)
        if (keyword != null && footerKeyword == null) {
            HeaderLayoutBuilder headerBuilder = BlockLayout.header()
                    .withKeyword(keyword)
                    .withExpressionSlot(condition, context, conditionType);

            if (onConditionChange != null) {
                headerBuilder.withAddButton(onConditionChange);
            }

            if (onDelete != null) {
                headerBuilder.withDeleteButton(onDelete);
            }

            container.getChildren().add(headerBuilder.build());
        }
        // Header for do-while (just keyword)
        else if (keyword != null) {
            container.getChildren().add(
                    BlockLayout.header()
                            .withKeyword(keyword)
                            .withDeleteButton(onDelete)
                            .build()
            );
        }

        // Body
        if (body != null) {
            VBox bodyContainer = new VBox();
            bodyContainer.setPadding(new Insets(5, 0, 0, 20));
            bodyContainer.getChildren().add(body.getUINode(context));
            container.getChildren().add(bodyContainer);
        }

        // Footer (for do-while)
        if (footerKeyword != null) {
            HBox footer = BlockLayout.sentence()
                    .addKeyword(footerKeyword)
                    .addExpressionSlot(condition, context, TypeInfo.from(conditionType))
                    .build();
            container.getChildren().add(footer);
        }

        return container;
    }
}