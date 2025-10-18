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

    private boolean isDarkMode = false;

    public Scene createScene() {
        blocksContainer = new VBox(10);
        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        HBox palette = createBlockPalette();
        palette.getStyleClass().add("palette");

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

        Button themeButton = new Button("Toggle Theme");
        HBox topBar = new HBox(10, themeButton);

        ScrollPane scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        VBox root = new VBox(10, topBar, palette, buttonBox, scrollPane, outputArea, statusLabel);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 600, 800);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());
        root.getStyleClass().add("light-theme");

        themeButton.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            root.getStyleClass().remove(isDarkMode ? "light-theme" : "dark-theme");
            root.getStyleClass().add(isDarkMode ? "dark-theme" : "light-theme");
        });

        return scene;
    }

    private HBox createBlockPalette() {
        HBox palette = new HBox(10);
        palette.setPadding(new Insets(5));

        for (AddableBlock blockType : AddableBlock.values()) {
            Label blockLabel = new Label(blockType.getDisplayName());
            blockLabel.getStyleClass().add("palette-block-label");
            blockLabel.getStyleClass().add("palette-" + blockType.name().toLowerCase() + "-label");
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
