package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;

public class BinaryExpressionBlock extends AbstractExpressionBlock {

    private ExpressionBlock leftOperand;
    private ExpressionBlock rightOperand;
    private String operator;
    private final ITypeBinding returnType;

    // Math operator display names (user-friendly)
    private static final String[] MATH_OPERATOR_NAMES = {
            "plus",         // +
            "minus",        // -
            "times",        // *
            "divided by",   // /
            "modulo"        // %
    };

    // Corresponding Java operators
    private static final String[] MATH_OPERATOR_SYMBOLS = {
            "+", "-", "*", "/", "%"
    };

    public BinaryExpressionBlock(String id, InfixExpression astNode) {
        super(id, astNode);
        this.operator = astNode.getOperator().toString();
        this.returnType = astNode.resolveTypeBinding();
    }

    public ExpressionBlock getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(ExpressionBlock leftOperand) {
        this.leftOperand = leftOperand;
    }

    public ExpressionBlock getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(ExpressionBlock rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String getOperator() {
        return operator;
    }

    public ITypeBinding getReturnType() {
        return returnType;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("binary-expression-block");

        HBox expressionBox = new HBox(5);
        expressionBox.setAlignment(Pos.CENTER_LEFT);

        // Left operand
        if (leftOperand != null) {
            expressionBox.getChildren().add(leftOperand.getUINode(context));
        }

        // Check if this is a math operator or comparison/logical operator
        if (isMathOperator(operator)) {
            // Operator selector for math operations
            ComboBox<String> operatorSelector = new ComboBox<>();
            operatorSelector.getItems().addAll(MATH_OPERATOR_NAMES);
            operatorSelector.getStyleClass().add("math-operator-selector");

            // Set current operator
            String currentName = getOperatorDisplayName(operator);
            operatorSelector.setValue(currentName);

            // Handle operator change
            operatorSelector.setOnAction(e -> {
                String selectedName = operatorSelector.getValue();
                String newOperator = getOperatorSymbol(selectedName);
                if (newOperator != null && !newOperator.equals(operator)) {
                    this.operator = newOperator;
                    System.out.println("Math operator changed to: " + newOperator);
                    // Note: Operator change will take effect on next code regeneration
                }
            });

            expressionBox.getChildren().add(operatorSelector);
        } else {
            // For non-math operators (comparisons, logical), just show the symbol
            Label operatorLabel = new Label(operator);
            operatorLabel.getStyleClass().add("operator-label");
            expressionBox.getChildren().add(operatorLabel);
        }

        // Right operand
        if (rightOperand != null) {
            expressionBox.getChildren().add(rightOperand.getUINode(context));
        }

        container.getChildren().add(expressionBox);

        // Type indicator (optional, can be removed if too cluttered)
        String typeName = "unknown";
        if (returnType != null) {
            typeName = returnType.getName();
        }
        Label typeLabel = new Label("→ " + typeName);
        typeLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999; -fx-font-size: 10px;");
        container.getChildren().add(typeLabel);

        return container;
    }

    /**
     * Check if operator is a math operator
     */
    private boolean isMathOperator(String op) {
        for (String mathOp : MATH_OPERATOR_SYMBOLS) {
            if (mathOp.equals(op)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert operator symbol to display name
     */
    private String getOperatorDisplayName(String symbol) {
        for (int i = 0; i < MATH_OPERATOR_SYMBOLS.length; i++) {
            if (MATH_OPERATOR_SYMBOLS[i].equals(symbol)) {
                return MATH_OPERATOR_NAMES[i];
            }
        }
        return symbol; // Return as-is if not found
    }

    /**
     * Convert display name to operator symbol
     */
    private String getOperatorSymbol(String displayName) {
        for (int i = 0; i < MATH_OPERATOR_NAMES.length; i++) {
            if (MATH_OPERATOR_NAMES[i].equals(displayName)) {
                return MATH_OPERATOR_SYMBOLS[i];
            }
        }
        return "+"; // default
    }
}