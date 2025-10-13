package com.botmaker.ui;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class BlockDragAndDropManager {

    public static final DataFormat ADDABLE_BLOCK_FORMAT = new DataFormat("application/x-java-addable-block");

    /**
     * Makes a UI node draggable, associating it with a specific type of AddableBlock.
     * @param node The node to make draggable (e.g., a Label in the palette).
     * @param blockType The type of block this node represents.
     */
    public void makeDraggable(Node node, AddableBlock blockType) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            // Store the enum name as a string.
            content.put(ADDABLE_BLOCK_FORMAT, blockType.name());
            db.setContent(content);
            System.out.println("Drag detected for: " + blockType.name()); // For debugging
            event.consume();
        });
    }

    /**
     * Makes a ListView within a BodyBlock a drop target for new blocks.
     * @param bodyBlock The logical BodyBlock.
     * @param listView The UI representation of the BodyBlock.
     */
    public void makeDroppable(BodyBlock bodyBlock, ListView<StatementBlock> listView) {
        listView.setOnDragOver(event -> {
            // Accept the drag only if it has the correct data format.
            if (event.getGestureSource() != listView && event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        listView.setOnDragExited(event -> {
            // This will be used later to remove the insertion marker
            System.out.println("Drag Exited BodyBlock: " + bodyBlock.getId());
            event.consume();
        });

        listView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                System.out.println("Dropped " + blockTypeName + " on BodyBlock " + bodyBlock.getId());

                // The next step would be to calculate the insertion index and
                // trigger an event to add the block to the AST.
                // For now, this is the end of the line.

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
