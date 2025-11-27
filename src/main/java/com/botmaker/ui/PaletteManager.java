package com.botmaker.ui;

import com.botmaker.ui.AddableBlock.BlockCategory;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaletteManager {

    private final BlockDragAndDropManager dragAndDropManager;

    public PaletteManager(BlockDragAndDropManager dragAndDropManager) {
        this.dragAndDropManager = dragAndDropManager;
    }

    public Accordion createCategorizedPalette() {
        Accordion accordion = new Accordion();
        Map<BlockCategory, List<AddableBlock>> grouped = Arrays.stream(AddableBlock.values())
                .collect(Collectors.groupingBy(AddableBlock::getCategory));

        BlockCategory[] order = {
                BlockCategory.OUTPUT, BlockCategory.INPUT, BlockCategory.VARIABLES,
                BlockCategory.FLOW, BlockCategory.LOOPS, BlockCategory.CONTROL, BlockCategory.UTILITY
        };

        for (BlockCategory category : order) {
            List<AddableBlock> blocks = grouped.get(category);
            if (blocks == null) continue;

            VBox content = new VBox(8);
            content.setPadding(new Insets(10));

            for (AddableBlock blockType : blocks) {
                Label blockLabel = new Label(blockType.getDisplayName());
                blockLabel.setMaxWidth(Double.MAX_VALUE);
                blockLabel.getStyleClass().addAll("palette-item", "palette-" + category.name().toLowerCase());

                // --- FIX: Force Black Text Color ---
                blockLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

                dragAndDropManager.makeDraggable(blockLabel, blockType);
                content.getChildren().add(blockLabel);
            }

            TitledPane pane = new TitledPane(category.getLabel(), content);
            pane.getStyleClass().add("palette-pane");
            accordion.getPanes().add(pane);
        }
        if (!accordion.getPanes().isEmpty()) accordion.setExpandedPane(accordion.getPanes().get(0));
        return accordion;
    }
}