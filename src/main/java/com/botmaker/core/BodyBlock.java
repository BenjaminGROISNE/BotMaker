package com.botmaker.core;

import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
        // Remove the default border so it doesn't clash with our new layout
        listView.setStyle("-fx-background-insets: 0; -fx-padding: 1;");

        // Tell the ListView how to render each statement
        listView.setCellFactory(param -> new ListCell<StatementBlock>() {
            @Override
            protected void updateItem(StatementBlock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Region topSeparator = createSeparator();
                    Region bottomSeparator = createSeparator();

                    // Pass the item to get its details for logging
                    addSeparatorDragHandlers(topSeparator, getIndex(), item);
                    addSeparatorDragHandlers(bottomSeparator, getIndex() + 1, item);

                    Node itemNode = item.getUINode(context);
                    VBox cellContainer = new VBox(topSeparator, itemNode, bottomSeparator);
                    setGraphic(cellContainer);
                }
            }
        });

        // --- Container for the whole BodyBlock with its own separators ---
        Region topBodySeparator = createSeparator();
        Region bottomBodySeparator = createSeparator();

        // These separators are not adjacent to a specific item, so pass null
        addSeparatorDragHandlers(topBodySeparator, 0, null);
        addSeparatorDragHandlers(bottomBodySeparator, statements.size(), null);

        VBox bodyContainer = new VBox(topBodySeparator, listView, bottomBodySeparator);
        bodyContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-style: dashed;");

        return bodyContainer;
    }

    /**
     * Creates a thin, transparent region to act as a separator and drop target.
     */
    private Region createSeparator() {
        Region separator = new Region();
        separator.setMinHeight(8);
        separator.setStyle("-fx-background-color: transparent;");
        return separator;
    }

    /**
     * Adds all necessary drag-and-drop event handlers to a separator region.
     * @param separator The region to add handlers to.
     * @param insertionIndex The index in the list where a drop should occur.
     * @param adjacentBlock The block next to the separator, for context (can be null).
     */
    private void addSeparatorDragHandlers(Region separator, int insertionIndex, StatementBlock adjacentBlock) {
        String defaultColor = "transparent";
        String hoverColor = "#007bff"; // A distinct blue

        separator.setOnDragEntered(event -> {
            if (dragAndDropManager != null && event.getDragboard().hasContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + hoverColor + ";");
                String logMessage = "Hovering insertion point at index: " + insertionIndex;
                if (adjacentBlock != null) {
                    logMessage += " (next to: " + adjacentBlock.getDetails() + ")";
                }
                System.out.println(logMessage);
            }
            event.consume();
        });

        separator.setOnDragExited(event -> {
            separator.setStyle("-fx-background-color: " + defaultColor + ";");
            event.consume();
        });

        separator.setOnDragOver(event -> {
            if (dragAndDropManager != null && event.getDragboard().hasContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        separator.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT);
                System.out.println("Dropped " + blockTypeName + " at index " + insertionIndex);
                // In the future, this is where we would trigger the AST modification
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
