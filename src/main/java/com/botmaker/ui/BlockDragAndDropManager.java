package com.botmaker.ui;

import com.botmaker.blocks.ClassBlock;
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
     * Makes a UI node draggable (Palette Items).
     * Added visual feedback (Opacity).
     */
    public void makeDraggable(Node node, AddableBlock blockType) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.put(ADDABLE_BLOCK_FORMAT, blockType.name());
            db.setContent(content);

            // Visual feedback: reduce opacity to show it's being dragged
            node.setOpacity(0.5);

            System.out.println("Drag detected for: " + blockType.name());
            event.consume();
        });

        // Reset opacity when drag finishes (success or cancel)
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    /**
     * Makes an existing block's UI node draggable for repositioning.
     */
    public void makeBlockMovable(Node node, StatementBlock block, BodyBlock sourceBody) {
        node.setOnDragDetected(event -> {
            if (event.getTarget() instanceof javafx.scene.control.Control) {
                return;
            }

            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(EXISTING_BLOCK_FORMAT, block.getId());
            db.setContent(content);

            node.setOpacity(0.5);

            System.out.println("Dragging existing block: " + block.getDetails());
            event.consume();
        });

        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            javafx.application.Platform.runLater(() -> {
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
                    scrollPane.setFocusTraversable(true);
                    scrollPane.requestFocus();
                }
            });
        });
    }

    public Region createSeparator() {
        Region separator = new Region();
        separator.setMinHeight(8);
        separator.setStyle("-fx-background-color: transparent;");
        return separator;
    }

    public void addSeparatorDragHandlers(Region separator, BodyBlock targetBody, int insertionIndex, StatementBlock adjacentBlock) {
        String defaultColor = "transparent";
        String hoverColor = "#007bff";
        String moveHoverColor = "#28a745";

        separator.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + hoverColor + ";");
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + moveHoverColor + ";");
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
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);
                if (onDrop != null) {
                    onDrop.accept(new DropInfo(type, targetBody, insertionIndex));
                    success = true;
                }
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                String blockId = (String) db.getContent(EXISTING_BLOCK_FORMAT);
                if (onBlockMove != null) {
                    onBlockMove.accept(new MoveBlockInfo(blockId, targetBody, insertionIndex));
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addClassMemberDropHandlers(Region separator, ClassBlock targetClass, int insertionIndex) {

        separator.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);

                // Allow Methods OR Enums
                if (type == AddableBlock.METHOD_DECLARATION || type == AddableBlock.DECLARE_ENUM) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
            event.consume();
        });

        separator.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);

                if ((type == AddableBlock.METHOD_DECLARATION || type == AddableBlock.DECLARE_ENUM) && onDrop != null) {
                    // We reuse DropInfo but check type in CodeEditorService
                    onDrop.accept(new DropInfo(type, null, insertionIndex, targetClass));
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addEmptyBodyDropHandlers(Region target, BodyBlock targetBody) {
        target.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT) || event.getDragboard().hasContent(EXISTING_BLOCK_FORMAT)) {
                target.getStyleClass().add("empty-body-drop-hover");
            }
            event.consume();
        });

        target.setOnDragExited(event -> {
            target.getStyleClass().remove("empty-body-drop-hover");
            event.consume();
        });

        target.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT) || event.getDragboard().hasContent(EXISTING_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        target.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                AddableBlock type = AddableBlock.valueOf(blockTypeName);
                if (onDrop != null) {
                    onDrop.accept(new DropInfo(type, targetBody, 0));
                    success = true;
                }
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                String blockId = (String) db.getContent(EXISTING_BLOCK_FORMAT);
                if (onBlockMove != null) {
                    onBlockMove.accept(new MoveBlockInfo(blockId, targetBody, 0));
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addExpressionDropHandlers(Region target) {
        String defaultStyle = "-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-style: dashed; -fx-min-width: 50; -fx-min-height: 25;";
        String hoverStyle = defaultStyle + "-fx-border-color: #007bff;";

        target.setStyle(defaultStyle);

        target.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) {
                target.setStyle(hoverStyle);
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
                success = true;
                // Handled by expression replacement logic usually, but here we just accept it
                // to indicate valid drop target visually
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}