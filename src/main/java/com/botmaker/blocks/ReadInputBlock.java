package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import static com.botmaker.ui.components.BlockUIComponents.createTypeLabel;

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
        Node varNode = variableName != null ? variableName.getUINode(context) : null;

        Label scannerLabel = new Label("scanner." + inputType + "()");
        scannerLabel.getStyleClass().add("method-call-label");

        Node content = createSentence(
                createTypeLabel(getTypeDisplayName()),
                varNode,
                createKeywordLabel("="),
                scannerLabel
        );

        Node container = createStandardHeader(context, content);
        container.getStyleClass().add("read-input-block");
        return container;
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