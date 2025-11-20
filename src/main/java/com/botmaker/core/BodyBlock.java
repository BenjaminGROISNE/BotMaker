package com.botmaker.core;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
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

    public List<StatementBlock> getStatements() {
        return new ArrayList<>(statements);
    }

    public void removeStatement(StatementBlock statement) {
        statements.remove(statement);
    }

    public void insertStatement(int index, StatementBlock statement) {
        if (index < 0 || index > statements.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + statements.size());
        }
        statements.add(index, statement);
    }

    @Override
    public List<CodeBlock> getChildren() {
        return new ArrayList<>(statements);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox();
        container.getStyleClass().add("body-block");
        VBox.setVgrow(container, Priority.ALWAYS);

        if (statements.isEmpty()) {
            javafx.scene.control.Label placeholder = new javafx.scene.control.Label("Drag block here");
            placeholder.getStyleClass().add("empty-body-placeholder");
            placeholder.setMouseTransparent(true);
            container.getChildren().add(placeholder);
            container.setAlignment(javafx.geometry.Pos.CENTER);
            container.setMinHeight(30);
            dragAndDropManager.addEmptyBodyDropHandlers(container, this);
        } else {
            // Add a separator at the beginning
            container.getChildren().add(createSeparatorWithHandlers(this, 0));

            for (int i = 0; i < statements.size(); i++) {
                StatementBlock statement = statements.get(i);
                Node statementNode = statement.getUINode(context);

                // Make the statement block draggable for repositioning
                makeStatementDraggable(statementNode, statement);

                container.getChildren().add(statementNode);
                // Add a separator after each statement
                container.getChildren().add(createSeparatorWithHandlers(this, i + 1));
            }
        }
        return container;
    }

    /**
     * Makes a statement block's UI node draggable so it can be repositioned.
     */
    private void makeStatementDraggable(Node statementNode, StatementBlock statement) {
        // Add visual cue that the block is draggable
        statementNode.setOnMouseEntered(e -> {
            statementNode.setCursor(Cursor.OPEN_HAND);
        });

        statementNode.setOnMouseExited(e -> {
            statementNode.setCursor(Cursor.DEFAULT);
        });

        // Register with the drag and drop manager
        dragAndDropManager.makeBlockMovable(statementNode, statement, this);
    }

    private Node createSeparatorWithHandlers(BodyBlock targetBody, int insertionIndex) {
        javafx.scene.layout.Region separator = dragAndDropManager.createSeparator();
        separator.getStyleClass().add("body-block-separator");
        StatementBlock adjacentBlock = (insertionIndex < statements.size()) ? statements.get(insertionIndex) : null;
        dragAndDropManager.addSeparatorDragHandlers(separator, targetBody, insertionIndex, adjacentBlock);
        return separator;
    }
}