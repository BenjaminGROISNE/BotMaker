package com.botmaker.core;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.DropInfo;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class BodyBlock extends AbstractStatementBlock implements BlockWithChildren {
    private final List<StatementBlock> statements = new ArrayList<>();
    private final BlockDragAndDropManager dragAndDropManager;

    public BodyBlock(String id, org.eclipse.jdt.core.dom.Block astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
        this.dragAndDropManager = dragAndDropManager;
    }

    public void addStatement(StatementBlock statement) {
        statements.add(statement);
    }

    @Override
    public List<CodeBlock> getChildren() {
        return new ArrayList<>(statements);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox();
        container.getStyleClass().add("body-block");

        if (statements.isEmpty()) {
            javafx.scene.control.Label placeholder = new javafx.scene.control.Label("Drag block here");
            placeholder.getStyleClass().add("empty-body-placeholder");
            dragAndDropManager.addSeparatorDragHandlers(placeholder, this, 0, null);
            container.getChildren().add(placeholder);
            container.setAlignment(javafx.geometry.Pos.CENTER);
            container.setMinHeight(30);
        } else {
            // Add a separator at the beginning
            container.getChildren().add(createSeparatorWithHandlers(this, 0));

            for (int i = 0; i < statements.size(); i++) {
                StatementBlock statement = statements.get(i);
                container.getChildren().add(statement.getUINode(context));
                // Add a separator after each statement
                container.getChildren().add(createSeparatorWithHandlers(this, i + 1));
            }
        }
        return container;
    }

    private Node createSeparatorWithHandlers(BodyBlock targetBody, int insertionIndex) {
        javafx.scene.layout.Region separator = dragAndDropManager.createSeparator();
        separator.getStyleClass().add("body-block-separator");
        StatementBlock adjacentBlock = (insertionIndex < statements.size()) ? statements.get(insertionIndex) : null;
        dragAndDropManager.addSeparatorDragHandlers(separator, targetBody, insertionIndex, adjacentBlock);
        return separator;
    }
}