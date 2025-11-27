package com.botmaker.blocks;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class LibraryBlock extends AbstractCodeBlock implements BlockWithChildren {

    private final String className;
    private final List<MethodDeclarationBlock> methods = new ArrayList<>();

    public LibraryBlock(String id, TypeDeclaration astNode) {
        super(id, astNode);
        this.className = astNode.getName().getIdentifier();
    }

    public void addMethod(MethodDeclarationBlock method) {
        methods.add(method);
    }

    @Override
    public List<CodeBlock> getChildren() {
        return new ArrayList<>(methods);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(10);
        container.getStyleClass().add("library-block");
        container.setPadding(new Insets(10));
        // Ensure background is set so white text (if any) is visible,
        // OR enforce text color to be dark.
        container.setStyle("-fx-background-color: #f4f6f9;");

        Label header = new Label("Library: " + className);
        // Using specific styling instead of generic main-block-header which might be designed for dark backgrounds
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");

        container.getChildren().add(header);

        for (MethodDeclarationBlock method : methods) {
            container.getChildren().add(method.getUINode(context));
        }

        return container;
    }
}