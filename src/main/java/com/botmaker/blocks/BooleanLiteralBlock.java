package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.BooleanLiteral;

/**
 * Block for true/false values with dropdown selection
 * User-friendly for non-coders
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
        HBox container = new HBox();
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.getStyleClass().add("boolean-literal-block");

        // Dropdown for true/false
        ComboBox<String> booleanSelector = new ComboBox<>();
        booleanSelector.getItems().addAll("true", "false");
        booleanSelector.setValue(value ? "true" : "false");
        booleanSelector.getStyleClass().add("boolean-selector");
        booleanSelector.setEditable(false);

        // Handle value change
        booleanSelector.setOnAction(e -> {
            String selected = booleanSelector.getValue();
            boolean newValue = "true".equals(selected);

            if (newValue != value) {
                this.value = newValue;
                // Update the AST
                context.codeEditor().replaceLiteralValue(
                        (org.eclipse.jdt.core.dom.Expression) this.astNode,
                        String.valueOf(newValue)
                );
            }
        });

        container.getChildren().add(booleanSelector);

        return container;
    }

    @Override
    public String getDetails() {
        return "Boolean: " + value;
    }
}