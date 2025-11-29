package com.botmaker.ui.builders;

import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.SelectorComponents;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SentenceLayoutBuilder {
    private final List<Node> nodes = new ArrayList<>();
    private double spacing = 5.0;
    private Pos alignment = Pos.CENTER_LEFT;

    public SentenceLayoutBuilder addKeyword(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("keyword-label");
        nodes.add(label);
        return this;
    }

    public SentenceLayoutBuilder addLabel(String text) {
        nodes.add(new Label(text));
        return this;
    }

    public SentenceLayoutBuilder addNode(Node node) {
        nodes.add(node);
        return this;
    }

    public SentenceLayoutBuilder addExpressionSlot(
            ExpressionBlock expr,
            CompletionContext context,
            String targetType) {
        if (expr != null) {
            nodes.add(expr.getUINode(context));
        } else {
            nodes.add(createDropZone(context));
        }
        return this;
    }

    public SentenceLayoutBuilder addOperatorSelector(
            String[] names,
            String[] symbols,
            String current,
            Consumer<String> onChange) {
        ComboBox<String> selector = SelectorComponents.createOperatorSelector(
                names, symbols, current, onChange
        );
        nodes.add(selector);
        return this;
    }

    public SentenceLayoutBuilder spacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public HBox build() {
        HBox container = new HBox(spacing);
        container.setAlignment(alignment);
        container.getChildren().addAll(nodes);
        return container;
    }

    private Node createDropZone(CompletionContext context) {
        return DropZoneFactory.createExpressionDropZone(context);
    }
}