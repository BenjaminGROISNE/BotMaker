package com.botmaker.blocks;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.util.BlockIdPrefix;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ClassBlock extends AbstractCodeBlock implements BlockWithChildren {

    private final String className;
    // Changed from List<MethodDeclarationBlock> to generic CodeBlock to hold Enums too
    private final List<CodeBlock> bodyDeclarations = new ArrayList<>();
    private final BlockDragAndDropManager dragAndDropManager;

    public ClassBlock(String id, TypeDeclaration astNode, BlockDragAndDropManager manager) {
        super(id, astNode);
        this.className = astNode.getName().getIdentifier();
        this.dragAndDropManager = manager;
    }

    public void addBodyDeclaration(CodeBlock block) {
        bodyDeclarations.add(block);
    }

    @Override
    public List<CodeBlock> getChildren() {
        return new ArrayList<>(bodyDeclarations);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(10);
        container.getStyleClass().add("class-block");
        container.setPadding(new Insets(15));

        container.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ecf0f1 0%, #bdc3c7 100%);" +
                        "-fx-border-color: #34495e;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;"
        );

        Label header = new Label("Class: " + className);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        container.getChildren().add(header);

        // Top separator
        container.getChildren().add(createClassMemberSeparator(context, 0));

        for (int i = 0; i < bodyDeclarations.size(); i++) {
            CodeBlock block = bodyDeclarations.get(i);
            container.getChildren().add(block.getUINode(context));
            container.getChildren().add(createClassMemberSeparator(context, i + 1));
        }

        return container;
    }

    private Region createClassMemberSeparator(CompletionContext context, int insertIndex) {
        Region separator = new Region();
        separator.setMinHeight(30);
        separator.setMaxHeight(30);
        separator.getStyleClass().add("method-separator"); // Reuse style

        separator.setStyle(
                "-fx-background-color: rgba(52, 73, 94, 0.15);" +
                        "-fx-border-color: rgba(52, 73, 94, 0.4);" +
                        "-fx-border-width: 2px 0 2px 0;" +
                        "-fx-border-style: dashed;" +
                        "-fx-cursor: hand;"
        );

        // This handler handles dropping Methods OR Enums into the class
        context.dragAndDropManager().addClassMemberDropHandlers(
                separator,
                this,
                insertIndex
        );

        separator.setOnMouseEntered(e -> separator.setStyle("-fx-background-color: rgba(142, 68, 173, 0.3); -fx-border-color: #8E44AD; -fx-border-width: 3px 0 3px 0; -fx-border-style: solid;"));
        separator.setOnMouseExited(e -> separator.setStyle("-fx-background-color: rgba(52, 73, 94, 0.15); -fx-border-color: rgba(52, 73, 94, 0.4); -fx-border-width: 2px 0 2px 0; -fx-border-style: dashed;"));

        return separator;
    }
}