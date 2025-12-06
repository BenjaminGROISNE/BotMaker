package com.botmaker.blocks;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ClassBlock extends AbstractCodeBlock implements BlockWithChildren {

    private final String className;
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

            // Make methods draggable for reordering
            if (block instanceof MethodDeclarationBlock) {
                Node node = block.getUINode(context);
                node.setOnDragDetected(e -> {
                    // Logic handled in BlockDragAndDropManager via makeBlockMovable logic
                    // But we need to ensure the manager knows about this.
                    // Since MethodDeclarationBlock doesn't have a "sourceBody", passing null or 'this' context is tricky
                    // unless we handle ClassBlock dragging specifically.
                    // We will handle this in MethodDeclarationBlock's own UI creation or via manager helper.
                    context.dragAndDropManager().makeBlockMovable(node, (com.botmaker.core.StatementBlock) block, null);
                });
            }

            container.getChildren().add(block.getUINode(context));
            container.getChildren().add(createClassMemberSeparator(context, i + 1));
        }

        // Add Method Button
        Button addMethodBtn = new Button("+ Add Function");
        addMethodBtn.setMaxWidth(Double.MAX_VALUE);
        addMethodBtn.setStyle(
                "-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"
        );
        addMethodBtn.setOnAction(e -> {
            context.codeEditor().addMethodToClass(
                    (TypeDeclaration) this.astNode,
                    "newMethod",
                    "void",
                    bodyDeclarations.size() // Add to end
            );
        });
        container.getChildren().add(addMethodBtn);

        return container;
    }

    private Region createClassMemberSeparator(CompletionContext context, int insertIndex) {
        Region separator = new Region();
        separator.setMinHeight(15);
        separator.setMaxHeight(15);

        separator.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;"
        );

        context.dragAndDropManager().addClassMemberDropHandlers(separator, this, insertIndex);

        return separator;
    }
}