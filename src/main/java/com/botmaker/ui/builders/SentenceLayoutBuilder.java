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

    /**
     * TypeInfo overload for addExpressionSlot
     */
    public SentenceLayoutBuilder addExpressionSlot(com.botmaker.core.ExpressionBlock expression,
                                                   com.botmaker.lsp.CompletionContext context,
                                                   com.botmaker.util.TypeInfo expectedType) {
        if (expression != null) {
            nodes.add(expression.getUINode(context));
        } else {
            javafx.scene.control.Label placeholder = new javafx.scene.control.Label("⟨expression⟩");
            placeholder.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-style: italic;");
            nodes.add(placeholder);
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