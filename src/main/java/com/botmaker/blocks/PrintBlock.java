package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrintBlock extends AbstractStatementBlock {

    private final List<ExpressionBlock> arguments = new ArrayList<>();

    public PrintBlock(String id, MethodInvocation astNode) {
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
        container.setStyle("-fx-background-color: #e6f7ff; -fx-border-color: #91d5ff; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");

        container.getChildren().add(new Text("Print:"));

        if (arguments.isEmpty()) {
            container.getChildren().add(createExpressionDropZone(context));
        } else {
            for (ExpressionBlock arg : arguments) {
                container.getChildren().add(arg.getUINode(context));
            }
            // Optionally, add a drop zone to add more arguments
            // container.getChildren().add(createExpressionDropZone(context));
        }

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