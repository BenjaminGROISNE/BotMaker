package com.astblocks.demo;

import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class BlockUI extends VBox {
    private final CodeBlock codeBlock;
    private final HBox contentBox;
    private final VBox childrenContainer;

    public BlockUI(CodeBlock codeBlock, Consumer<BlockUI> completionRequester) {
        this.codeBlock = codeBlock;

        // Main container for this block's content
        contentBox = new HBox(5);
        contentBox.setPadding(new Insets(5));
        contentBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Container for any nested/child blocks
        childrenContainer = new VBox(5);
        childrenContainer.setPadding(new Insets(5, 5, 5, 20)); // Indent children

        // Style the block based on its type
        String style = "-fx-border-width: 1; -fx-border-radius: 5;";
        switch (codeBlock.getType()) {
            case "print":
                contentBox.setStyle(style + "-fx-background-color: #e6f7ff; -fx-border-color: #91d5ff;");
                contentBox.getChildren().add(new Text("Print:"));
                break;
            case "if":
                contentBox.setStyle(style + "-fx-background-color: #fffbe6; -fx-border-color: #ffe58f;");
                contentBox.getChildren().add(new Text("If"));
                break;
            case "condition_wrapper":
                // Transparent wrapper, no visible UI for itself
                break;
            case "then_wrapper":
                 // Transparent wrapper, no visible UI for itself
                break;
            case "variable_declaration":
                contentBox.setStyle(style + "-fx-background-color: #f6ffed; -fx-border-color: #b7eb8f;");
                contentBox.getChildren().add(new Text("New Variable:"));
                break;
            case "string_literal":
                contentBox.setStyle(style + "-fx-background-color: #fff0f6; -fx-border-color: #ffadd2;");
                contentBox.getChildren().add(new Text('"' + codeBlock.getCode() + '"'));
                break;
            case "simple_name":
                contentBox.setStyle(style + "-fx-background-color: #fafafa; -fx-border-color: #d9d9d9;");
                contentBox.getChildren().add(new Text(codeBlock.getCode()));
                break;
            default:
                contentBox.setStyle(style + "-fx-border-color: black;");
                break;
        }

        // Add context menu for completions if it's a clickable block
        if (isClickable()) {
            contentBox.setOnMouseClicked(e -> {
                if (completionRequester != null) {
                    completionRequester.accept(this);
                }
            });
        }

        // Only add the content box if it has a specific style (i.e., it's not a wrapper)
        if (!contentBox.getStyle().isEmpty()) {
            this.getChildren().add(contentBox);
        }
        this.getChildren().add(childrenContainer);
    }

    public void add_child_ui(BlockUI child) {
        if (codeBlock.getType().equals("if") && childrenContainer.getChildren().size() == 0) {
            // First child of an IF is the condition
            HBox conditionBox = new HBox(5, new Label("Condition:"), child);
            childrenContainer.getChildren().add(conditionBox);
        } else if (codeBlock.getType().equals("if") && childrenContainer.getChildren().size() == 1) {
            // Second child is the body
            VBox thenBox = new VBox(5, new Label("Then:"), child);
            childrenContainer.getChildren().add(thenBox);
        } else {
            childrenContainer.getChildren().add(child);
        }
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }

    private boolean isClickable() {
        // Define which blocks should trigger completions
        return codeBlock.getType().equals("simple_name") || codeBlock.getType().equals("print");
    }

    public void showCompletions(ContextMenu menu) {
        menu.show(this, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}