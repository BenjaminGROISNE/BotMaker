package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class VariableDeclarationBlock extends AbstractStatementBlock {

    private final String variableName;
    private final Type variableType;
    private ExpressionBlock initializer;

    public VariableDeclarationBlock(String id, VariableDeclarationStatement astNode) {
        super(id, astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);
        this.variableName = fragment.getName().getIdentifier();
        this.variableType = astNode.getType();
        this.initializer = null; // This will be set by the converter
    }

    public String getVariableName() {
        return variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public ExpressionBlock getInitializer() {
        return initializer;
    }

    public void setInitializer(ExpressionBlock initializer) {
        this.initializer = initializer;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("variable-declaration-block");

        container.getChildren().add(new Text(variableType.toString()));
        container.getChildren().add(new Text(variableName));
        container.getChildren().add(new Text("="));

        if (initializer != null) {
            container.getChildren().add(initializer.getUINode(context));
        } else {
            container.getChildren().add(createExpressionDropZone(context));
        }

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    @Override
    public String getDetails() {
        String initializerText = initializer != null ? " = ..." : "";
        return "Variable Declaration: " + variableType.toString() + " " + variableName + initializerText;
    }
}