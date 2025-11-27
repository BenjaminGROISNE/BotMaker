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
import javafx.scene.layout.Region;
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
        container.setPadding(new Insets(15));

        // Better styling with visible borders
        container.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ecf0f1 0%, #bdc3c7 100%);" +
                        "-fx-border-color: #34495e;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;"
        );

        // Header with better contrast
        Label header = new Label("Class: " + className);
        header.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-padding: 0 0 15 0;" +
                        "-fx-border-width: 0 0 2 0;" +
                        "-fx-border-color: #34495e;"
        );
        container.getChildren().add(header);

        // Top separator with drag-and-drop
        Region topSeparator = createMethodSeparator(context, 0);
        container.getChildren().add(topSeparator);

        // Methods with separators between them
        for (int i = 0; i < methods.size(); i++) {
            MethodDeclarationBlock method = methods.get(i);
            container.getChildren().add(method.getUINode(context));

            // Separator after each method
            final int insertIndex = i + 1;
            Region separator = createMethodSeparator(context, insertIndex);
            container.getChildren().add(separator);
        }

        return container;
    }

    private Region createMethodSeparator(CompletionContext context, int insertIndex) {
        Region separator = new Region();
        separator.setMinHeight(30); // Increased for better visibility
        separator.setMaxHeight(30);
        separator.getStyleClass().add("method-separator");

        // Visible styling
        separator.setStyle(
                "-fx-background-color: rgba(52, 73, 94, 0.15);" +
                        "-fx-border-color: rgba(52, 73, 94, 0.4);" +
                        "-fx-border-width: 2px 0 2px 0;" +
                        "-fx-border-style: dashed;" +
                        "-fx-cursor: hand;"
        );

        // Add drag-and-drop handlers
        context.dragAndDropManager().addMethodDeclarationDropHandlers(
                separator,
                this,
                insertIndex
        );

        // Visual feedback on hover
        separator.setOnMouseEntered(e -> {
            separator.setStyle(
                    "-fx-background-color: rgba(142, 68, 173, 0.3);" +
                            "-fx-border-color: #8E44AD;" +
                            "-fx-border-width: 3px 0 3px 0;" +
                            "-fx-border-style: solid;" +
                            "-fx-cursor: hand;"
            );
        });

        separator.setOnMouseExited(e -> {
            separator.setStyle(
                    "-fx-background-color: rgba(52, 73, 94, 0.15);" +
                            "-fx-border-color: rgba(52, 73, 94, 0.4);" +
                            "-fx-border-width: 2px 0 2px 0;" +
                            "-fx-border-style: dashed;" +
                            "-fx-cursor: hand;"
            );
        });

        // FIXED: Add click handler for quick method addition
        separator.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { // Double-click to add method
                addNewMethod(context, insertIndex);
            }
        });

        return separator;
    }

    private void addNewMethod(CompletionContext context, int index) {
        System.out.println("DEBUG: Adding method at index " + index); // DEBUG
        context.codeEditor().addMethodToClass(
                (TypeDeclaration) this.astNode,
                "newMethod",
                "void",
                index
        );
    }
}