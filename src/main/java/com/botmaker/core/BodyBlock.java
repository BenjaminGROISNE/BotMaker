package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.Block;

import java.util.ArrayList;
import java.util.List;

public class BodyBlock extends AbstractStatementBlock {
    private final List<StatementBlock> statements = new ArrayList<>();
    private final BlockDragAndDropManager dragAndDropManager;

    public BodyBlock(String id, Block astNode, BlockDragAndDropManager manager) {
        super(id, astNode);
        this.dragAndDropManager = manager;
    }

    public List<StatementBlock> getStatements() {
        return statements;
    }

    public void addStatement(StatementBlock statement) {
        this.statements.add(statement);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        ListView<StatementBlock> listView = new ListView<>();
        listView.setItems(FXCollections.observableList(statements));
        listView.setStyle("-fx-background-insets: 0; -fx-padding: 1;");

        listView.setCellFactory(param -> new ListCell<StatementBlock>() {
            @Override
            protected void updateItem(StatementBlock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Each cell only has a separator at the bottom, for insertion *after* the current item.
                    Region bottomSeparator = dragAndDropManager.createSeparator();
                    dragAndDropManager.addSeparatorDragHandlers(bottomSeparator, getIndex() + 1, item);

                    Node itemNode = item.getUINode(context);
                    VBox cellContainer = new VBox(itemNode, bottomSeparator);
                    setGraphic(cellContainer);
                    setText(null);
                    setPadding(Insets.EMPTY);
                }
            }
        });

        // A separator at the top of the whole body handles insertion at the very beginning (index 0).
        Region topBodySeparator = dragAndDropManager.createSeparator();
        dragAndDropManager.addSeparatorDragHandlers(topBodySeparator, 0, null);

        VBox bodyContainer = new VBox(topBodySeparator, listView);
        bodyContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-style: dashed;");

        return bodyContainer;
    }
}
