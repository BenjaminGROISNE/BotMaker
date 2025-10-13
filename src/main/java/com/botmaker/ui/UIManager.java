package com.botmaker.ui;

import com.botmaker.Main;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UIManager {

    private final Main mainApp;
    private final BlockDragAndDropManager dragAndDropManager;
    private VBox blocksContainer;
    private Label statusLabel;
    private TextArea outputArea;
    private Button debugButton;
    private Button resumeButton;

    public UIManager(Main mainApp, BlockDragAndDropManager dragAndDropManager) {
        this.mainApp = mainApp;
        this.dragAndDropManager = dragAndDropManager;
    }

    public Scene createScene() {
        blocksContainer = new VBox(10);
        statusLabel = new Label("Ready");
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        HBox palette = createBlockPalette();

        Button compileButton = new Button("Compile");
        compileButton.setOnAction(e -> mainApp.compileCode());

        Button runButton = new Button("Run");
        runButton.setOnAction(e -> mainApp.runCode());

        debugButton = new Button("Debug");
        debugButton.setOnAction(e -> mainApp.startDebugging());

        resumeButton = new Button("Resume");
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> mainApp.resumeDebugging());

        HBox buttonBox = new HBox(10, compileButton, runButton, debugButton, resumeButton);

        ScrollPane scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        VBox root = new VBox(10, palette, buttonBox, scrollPane, outputArea, statusLabel);
        root.setPadding(new Insets(10));

        return new Scene(root, 600, 800);
    }

    private HBox createBlockPalette() {
        HBox palette = new HBox(10);
        palette.setPadding(new Insets(5));
        palette.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        for (AddableBlock blockType : AddableBlock.values()) {
            Label blockLabel = new Label(blockType.getDisplayName());
            blockLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 8; -fx-border-color: #c0c0c0; -fx-border-radius: 4; -fx-background-radius: 4;");
            dragAndDropManager.makeDraggable(blockLabel, blockType);
            palette.getChildren().add(blockLabel);
        }
        return palette;
    }

    public VBox getBlocksContainer() {
        return blocksContainer;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public void onDebuggerStarted() {
        debugButton.setDisable(true);
    }

    public void onDebuggerPaused() {
        resumeButton.setDisable(false);
    }

    public void onDebuggerResumed() {
        resumeButton.setDisable(true);
    }

    public void onDebuggerFinished() {
        debugButton.setDisable(false);
        resumeButton.setDisable(true);
    }
}
