package com.botmaker.blocks;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassBlock extends AbstractCodeBlock implements BlockWithChildren {

    private final String className;
    private final List<MethodDeclarationBlock> methods = new ArrayList<>();
    private final BlockDragAndDropManager dragAndDropManager;

    public ClassBlock(String id, TypeDeclaration astNode, BlockDragAndDropManager manager) {
        super(id, astNode);
        this.className = astNode.getName().getIdentifier();
        this.dragAndDropManager = manager;
    }

    public void addMethod(MethodDeclarationBlock method) {
        methods.add(method);
    }

    public List<MethodDeclarationBlock> getMethods() {
        return new ArrayList<>(methods);
    }

    @Override
    public List<CodeBlock> getChildren() {
        return new ArrayList<>(methods);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(10);
        container.getStyleClass().add("class-block");
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #f4f6f9;");

        // Header
        Label header = new Label("Class: " + className);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        container.getChildren().add(header);

        // Top "Add Method" button
        Button addMethodTopBtn = new Button("+ Add Method");
        addMethodTopBtn.setMaxWidth(Double.MAX_VALUE);
        addMethodTopBtn.setOnAction(e -> addNewMethod(context, 0));
        container.getChildren().add(addMethodTopBtn);

        // Methods
        for (int i = 0; i < methods.size(); i++) {
            MethodDeclarationBlock method = methods.get(i);
            container.getChildren().add(method.getUINode(context));

            // Add method button between methods
            final int insertIndex = i + 1;
            Button addMethodBtn = new Button("+ Add Method");
            addMethodBtn.setMaxWidth(Double.MAX_VALUE);
            addMethodBtn.setOnAction(e -> addNewMethod(context, insertIndex));
            container.getChildren().add(addMethodBtn);
        }

        return container;
    }

    private void addNewMethod(CompletionContext context, int index) {
        context.codeEditor().addMethodToClass(
                (TypeDeclaration) this.astNode,
                "newMethod",
                "void",
                index
        );
    }
}