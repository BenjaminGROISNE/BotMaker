package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.eclipse.jdt.core.dom.BooleanLiteral;

/**
 * Block for true/false values with improved UI (Toggle Switch style)
 */
public class BooleanLiteralBlock extends AbstractExpressionBlock {

    private boolean value;

    public BooleanLiteralBlock(String id, BooleanLiteral astNode) {
        super(id, astNode);
        this.value = astNode.booleanValue();
    }

    public boolean getValue() {
        return value;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        StackPane root = new StackPane();
        root.getStyleClass().add("boolean-literal-block");

        // Define colors
        String trueColor = "#2ecc71"; // Emerald Green
        String falseColor = "#e74c3c"; // Alizarin Red
        String currentColor = value ? trueColor : falseColor;

        // 1. The invisible functional dropdown
        ComboBox<String> booleanSelector = new ComboBox<>();
        booleanSelector.getItems().addAll("true", "false");
        booleanSelector.setValue(value ? "true" : "false");

        // Make the combo box fill the area but be invisible
        // We use opacity 0 so the user can still click it, but sees the label behind it
        booleanSelector.setStyle("-fx-opacity: 0; -fx-cursor: hand;");
        booleanSelector.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 2. The visible label (pill shape)
        Label displayLabel = new Label(value ? "TRUE" : "FALSE");
        displayLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-family: 'Segoe UI', sans-serif;" +
                        "-fx-padding: 3 10 3 10;" +
                        "-fx-background-color: " + currentColor + ";" +
                        "-fx-background-radius: 12;" // Pill shape
        );

        // Center the label
        StackPane.setAlignment(displayLabel, Pos.CENTER);

        // Handle value change
        booleanSelector.setOnAction(e -> {
            String selected = booleanSelector.getValue();
            boolean newValue = "true".equals(selected);

            if (newValue != value) {
                this.value = newValue;
                // Update logic
                context.codeEditor().replaceLiteralValue(
                        (org.eclipse.jdt.core.dom.Expression) this.astNode,
                        String.valueOf(newValue)
                );
                // Immediate UI feedback (before full rebuild)
                displayLabel.setText(newValue ? "TRUE" : "FALSE");
                displayLabel.setStyle(displayLabel.getStyle().replace(currentColor, newValue ? trueColor : falseColor));
            }
        });

        root.getChildren().addAll(displayLabel, booleanSelector);

        // Force specific size to look neat
        root.setMinWidth(60);
        root.setMaxHeight(24);

        return root;
    }

    @Override
    public String getDetails() {
        return "Boolean: " + value;
    }
}