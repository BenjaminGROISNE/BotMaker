package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrintBlock extends AbstractStatementBlock {

    private final List<ExpressionBlock> arguments = new ArrayList<>();

    public PrintBlock(String id, ExpressionStatement astNode) {
        super(id, astNode);
    }

    public List<ExpressionBlock> getArguments() {
        return arguments;
    }

    public void addArgument(ExpressionBlock argument) {
        this.arguments.add(argument);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("print-block");

        container.getChildren().add(new Text("Print:"));

        if (arguments.isEmpty()) {
            container.getChildren().add(createExpressionDropZone(context));
        } else {
            for (ExpressionBlock arg : arguments) {
                container.getChildren().add(arg.getUINode(context));
            }
        }

        javafx.scene.control.Button addButton = new javafx.scene.control.Button("+");
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.values()) {
            javafx.scene.control.MenuItem menuItem = new javafx.scene.control.MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                if (!arguments.isEmpty()) {
                    // We are assuming one argument for now
                    Expression toReplace = (Expression) arguments.get(0).getAstNode();
                    context.codeEditor().replaceExpression(toReplace, type);
                }
            });
            contextMenu.getItems().add(menuItem);
        }

        addButton.setOnAction(e -> contextMenu.show(addButton, javafx.geometry.Side.BOTTOM, 0, 0));
        container.getChildren().add(addButton);

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    @Override
    public String getDetails() {
        String argsString = arguments.stream()
                .map(ExpressionBlock::getDetails)
                .collect(Collectors.joining(", "));
        return "Print Statement: " + argsString;
    }
}
