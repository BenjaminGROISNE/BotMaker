package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.theme.StyleBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Block for true/false values with toggle switch style UI
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

        // Colors
        String trueColor = "#2ecc71";  // Emerald Green
        String falseColor = "#e74c3c"; // Alizarin Red
        String currentColor = value ? trueColor : falseColor;

        // Invisible functional dropdown
        ComboBox<String> booleanSelector = new ComboBox<>();
        booleanSelector.getItems().addAll("true", "false");
        booleanSelector.setValue(value ? "true" : "false");
        booleanSelector.setStyle("-fx-opacity: 0; -fx-cursor: hand;");
        booleanSelector.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Visible pill label
        Label displayLabel = new Label(value ? "TRUE" : "FALSE");
        StyleBuilder.create()
                .textColor("white")
                .fontWeight("bold")
                .fontSize(11)
                .fontFamily("'Segoe UI', sans-serif")
                .padding(3, 10, 3, 10)
                .backgroundColor(currentColor)
                .backgroundRadius(12)
                .applyTo(displayLabel);

        StackPane.setAlignment(displayLabel, Pos.CENTER);

        // Handle value change
        booleanSelector.setOnAction(e -> {
            String selected = booleanSelector.getValue();
            boolean newValue = "true".equals(selected);

            if (newValue != value) {
                this.value = newValue;
                context.codeEditor().replaceLiteralValue(
                        (Expression) this.astNode,
                        String.valueOf(newValue)
                );
                // Immediate UI feedback
                displayLabel.setText(newValue ? "TRUE" : "FALSE");
                String newColor = newValue ? trueColor : falseColor;
                StyleBuilder.create()
                        .backgroundColor(newColor)
                        .applyTo(displayLabel);
            }
        });

        root.getChildren().addAll(displayLabel, booleanSelector);
        root.setMinWidth(60);
        root.setMaxHeight(24);

        return root;
    }

    @Override
    public String getDetails() {
        return "Boolean: " + value;
    }
}