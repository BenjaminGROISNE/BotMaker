package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrintBlock extends AbstractStatementBlock {

    private final List<ExpressionBlock> arguments = new ArrayList<>();

    public PrintBlock(String id, ExpressionStatement astNode) {
        super(id, astNode);
    }

    public void addArgument(ExpressionBlock argument) { this.arguments.add(argument); }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox content = createSentence();
        content.getChildren().add(new Text("Print:"));

        if (arguments.isEmpty()) {
            content.getChildren().add(createExpressionDropZone(context));
        } else {
            for (ExpressionBlock arg : arguments) {
                content.getChildren().add(arg.getUINode(context));
            }
        }

        // Add Button
        Button addButton = createAddButton(e -> {
            // Logic handles one argument for now in standard print blocks
            org.eclipse.jdt.core.dom.Expression toReplace = !arguments.isEmpty() ?
                    (org.eclipse.jdt.core.dom.Expression) arguments.get(0).getAstNode() : null;
            showExpressionMenuAndReplace((Button)e.getSource(), context, "any", toReplace);
        });

        content.getChildren().add(addButton);

        Node container = createStandardHeader(context, content);
        container.getStyleClass().add("print-block");
        return container;
    }

    @Override
    public String getDetails() {
        String argsString = arguments.stream().map(ExpressionBlock::getDetails).collect(Collectors.joining(", "));
        return "Print Statement: " + argsString;
    }
}