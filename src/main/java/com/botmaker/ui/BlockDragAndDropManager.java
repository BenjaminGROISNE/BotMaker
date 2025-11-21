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
    public static final DataFormat EXISTING_BLOCK_FORMAT = new DataFormat("application/x-java-existing-block");

    // Callbacks
    private Consumer<DropInfo> onDrop;
    private Consumer<MoveBlockInfo> onBlockMove;

    public BlockDragAndDropManager(Consumer<DropInfo> onDrop) {
        this.onDrop = onDrop;
    }

    public void setCallback(Consumer<DropInfo> onDrop) {
        this.onDrop = onDrop;
    }

    public void setMoveCallback(Consumer<MoveBlockInfo> onBlockMove) {
        this.onBlockMove = onBlockMove;
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
            content.put(ADDABLE_BLOCK_FORMAT, blockType.name());
            db.setContent(content);
            System.out.println("Drag detected for: " + blockType.name());
            event.consume();
        });
    }

    /**
     * Makes an existing block's UI node draggable for repositioning.
     * @param node The UI node of the block to make draggable.
     * @param block The StatementBlock instance being dragged.
     * @param sourceBody The BodyBlock containing this block.
     */
    public void makeBlockMovable(Node node, StatementBlock block, BodyBlock sourceBody) {
        node.setOnDragDetected(event -> {
            // Only start drag if not clicking on interactive elements
            if (event.getTarget() instanceof javafx.scene.control.Control) {
                return;
            }

            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(EXISTING_BLOCK_FORMAT, block.getId());
            db.setContent(content);

            // Visual feedback - make the block semi-transparent while dragging
            node.setOpacity(0.5);

            System.out.println("Dragging existing block: " + block.getDetails());
            event.consume();
        });

        // Reset opacity and release focus when drag is done
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            // DON'T consume - let the event propagate to restore normal mouse behavior
            // event.consume();

            // Multiple approaches to ensure drag is fully released
            javafx.application.Platform.runLater(() -> {
                // 1. Find and focus ScrollPane
                javafx.scene.Node current = node;
                javafx.scene.control.ScrollPane scrollPane = null;
                while (current != null) {
                    if (current instanceof javafx.scene.control.ScrollPane) {
                        scrollPane = (javafx.scene.control.ScrollPane) current;
                        break;
                    }
                    current = current.getParent();
                }

                if (scrollPane != null) {
                    // Make sure ScrollPane can receive focus
                    scrollPane.setFocusTraversable(true);
                    scrollPane.requestFocus();

                    // Also try to release any event filters
                    final javafx.scene.control.ScrollPane sp = scrollPane;
                    javafx.application.Platform.runLater(() -> {
                        sp.requestFocus();
                    });
                }
            });
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
     * Handles both adding new blocks and moving existing blocks.
     * @param separator The region to add handlers to.
     * @param targetBody The body where blocks will be inserted.
     * @param insertionIndex The index in the list where a drop should occur.
     * @param adjacentBlock The block next to the separator, for context (can be null).
     */
    public void addSeparatorDragHandlers(Region separator, BodyBlock targetBody, int insertionIndex, StatementBlock adjacentBlock) {
        String defaultColor = "transparent";
        String hoverColor = "#007bff"; // A distinct blue
        String moveHoverColor = "#28a745"; // Green for moving blocks

        separator.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + hoverColor + ";");
                String logMessage = "Hovering insertion point at index: " + insertionIndex;
                if (adjacentBlock != null) {
                    logMessage += " (next to: " + adjacentBlock.getDetails() + ")";
                }
                System.out.println(logMessage);
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + moveHoverColor + ";");
                System.out.println("Hovering to move block at index: " + insertionIndex);
            }
            event.consume();
        });

        separator.setOnDragExited(event -> {
            separator.setStyle("-fx-background-color: " + defaultColor);
            event.consume();
        });

        separator.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT) || db.hasContent(EXISTING_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        separator.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                // Adding a new block
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);

                if (onDrop != null) {
                    onDrop.accept(new DropInfo(type, targetBody, insertionIndex));
                    success = true;
                } else {
                    System.err.println("WARNING: onDrop callback not set yet!");
                }
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                // Moving an existing block
                String blockId = (String) db.getContent(EXISTING_BLOCK_FORMAT);

                if (onBlockMove != null) {
                    onBlockMove.accept(new MoveBlockInfo(blockId, targetBody, insertionIndex));
                    success = true;
                } else {
                    System.err.println("WARNING: onBlockMove callback not set yet!");
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addEmptyBodyDropHandlers(Region target, BodyBlock targetBody) {
        target.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT) ||
                    event.getDragboard().hasContent(EXISTING_BLOCK_FORMAT)) {
                target.getStyleClass().add("empty-body-drop-hover");
                System.out.println("Added hover class to: " + target.getClass().getSimpleName());
            }
            event.consume();
        });

        target.setOnDragExited(event -> {
            target.getStyleClass().remove("empty-body-drop-hover");
            event.consume();
        });

        target.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT) || db.hasContent(EXISTING_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        target.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                // Adding a new block
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);

                if (onDrop != null) {
                    onDrop.accept(new DropInfo(type, targetBody, 0)); // Always index 0 for empty body
                    success = true;
                } else {
                    System.err.println("WARNING: onDrop callback not set yet!");
                }
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                // Moving an existing block
                String blockId = (String) db.getContent(EXISTING_BLOCK_FORMAT);

                if (onBlockMove != null) {
                    onBlockMove.accept(new MoveBlockInfo(blockId, targetBody, 0));
                    success = true;
                } else {
                    System.err.println("WARNING: onBlockMove callback not set yet!");
                }
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
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                System.out.println("Cannot drop a Statement block ('" + blockTypeName + "') into an Expression slot.");
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}