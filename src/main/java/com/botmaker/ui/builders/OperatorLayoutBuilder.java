package com.botmaker.ui.builders;

import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.SelectorComponents;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class OperatorLayoutBuilder {
    private ExpressionBlock leftOperand;
    private ExpressionBlock rightOperand;
    private String operator;
    private String[] operatorNames;
    private String[] operatorSymbols;
    private Consumer<String> onOperatorChange;
    private CompletionContext context;
    private String operandType = "number";

    public OperatorLayoutBuilder withLeftOperand(
            ExpressionBlock operand,
            CompletionContext context) {
        this.leftOperand = operand;
        this.context = context;
        return this;
    }

    public OperatorLayoutBuilder withRightOperand(
            ExpressionBlock operand,
            CompletionContext context) {
        this.rightOperand = operand;
        this.context = context;
        return this;
    }

    public OperatorLayoutBuilder withOperator(
            String operator,
            String[] names,
            String[] symbols,
            Consumer<String> onChange) {
        this.operator = operator;
        this.operatorNames = names;
        this.operatorSymbols = symbols;
        this.onOperatorChange = onChange;
        return this;
    }

    public OperatorLayoutBuilder withOperandType(String type) {
        this.operandType = type;
        return this;
    }

    public HBox build() {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("binary-expression-block");

        HBox expressionBox = new HBox(5);
        expressionBox.setAlignment(Pos.CENTER_LEFT);

        // Left operand
        if (leftOperand != null) {
            expressionBox.getChildren().add(leftOperand.getUINode(context));
            Button changeLeft = createChangeButton();
            expressionBox.getChildren().add(changeLeft);
        }

        // Operator selector
        if (operatorNames != null && operatorSymbols != null) {
            ComboBox<String> selector = SelectorComponents.createOperatorSelector(
                    operatorNames, operatorSymbols, operator, onOperatorChange
            );
            selector.getStyleClass().add("math-operator-selector");
            expressionBox.getChildren().add(selector);
        } else {
            Label opLabel = new Label(operator);
            opLabel.getStyleClass().add("operator-label");
            expressionBox.getChildren().add(opLabel);
        }

        // Right operand
        if (rightOperand != null) {
            expressionBox.getChildren().add(rightOperand.getUINode(context));
            Button changeRight = createChangeButton();
            expressionBox.getChildren().add(changeRight);
        }

        container.getChildren().add(expressionBox);

        return container;
    }

    private Button createChangeButton() {
        Button btn = new Button("↻");
        btn.getStyleClass().add("icon-button");
        btn.setStyle("-fx-font-size: 8px; -fx-padding: 1px 3px;");
        return btn;
    }
}