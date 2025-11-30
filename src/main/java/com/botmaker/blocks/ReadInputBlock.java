package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ReadInputBlock extends AbstractStatementBlock {

    private ExpressionBlock variableName;
    private String inputType;

    public ReadInputBlock(String id, VariableDeclarationStatement astNode, String inputType) {
        super(id, astNode);
        this.inputType = inputType;
    }

    public void setVariableName(ExpressionBlock variableName) { this.variableName = variableName; }

    @Override
    protected Node createUINode(CompletionContext context) {
        Label scannerLabel = new Label("scanner." + inputType + "()");
        scannerLabel.getStyleClass().add("method-call-label");

        var sentence = BlockLayout.sentence()
                .addNode(BlockUIComponents.createTypeLabel(getTypeDisplayName()))
                .addNode(variableName != null ? variableName.getUINode(context) : createExpressionDropZone(context))
                .addKeyword("=")
                .addNode(scannerLabel)
                .build();

        return BlockLayout.header()
                .withCustomNode(sentence)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build();
    }

    private String getTypeDisplayName() {
        switch (inputType) {
            case "nextLine": return "String";
            case "nextInt": return "int";
            case "nextDouble": return "double";
            case "nextBoolean": return "boolean";
            default: return "var";
        }
    }
}