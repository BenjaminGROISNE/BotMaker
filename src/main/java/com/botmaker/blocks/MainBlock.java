package com.botmaker.blocks;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * MainBlock is just a MethodDeclarationBlock that detects if it's the "main" method
 * and adjusts its UI styling accordingly.
 */
public class MainBlock extends MethodDeclarationBlock {

    private final boolean isMainMethod;

    public MainBlock(String id, MethodDeclaration astNode, BlockDragAndDropManager manager) {
        super(id, astNode, manager);
        this.isMainMethod = "main".equals(astNode.getName().getIdentifier()) &&
                org.eclipse.jdt.core.dom.Modifier.isStatic(astNode.getModifiers());
    }

    // MainBlock.java
    @Override
    protected Node createUINode(CompletionContext context) {
        Node standardUI = super.createUINode(context);

        if (isMainMethod) {
            VBox wrapper = new VBox(standardUI);
            wrapper.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 5;");

            Label mainBadge = new Label("⭐ Program Entry Point");
            mainBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
            wrapper.getChildren().add(0, mainBadge);

            return wrapper;
        }

        return standardUI;
    }
}