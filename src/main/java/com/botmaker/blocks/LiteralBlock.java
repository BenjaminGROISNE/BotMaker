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

        // Update when the text field loses focus, if the value has changed.
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // newVal is false when focus is lost
            if (!newVal) {
                String newText = textField.getText();
                String oldText = (value instanceof String) ? (String) value : String.valueOf(value);

                if (!newText.equals(oldText)) {
                    // Handle the special synthetic case for an empty println
                    if (this.astNode instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
                        org.eclipse.jdt.core.dom.MethodInvocation mi = (org.eclipse.jdt.core.dom.MethodInvocation) this.astNode;
                        context.codeEditor().addStringArgumentToMethodInvocation(mi, newText);
                    } else {
                        // This is the normal case for editing an existing literal
                        context.codeEditor().replaceLiteralValue((Expression) this.astNode, newText);
                    }
                }
            }
        });

        HBox container = new HBox(textField);
        container.setStyle("-fx-background-color: #fff0f6; -fx-border-color: #ffadd2; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");
        return container;
    }
}
