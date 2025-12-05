package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.util.TypeInfo;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
        var sentenceBuilder = BlockLayout.sentence()
                .addLabel("Print:");

        if (arguments.isEmpty()) {
            sentenceBuilder.addNode(createExpressionDropZone(context));
        } else {
            for (ExpressionBlock arg : arguments) {
                sentenceBuilder.addNode(arg.getUINode(context));
            }
        }

        // Add Button
        Button addButton = createAddButton(e -> {
            org.eclipse.jdt.core.dom.Expression toReplace = !arguments.isEmpty() ?
                    (org.eclipse.jdt.core.dom.Expression) arguments.get(0).getAstNode() : null;
            showExpressionMenuAndReplace((Button)e.getSource(), context, TypeInfo.UNKNOWN, toReplace);
        });

        sentenceBuilder.addNode(addButton);

        return BlockLayout.header()
                .withCustomNode(sentenceBuilder.build())
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build();
    }

    @Override
    public String getDetails() {
        String argsString = arguments.stream().map(ExpressionBlock::getDetails).collect(Collectors.joining(", "));
        return "Print Statement: " + argsString;
    }
}