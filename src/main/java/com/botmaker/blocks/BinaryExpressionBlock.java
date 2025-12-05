package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeInfo;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Expression;

import static com.botmaker.ui.components.BlockUIComponents.createChangeButton;

public class BinaryExpressionBlock extends AbstractExpressionBlock {

    private ExpressionBlock leftOperand;
    private ExpressionBlock rightOperand;
    private String operator;
    private final ITypeBinding returnType;

    private static final String[] MATH_OPERATOR_NAMES = { "plus", "minus", "times", "divided by", "modulo" };
    private static final String[] MATH_OPERATOR_SYMBOLS = { "+", "-", "*", "/", "%" };

    public BinaryExpressionBlock(String id, InfixExpression astNode) {
        super(id, astNode);
        this.operator = astNode.getOperator().toString();
        this.returnType = astNode.resolveTypeBinding();
    }

    public void setLeftOperand(ExpressionBlock leftOperand) { this.leftOperand = leftOperand; }
    public void setRightOperand(ExpressionBlock rightOperand) { this.rightOperand = rightOperand; }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox expressionBox = new HBox(5);
        expressionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Left operand + Change Button
        if (leftOperand != null) {
            expressionBox.getChildren().add(leftOperand.getUINode(context));
            javafx.scene.control.Button changeLeft = createChangeButton(e ->
                    showExpressionMenuAndReplace((Button)e.getSource(), context, TypeInfo.INT,
                            (Expression) leftOperand.getAstNode())
            );
            changeLeft.setStyle("-fx-font-size: 8px; -fx-padding: 1px 3px;");
            expressionBox.getChildren().add(changeLeft);
        }

        // Operator Selector
        if (isMathOperator(operator)) {
            javafx.scene.control.ComboBox<String> selector = createOperatorSelector(
                    MATH_OPERATOR_NAMES,
                    MATH_OPERATOR_SYMBOLS,
                    operator,
                    newOp -> {
                        this.operator = newOp;
                        context.codeEditor().updateBinaryOperator((InfixExpression) this.astNode, newOp);
                    }
            );
            expressionBox.getChildren().add(selector);
        } else {
            expressionBox.getChildren().add(createOperatorLabel(operator));
        }

        // Right operand + Change Button
        if (rightOperand != null) {
            expressionBox.getChildren().add(rightOperand.getUINode(context));
            javafx.scene.control.Button changeRight = createChangeButton(e ->
                    showExpressionMenuAndReplace((Button)e.getSource(), context, TypeInfo.INT,
                            (Expression) rightOperand.getAstNode())
            );
            changeRight.setStyle("-fx-font-size: 8px; -fx-padding: 1px 3px;");
            expressionBox.getChildren().add(changeRight);
        }

        container.getChildren().add(expressionBox);

        // Type indicator
        String typeName = (returnType != null) ? returnType.getName() : "unknown";
        javafx.scene.control.Label typeLabel = new javafx.scene.control.Label("→ " + typeName);
        typeLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999; -fx-font-size: 10px;");
        container.getChildren().add(typeLabel);

        return container;
    }

    private boolean isMathOperator(String op) {
        for (String mathOp : MATH_OPERATOR_SYMBOLS) {
            if (mathOp.equals(op)) return true;
        }
        return false;
    }
}