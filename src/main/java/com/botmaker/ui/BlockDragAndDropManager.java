package com.botmaker.ui;

import com.botmaker.blocks.ClassBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.state.ApplicationState;
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

    private Consumer<DropInfo> onDrop;
    private Consumer<MoveBlockInfo> onBlockMove;
    private final ApplicationState state;

    public BlockDragAndDropManager(ApplicationState state) {
        this.state = state;
    }

    public void setCallback(Consumer<DropInfo> onDrop) {
        this.onDrop = onDrop;
    }

    public void setMoveCallback(Consumer<MoveBlockInfo> onBlockMove) {
        this.onBlockMove = onBlockMove;
    }

    public void makeDraggable(Node node, AddableBlock blockType) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.put(ADDABLE_BLOCK_FORMAT, blockType.name());
            db.setContent(content);
            node.setOpacity(0.5);
            event.consume();
        });
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    public void makeBlockMovable(Node node, StatementBlock block, BodyBlock sourceBody) {
        // Check for read-only status
        if (block.isReadOnly()) return;

        node.setOnDragDetected(event -> {
            if (event.getTarget() instanceof javafx.scene.control.Control) return;
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(EXISTING_BLOCK_FORMAT, block.getId());
            db.setContent(content);
            node.setOpacity(0.5);
            event.consume();
        });
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            event.consume();
        });
    }

    // ... (Keep the rest of the file exactly as it was in Step 1)

    public Region createSeparator() {
        Region separator = new Region();
        separator.setMinHeight(8);
        separator.setStyle("-fx-background-color: transparent;");
        return separator;
    }

    public void addSeparatorDragHandlers(Region separator, BodyBlock targetBody, int insertionIndex, StatementBlock adjacentBlock) {
        // If the target body belongs to a read-only block, disable drop
        if (targetBody.isReadOnly()) return;

        String defaultColor = "transparent";
        String hoverColor = "#007bff";
        String moveHoverColor = "#28a745";

        separator.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) separator.setStyle("-fx-background-color: " + hoverColor + ";");
            else if (db.hasContent(EXISTING_BLOCK_FORMAT)) separator.setStyle("-fx-background-color: " + moveHoverColor + ";");
            event.consume();
        });

        separator.setOnDragExited(event -> {
            separator.setStyle("-fx-background-color: " + defaultColor);
            event.consume();
        });

        separator.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) event.acceptTransferModes(TransferMode.COPY);
            else if (event.getDragboard().hasContent(EXISTING_BLOCK_FORMAT)) event.acceptTransferModes(TransferMode.MOVE);
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
        // If class is read-only, disable drop
        if (targetClass.isReadOnly()) return;

        String hoverColor = "#007bff";
        String moveHoverColor = "#28a745";

        separator.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String typeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                try {
                    AddableBlock type = AddableBlock.valueOf(typeName);
                    if (type == AddableBlock.METHOD_DECLARATION || type == AddableBlock.DECLARE_ENUM) {
                        separator.setStyle("-fx-background-color: " + hoverColor + "; -fx-min-height: 10;");
                    }
                } catch (IllegalArgumentException ignored) {}
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                separator.setStyle("-fx-background-color: " + moveHoverColor + "; -fx-min-height: 10;");
            }
            event.consume();
        });

        separator.setOnDragExited(event -> {
            separator.setStyle("-fx-background-color: transparent;");
            event.consume();
        });

        separator.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(ADDABLE_BLOCK_FORMAT)) {
                String blockTypeName = (String) db.getContent(ADDABLE_BLOCK_FORMAT);
                try {
                    AddableBlock type = AddableBlock.valueOf(blockTypeName);
                    if (type == AddableBlock.METHOD_DECLARATION || type == AddableBlock.DECLARE_ENUM) {
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                } catch (IllegalArgumentException ignored) {}
            } else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
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
                    onDrop.accept(new DropInfo(type, null, insertionIndex, targetClass));
                    success = true;
                }
            }
            else if (db.hasContent(EXISTING_BLOCK_FORMAT)) {
                String blockId = (String) db.getContent(EXISTING_BLOCK_FORMAT);
                if (onBlockMove != null) {
                    onBlockMove.accept(new MoveBlockInfo(blockId, null, targetClass, insertionIndex));
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addEmptyBodyDropHandlers(Region target, BodyBlock targetBody) {
        // Disable for read-only bodies
        if (targetBody.isReadOnly()) return;

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

        target.setOnDragEntered(e -> {
            if (e.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT) || e.getDragboard().hasContent(EXISTING_BLOCK_FORMAT))
                target.setStyle("-fx-background-color: rgba(0, 123, 255, 0.2); -fx-border-color: #007bff; -fx-border-style: dashed;");
            e.consume();
        });

        target.setOnDragExited(e -> {
            target.setStyle(""); // Reset to default CSS
            target.getStyleClass().add("empty-body-placeholder"); // Re-apply class just in case
            e.consume();
        });

        target.setOnDragOver(e -> {
            if (e.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT) || e.getDragboard().hasContent(EXISTING_BLOCK_FORMAT))
                e.acceptTransferModes(TransferMode.ANY);
            e.consume();
        });
    }

    public void addExpressionDropHandlers(Region target) {
        String defaultStyle = "-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-style: dashed; -fx-min-width: 50; -fx-min-height: 25;";
        String hoverStyle = defaultStyle + "-fx-border-color: #007bff;";
        target.setStyle(defaultStyle);
        target.setOnDragEntered(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) target.setStyle(hoverStyle);
            event.consume();
        });
        target.setOnDragExited(event -> {
            target.setStyle(defaultStyle);
            event.consume();
        });
        target.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT)) event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });
        target.setOnDragDropped(event -> {
            boolean success = event.getDragboard().hasContent(ADDABLE_BLOCK_FORMAT);
            event.setDropCompleted(success);
            event.consume();
        });
    }
}