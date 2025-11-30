package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class LiteralBlock<T> extends AbstractExpressionBlock {
    private final T value;

    public LiteralBlock(String id, Expression astNode, T value) {
        super(id, astNode);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    // LiteralBlock.java - already well structured, keep as is
    @Override
    protected Node createUINode(CompletionContext context) {
        String initialText = (value instanceof String) ? (String) value : String.valueOf(value);
        TextField textField = new TextField(initialText);

        if (initialText.isEmpty() && value instanceof String) {
            textField.setPromptText("Type a value...");
        }

        textField.setCursor(Cursor.TEXT);

        UnaryOperator<TextFormatter.Change> filter = createInputFilter();
        if (filter != null) {
            textField.setTextFormatter(new TextFormatter<>(filter));
        }

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String newText = textField.getText();
                String oldText = (value instanceof String) ? (String) value : String.valueOf(value);

                if (isNumberType() && !isValidFinalNumber(newText)) {
                    textField.setText(oldText);
                    return;
                }

                String textToSave = normalizeNumberSuffix(newText);

                if (!textToSave.equals(newText)) {
                    textField.setText(textToSave);
                }

                if (!textToSave.equals(oldText)) {
                    if (this.astNode instanceof MethodInvocation) {
                        MethodInvocation mi = (MethodInvocation) this.astNode;
                        context.codeEditor().addStringArgumentToMethodInvocation(mi, textToSave);
                    } else {
                        context.codeEditor().replaceLiteralValue((Expression) this.astNode, textToSave);
                    }
                }
            }
        });

        HBox container = new HBox(textField);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return container;
    }

    /**
     * Automatically appends required Java suffixes (f, L) if missing.
     */
    private String normalizeNumberSuffix(String text) {
        if (value instanceof Float) {
            if (!text.toLowerCase().endsWith("f")) {
                return text + "f";
            }
        } else if (value instanceof Long) {
            if (!text.toLowerCase().endsWith("l")) {
                return text + "L"; // Use Uppercase L to avoid confusion with 1
            }
        }
        return text;
    }

    /**
     * Creates a filter based on the type of the value stored in this block.
     */
    private UnaryOperator<TextFormatter.Change> createInputFilter() {
        return change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) return change;

            // 1. INTEGER Types (Byte, Short, Integer, Long)
            if (value instanceof Integer || value instanceof Short || value instanceof Byte || value instanceof Long) {
                // Allow digits, minus, and L suffix for Longs
                String regex = (value instanceof Long) ? "-?[0-9]*[lL]?" : "-?[0-9]*";

                if (newText.matches(regex)) {
                    if (isWithinRange(newText, value.getClass())) {
                        return change;
                    }
                }
                return null;
            }

            // 2. FLOATING POINT Types (Float, Double)
            else if (value instanceof Double || value instanceof Float) {
                // Allow digits, dot, minus, and f/d suffixes
                if (newText.matches("-?[0-9]*\\.?[0-9]*[dDfF]?")) {
                    if (isWithinRange(newText, value.getClass())) {
                        return change;
                    }
                }
                return null;
            }

            // 3. STRING
            else if (value instanceof String) {
                String input = change.getText();
                if (input.contains("\"") || input.contains("\n") || input.contains("\r")) {
                    return null;
                }
                return change;
            }

            return change;
        };
    }

    /**
     * Checks if the text value fits within the MIN/MAX bounds of the target type.
     */
    private boolean isWithinRange(String text, Class<?> type) {
        // Allow intermediate states
        if (text.equals("-") || text.equals(".") || text.equals("-.")) return true;

        // Strip suffixes for parsing check
        String cleanText = text.replaceAll("[dDfFlL]$", "");
        if (cleanText.isEmpty()) return true;

        try {
            if (type == Byte.class) {
                long val = Long.parseLong(cleanText);
                return val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE;
            }
            if (type == Short.class) {
                long val = Long.parseLong(cleanText);
                return val >= Short.MIN_VALUE && val <= Short.MAX_VALUE;
            }
            if (type == Integer.class) {
                long val = Long.parseLong(cleanText);
                return val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE;
            }
            if (type == Long.class) {
                Long.parseLong(cleanText);
                return true;
            }
            if (type == Float.class) {
                double val = Double.parseDouble(cleanText);
                // Float.MAX_VALUE is huge, mostly we check for Infinity overflow
                return !Double.isInfinite(val);
            }
            if (type == Double.class) {
                BigDecimal val = new BigDecimal(cleanText);
                // Check if it fits in a double
                return !Double.isInfinite(val.doubleValue());
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isValidFinalNumber(String text) {
        if (text.equals("-") || text.equals(".") || text.equals("-.")) return false;
        if (text.endsWith(".")) return false;
        return true;
    }

    private boolean isNumberType() {
        return value instanceof Number;
    }
}