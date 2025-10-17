package com.botmaker.ui;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

public class BlockDragAndDropManager {

    public static final DataFormat ADDABLE_BLOCK_FORMAT = new DataFormat("application/x-java-addable-block");
    private final Consumer<DropInfo> onDrop;

    public BlockDragAndDropManager(Consumer<DropInfo> onDrop) {
        this.onDrop = onDrop;
    }

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
     * Creates a thin, transparent region to act as a separator and drop target.
     */
    public Region createSeparator() {
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
    public void addSeparatorDragHandlers(Region separator, BodyBlock targetBody, int insertionIndex, StatementBlock adjacentBlock) {
        String defaultColor = "transparent";
        String hoverColor = "#007bff"; // A distinct blue

        separator.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT)) {
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
            separator.setStyle("-fx-background-color: " + defaultColor );
            event.consume();
        });

        separator.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(BlockDragAndDropManager.ADDABLE_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        separator.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);
                onDrop.accept(new DropInfo(type, targetBody, insertionIndex));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addExpressionDropHandlers(Region target) {
        String defaultStyle = "-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-style: dashed; -fx-min-width: 50; -fx-min-height: 25;";
        String hoverStyle = defaultStyle + "-fx-border-color: #007bff;"; // Highlight with blue

        target.setStyle(defaultStyle);

        target.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) {
                target.setStyle(hoverStyle);
                System.out.println("Hovering expression slot.");
            }
            event.consume();
        });

        target.setOnDragExited(event -> {
            target.setStyle(defaultStyle);
            event.consume();
        });

        target.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        target.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                // For now, we just acknowledge the drop and show a message.
                // In the future, we would need a different DataFormat for expressions.
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                System.out.println("Cannot drop a Statement block ('" + blockTypeName + "') into an Expression slot.");
                // We'll still mark it as a "successful" drop to finalize the gesture.
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}