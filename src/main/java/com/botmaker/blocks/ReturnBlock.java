package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

public class ReturnBlock extends AbstractStatementBlock {

    private ExpressionBlock expression;

    public ReturnBlock(String id, ReturnStatement astNode) {
        super(id, astNode);
    }

    public void setExpression(ExpressionBlock expression) {
        this.expression = expression;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("return-block");

        Label returnLabel = new Label("return");
        returnLabel.getStyleClass().add("keyword-label");
        container.getChildren().add(returnLabel);

        // Determine parent method return type
        String methodReturnType = findParentMethodReturnType();
        boolean isVoid = "void".equals(methodReturnType);

        if (expression != null) {
            container.getChildren().add(expression.getUINode(context));

            // Allow changing it
            Button changeBtn = new Button("↻");
            changeBtn.getStyleClass().add("icon-button");
            changeBtn.setOnAction(e -> showExpressionMenu(changeBtn, context, methodReturnType));
            container.getChildren().add(changeBtn);

        } else if (!isVoid) {
            // Only show "Add" button if not void
            Button addButton = new Button("+");
            addButton.getStyleClass().add("expression-add-button");
            addButton.setOnAction(e -> showExpressionMenu(addButton, context, methodReturnType));
            container.getChildren().add(addButton);
        } else {
            // It's void and has no expression -> Just show implicit "void" label or nothing
            Label voidLabel = new Label("(void)");
            voidLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px; -fx-font-style: italic;");
            container.getChildren().add(voidLabel);
        }

        // Spacer and Delete Button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    private String findParentMethodReturnType() {
        ASTNode current = this.astNode.getParent();
        while (current != null) {
            if (current instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) current;
                if (md.getReturnType2() != null) {
                    return md.getReturnType2().toString();
                }
                return "void";
            }
            current = current.getParent();
        }
        return "void"; // Default safe fallback
    }

    private void showExpressionMenu(Button button, CompletionContext context, String targetType) {
        ContextMenu menu = new ContextMenu();

        // Convert Java type (e.g., int) to UI type (e.g., number)
        String uiType = TypeManager.determineUiType(targetType);

        for (AddableExpression type : AddableExpression.getForType(uiType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                context.codeEditor().setReturnExpression(
                        (ReturnStatement) this.astNode,
                        type
                );
            });
            menu.getItems().add(menuItem);
        }
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}