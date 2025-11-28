package com.botmaker.ui;

import com.botmaker.ui.AddableBlock.BlockCategory;
import javafx.geometry.Pos;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaletteManager {

    private final BlockDragAndDropManager dragAndDropManager;

    public PaletteManager(BlockDragAndDropManager dragAndDropManager) {
        this.dragAndDropManager = dragAndDropManager;
    }

    public HBox createHorizontalPalette() {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER);
        // Remove padding here so buttons can touch edges if needed
        container.setStyle("-fx-padding: 0;");
        container.getStyleClass().add("palette-bar");

        Map<BlockCategory, List<AddableBlock>> grouped = Arrays.stream(AddableBlock.values())
                .collect(Collectors.groupingBy(AddableBlock::getCategory));

        BlockCategory[] order = {
                BlockCategory.OUTPUT,
                BlockCategory.INPUT,
                BlockCategory.VARIABLES,
                BlockCategory.FLOW,
                BlockCategory.LOOPS,
                BlockCategory.CONTROL,
                BlockCategory.FUNCTIONS,
                BlockCategory.UTILITY
        };

        for (BlockCategory category : order) {
            List<AddableBlock> blocks = grouped.get(category);
            if (blocks == null) continue;

            String categoryColor = getCategoryColor(category);

            MenuButton categoryMenu = new MenuButton();
            categoryMenu.getStyleClass().addAll("palette-category-btn", "palette-" + category.name().toLowerCase());

            // Make button stretch to fill toolbar height
            categoryMenu.setMaxHeight(Double.MAX_VALUE);
            // Allow button to shrink horizontally if needed
            categoryMenu.setMinWidth(Region.USE_PREF_SIZE);

            // Allow HBox to distribute extra space or shrink components
            HBox.setHgrow(categoryMenu, Priority.SOMETIMES);

            Label btnLabel = new Label(category.getLabel());
            btnLabel.setStyle(
                    "-fx-text-fill: white; " +
                            "-fx-font-family: 'Segoe UI', sans-serif; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px;"
            );
            categoryMenu.setGraphic(btnLabel);

            categoryMenu.setStyle(
                    "-fx-background-color: " + categoryColor + "; " +
                            "-fx-background-radius: 4; " + // Slightly smaller radius for "bar" look
                            "-fx-border-color: rgba(0,0,0,0.1); " +
                            "-fx-border-radius: 4; " +
                            "-fx-cursor: hand;"
                    // Removed fixed padding here to allow layout to control height
            );

            for (AddableBlock blockType : blocks) {
                Label blockLabel = new Label(blockType.getDisplayName());
                blockLabel.setPrefWidth(180);
                blockLabel.setMaxWidth(Double.MAX_VALUE);

                blockLabel.setStyle(
                        "-fx-background-color: " + categoryColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 8 12 8 12; " +
                                "-fx-background-radius: 2;"
                );

                dragAndDropManager.makeDraggable(blockLabel, blockType);

                CustomMenuItem item = new CustomMenuItem(blockLabel);
                item.setHideOnClick(false);
                categoryMenu.getItems().add(item);
            }

            container.getChildren().add(categoryMenu);
        }

        return container;
    }

    private String getCategoryColor(BlockCategory category) {
        return switch (category) {
            case OUTPUT -> "#3498DB";
            case INPUT -> "#9B59B6";
            case VARIABLES -> "#F39C12";
            case FLOW -> "#E67E22";
            case LOOPS -> "#2ECC71";
            case CONTROL -> "#E74C3C";
            case FUNCTIONS -> "#8E44AD";
            case UTILITY -> "#7F8C8D";
            default -> "#34495E";
        };
    }
}