package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;

public class ComparisonExpressionBlock extends AbstractExpressionBlock {

    private ExpressionBlock leftOperand;
    private ExpressionBlock rightOperand;
    private String operator;
    private final ITypeBinding returnType;

    // Operator display names (user-friendly)
    private static final String[] OPERATOR_NAMES = {
            "less than",                    // <
            "less than or equal",           // <=
            "greater than",                 // >
            "greater than or equal",        // >=
            "equal to",                     // ==
            "not equal to"                  // !=
    };

    // Corresponding Java operators
    private static final String[] OPERATOR_SYMBOLS = {
            "<", "<=", ">", ">=", "==", "!="
    };

    public ComparisonExpressionBlock(String id, InfixExpression astNode) {
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
        container.getStyleClass().add("comparison-expression-block");

        // Left operand
        if (leftOperand != null) {
            container.getChildren().add(leftOperand.getUINode(context));
        }

        // Operator selector - shows user-friendly names
        ComboBox<String> operatorSelector = new ComboBox<>();
        operatorSelector.getItems().addAll(OPERATOR_NAMES);
        operatorSelector.getStyleClass().add("comparison-operator-selector");

        // Set current operator
        String currentName = getOperatorDisplayName(operator);
        operatorSelector.setValue(currentName);

        // Handle operator change
        operatorSelector.setOnAction(e -> {
            String selectedName = operatorSelector.getValue();
            String newOperator = getOperatorSymbol(selectedName);
            if (newOperator != null && !newOperator.equals(operator)) {
                this.operator = newOperator;
                // Trigger code regeneration with new operator
                System.out.println("Comparison operator changed to: " + newOperator);
            }
        });

        container.getChildren().add(operatorSelector);

        // Right operand
        if (rightOperand != null) {
            container.getChildren().add(rightOperand.getUINode(context));
        }

        return container;
    }

    /**
     * Convert operator symbol to display name
     */
    private String getOperatorDisplayName(String symbol) {
        for (int i = 0; i < OPERATOR_SYMBOLS.length; i++) {
            if (OPERATOR_SYMBOLS[i].equals(symbol)) {
                return OPERATOR_NAMES[i];
            }
        }
        return "equal to"; // default
    }

    /**
     * Convert display name to operator symbol
     */
    private String getOperatorSymbol(String displayName) {
        for (int i = 0; i < OPERATOR_NAMES.length; i++) {
            if (OPERATOR_NAMES[i].equals(displayName)) {
                return OPERATOR_SYMBOLS[i];
            }
        }
        return "=="; // default
    }
}