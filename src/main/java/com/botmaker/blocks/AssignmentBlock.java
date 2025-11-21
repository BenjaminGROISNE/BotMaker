package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class AssignmentBlock extends AbstractStatementBlock {

    private ExpressionBlock leftHandSide;
    private ExpressionBlock rightHandSide;
    private String operator;

    // Operator display names (user-friendly)
    private static final String[] OPERATOR_NAMES = {
            "set to",           // =
            "add",              // +=
            "subtract",         // -=
            "multiply by",      // *=
            "divide by",        // /=
            "increment",        // ++
            "decrement"         // --
    };

    // Corresponding Java operators
    private static final String[] OPERATOR_SYMBOLS = {
            "=", "+=", "-=", "*=", "/=", "++", "--"
    };

    public AssignmentBlock(String id, ExpressionStatement astNode) {
        super(id, astNode);

        // Detect the operator from the AST node
        if (astNode.getExpression() instanceof Assignment) {
            Assignment assignment = (Assignment) astNode.getExpression();
            this.operator = assignment.getOperator().toString();
        } else if (astNode.getExpression() instanceof PostfixExpression) {
            PostfixExpression postfix = (PostfixExpression) astNode.getExpression();
            this.operator = postfix.getOperator().toString();
        } else if (astNode.getExpression() instanceof PrefixExpression) {
            PrefixExpression prefix = (PrefixExpression) astNode.getExpression();
            this.operator = prefix.getOperator().toString();
        } else {
            this.operator = "="; // default
        }
    }

    public ExpressionBlock getLeftHandSide() {
        return leftHandSide;
    }

    public void setLeftHandSide(ExpressionBlock leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    public ExpressionBlock getRightHandSide() {
        return rightHandSide;
    }

    public void setRightHandSide(ExpressionBlock rightHandSide) {
        this.rightHandSide = rightHandSide;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("assignment-block");

        // Left hand side
        if (leftHandSide != null) {
            container.getChildren().add(leftHandSide.getUINode(context));
        }

        // Operator selector - shows user-friendly names
        ComboBox<String> operatorSelector = new ComboBox<>();
        operatorSelector.getItems().addAll(OPERATOR_NAMES);
        operatorSelector.getStyleClass().add("operator-selector");
        operatorSelector.setEditable(false); // Not text-editable, just dropdown selection

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
                System.out.println("Operator changed to: " + newOperator);
                // Note: This will take effect on next code regeneration
            }
        });

        container.getChildren().add(operatorSelector);

        // Right hand side (only show for non-increment/decrement)
        if (!operator.equals("++") && !operator.equals("--")) {
            if (rightHandSide != null) {
                container.getChildren().add(rightHandSide.getUINode(context));
            }
        }

        // Add spacer and delete button (NO semicolon!)
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

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
        return "set to"; // default
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
        return "="; // default
    }
}