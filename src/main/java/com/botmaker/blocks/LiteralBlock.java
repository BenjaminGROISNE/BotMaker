package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
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
        String initialText = (value instanceof String) ? (String) value : String.valueOf(value);
        TextField textField = new TextField(initialText);

        if (initialText.isEmpty() && value instanceof String) {
            textField.setPromptText("Type a value...");
        }
        textField.setCursor(Cursor.TEXT);

        // When user presses Enter
        textField.setOnAction(event -> {
            String newText = textField.getText();
            String replacementCode;

            if (value instanceof String) {
                // Escape quotes and wrap in quotes for the new source code
                replacementCode = "\"" + newText.replace("\"", "\\\"") + "\"";
            } else {
                // For numbers, booleans, etc., use the text as is.
                // This assumes valid input.
                replacementCode = newText;
            }

            int start = this.astNode.getStartPosition();
            int end = start + this.astNode.getLength();
            String newCode = context.sourceCode().substring(0, start) + replacementCode + context.sourceCode().substring(end);

            context.onCodeUpdate().accept(newCode);
        });

        HBox container = new HBox(textField);
        container.setStyle("-fx-background-color: #fff0f6; -fx-border-color: #ffadd2; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");
        return container;
    }
}
