// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\blocks\ComparisonExpressionBlock.java
package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import javafx.scene.Node;
import org.eclipse.jdt.core.dom.Expression;
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
            "not equal to",                 // !=
            "AND (&&)",                     // &&
            "OR (||)"                       // ||
    };

    // Corresponding Java operators
    private static final String[] OPERATOR_SYMBOLS = {
            "<", "<=", ">", ">=", "==", "!=", "&&", "||"
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
        // Determine types for drop slots based on operator
        // If Logic (&&, ||), operands must be boolean.
        // If Comparison (>, <, etc), operands are typically numbers (but == can be anything).

        String operandType = "number"; // default for <, >, etc
        if ("&&".equals(operator) || "||".equals(operator)) {
            operandType = "boolean";
        } else if ("==".equals(operator) || "!=".equals(operator)) {
            operandType = "any";
        }

        var sentence = BlockLayout.sentence()
                .addExpressionSlot(leftOperand, context, operandType)
                .addOperatorSelector(
                        OPERATOR_NAMES,
                        OPERATOR_SYMBOLS,
                        operator,
                        newOperator -> {
                            if (newOperator != null && !newOperator.equals(operator)) {
                                this.operator = newOperator;
                                // Update AST
                                if (this.astNode instanceof InfixExpression) {
                                    context.codeEditor().updateBinaryOperator((InfixExpression) this.astNode, newOperator);
                                }
                            }
                        }
                )
                .addExpressionSlot(rightOperand, context, operandType)
                .build();

        // Add specific styling for logic blocks
        if ("&&".equals(operator) || "||".equals(operator)) {
            sentence.getStyleClass().add("logic-expression-block");
        } else {
            sentence.getStyleClass().add("comparison-expression-block");
        }

        return sentence;
    }
}