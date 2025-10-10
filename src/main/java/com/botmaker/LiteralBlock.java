package com.botmaker;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.Expression;

public class LiteralBlock<T> extends AbstractExpressionBlock {
    private final T value;

    public LiteralBlock(String id, Expression astNode, T value) {
        super(id, astNode);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public Class<?> getValueType() {
        if (value == null) return null;
        return value.getClass();
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        String displayValue = (value instanceof String) ? "\"" + value + "\"" : String.valueOf(value);
        Text text = new Text(displayValue);
        HBox container = new HBox(text);
        container.setStyle("-fx-background-color: #fff0f6; -fx-border-color: #ffadd2; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");
        return container;
    }
}
