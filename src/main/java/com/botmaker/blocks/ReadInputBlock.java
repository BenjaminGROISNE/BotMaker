package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ReadInputBlock extends AbstractStatementBlock {

    private ExpressionBlock variableName;
    private String inputType; // "nextLine", "nextInt", "nextDouble", etc.

    public ReadInputBlock(String id, VariableDeclarationStatement astNode, String inputType) {
        super(id, astNode);
        this.inputType = inputType;
    }

    public ExpressionBlock getVariableName() {
        return variableName;
    }

    public void setVariableName(ExpressionBlock variableName) {
        this.variableName = variableName;
    }

    public String getInputType() {
        return inputType;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("read-input-block");

        // Display based on input type
        Label typeLabel = new Label(getTypeDisplayName());
        typeLabel.getStyleClass().add("type-label");

        if (variableName != null) {
            container.getChildren().add(variableName.getUINode(context));
        }

        Label equalsLabel = new Label("=");
        Label scannerLabel = new Label("scanner." + inputType + "()");
        scannerLabel.getStyleClass().add("method-call-label");

        container.getChildren().addAll(typeLabel, equalsLabel, scannerLabel);

        // Add spacer and delete button
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    private String getTypeDisplayName() {
        switch (inputType) {
            case "nextLine":
                return "String";
            case "nextInt":
                return "int";
            case "nextDouble":
                return "double";
            case "nextBoolean":
                return "boolean";
            default:
                return "var";
        }
    }
}