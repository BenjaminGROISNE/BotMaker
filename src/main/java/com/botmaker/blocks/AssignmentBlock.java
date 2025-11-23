package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.SelectorComponents;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.*;

public class AssignmentBlock extends AbstractStatementBlock {

    private ExpressionBlock leftHandSide;
    private ExpressionBlock rightHandSide;
    private String operator;

    private static final String[] OPERATOR_NAMES = {
            "set to", "add", "subtract", "multiply by", "divide by", "increment", "decrement"
    };

    private static final String[] OPERATOR_SYMBOLS = {
            "=", "+=", "-=", "*=", "/=", "++", "--"
    };

    public AssignmentBlock(String id, ExpressionStatement astNode) {
        super(id, astNode);
        initializeOperator(astNode);
    }

    private void initializeOperator(ExpressionStatement astNode) {
        if (astNode.getExpression() instanceof Assignment) {
            this.operator = ((Assignment) astNode.getExpression()).getOperator().toString();
        } else if (astNode.getExpression() instanceof PostfixExpression) {
            this.operator = ((PostfixExpression) astNode.getExpression()).getOperator().toString();
        } else if (astNode.getExpression() instanceof PrefixExpression) {
            this.operator = ((PrefixExpression) astNode.getExpression()).getOperator().toString();
        } else {
            this.operator = "=";
        }
    }

    public void setLeftHandSide(ExpressionBlock leftHandSide) { this.leftHandSide = leftHandSide; }
    public void setRightHandSide(ExpressionBlock rightHandSide) { this.rightHandSide = rightHandSide; }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("assignment-block");

        // Left hand side
        if (leftHandSide != null) {
            container.getChildren().add(leftHandSide.getUINode(context));
        }

        // Operator selector via Component Factory
        ComboBox<String> operatorSelector = SelectorComponents.createOperatorSelector(
                OPERATOR_NAMES,
                OPERATOR_SYMBOLS,
                operator,
                newOperator -> {
                    this.operator = newOperator;
                    if (this.astNode instanceof ExpressionStatement) {
                        Expression expr = ((ExpressionStatement) this.astNode).getExpression();
                        context.codeEditor().updateAssignmentOperator(expr, newOperator);
                    }
                }
        );
        container.getChildren().add(operatorSelector);

        // Right hand side (only for non-increment/decrement)
        if (!operator.equals("++") && !operator.equals("--")) {
            if (rightHandSide != null) {
                container.getChildren().add(rightHandSide.getUINode(context));
            }

            // FIX: Use e.getSource() to avoid uninitialized variable reference
            Button addButton = createAddButton(e -> showExpressionMenu((Button) e.getSource(), context));
            container.getChildren().add(addButton);
        }

        // Spacer and Delete button
        container.getChildren().addAll(
                BlockUIComponents.createSpacer(),
                createDeleteButton(context)
        );

        return container;
    }

    private void showExpressionMenu(Button button, CompletionContext context) {
        String targetType = "any";
        if (leftHandSide != null && leftHandSide.getAstNode() != null) {
            Expression lhsExpr = (org.eclipse.jdt.core.dom.Expression) leftHandSide.getAstNode();
            org.eclipse.jdt.core.dom.ITypeBinding binding = lhsExpr.resolveTypeBinding();
            if (binding != null) {
                targetType = com.botmaker.util.TypeManager.determineUiType(binding.getName());
            }
        }

        org.eclipse.jdt.core.dom.Expression toReplace = null;
        if (rightHandSide != null) {
            toReplace = (org.eclipse.jdt.core.dom.Expression) rightHandSide.getAstNode();
        }

        // Use helper from AbstractStatementBlock
        showExpressionMenuAndReplace(button, context, targetType, toReplace);
    }
}