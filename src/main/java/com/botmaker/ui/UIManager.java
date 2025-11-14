package com.botmaker.ui;

import com.botmaker.Main;
import com.botmaker.core.CodeBlock;
import com.botmaker.validation.ErrorTranslator;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.List;

public class UIManager {

    private final Main mainApp;
    private final BlockDragAndDropManager dragAndDropManager;
    private VBox blocksContainer;
    private Label statusLabel;
    private TextArea outputArea;
    private Button debugButton;
    private Button resumeButton;
    private ScrollPane scrollPane;
    private ListView<Diagnostic> errorListView;
    private TabPane bottomTabPane;


    public UIManager(Main mainApp, BlockDragAndDropManager dragAndDropManager) {
        this.mainApp = mainApp;
        this.dragAndDropManager = dragAndDropManager;
    }

    private boolean isDarkMode = false;

    public Scene createScene() {
        blocksContainer = new VBox(10);
        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");

        // Initialize the components for the tabs
        outputArea = new TextArea();
        outputArea.setEditable(false);

        errorListView = new ListView<>();
        errorListView.setPlaceholder(new Label("No errors to display."));

        errorListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Diagnostic diagnostic, boolean empty) {
                super.updateItem(diagnostic, empty);
                getStyleClass().removeAll("error-cell", "warning-cell");

                if (empty || diagnostic == null) {
                    setText(null);
                    setOnMouseClicked(null);
                } else {
                    String message = ErrorTranslator.getShortSummary(diagnostic);
                    int line = diagnostic.getRange().getStart().getLine() + 1;
                    setText(String.format("Line %d: %s", line, message));

                    if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
                        getStyleClass().add("error-cell");
                    } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
                        getStyleClass().add("warning-cell");
                    }

                    setOnMouseClicked(event -> {
                        if (event.getClickCount() >= 1) { // Use single-click
                            mainApp.getDiagnosticsManager().findBlockForDiagnostic(diagnostic)
                                    .ifPresent(this::scrollToBlock);
                        }
                    });
                }
            }

            private void scrollToBlock(CodeBlock block) {
                Node uiNode = block.getUINode();
                if (uiNode == null) return;

                // --- Blinking Animation ---
                final String blinkStyle = "error-block-blink";
                if (!uiNode.getStyleClass().contains(blinkStyle)) { // Prevent multiple animations
                    uiNode.getStyleClass().add(blinkStyle);
                    PauseTransition blinkOff = new PauseTransition(Duration.seconds(1));
                    blinkOff.setOnFinished(event -> uiNode.getStyleClass().remove(blinkStyle));
                    blinkOff.play();
                }
                // --- End Animation ---

                uiNode.requestFocus();
                double containerHeight = blocksContainer.getBoundsInLocal().getHeight();
                double blockY = uiNode.getBoundsInParent().getMinY();
                double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();
                double vValue = blockY / (containerHeight - scrollPaneHeight);
                scrollPane.setVvalue(Math.max(0, Math.min(1, vValue)));
            }
        });

        // Create the TabPane
        bottomTabPane = new TabPane();
        Tab terminalTab = new Tab("Terminal", outputArea);
        terminalTab.setClosable(false);
        Tab errorsTab = new Tab("Errors", errorListView);
errorsTab.setClosable(false);
        bottomTabPane.getTabs().addAll(terminalTab, errorsTab);


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

        scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);

        // Create a SplitPane for resizable vertical layout
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(scrollPane, bottomTabPane);
        splitPane.setDividerPositions(0.7); // 70% for code blocks, 30% for tabs

        VBox.setVgrow(splitPane, Priority.ALWAYS); // Make the SplitPane grow to fill space

        VBox root = new VBox(10, topBar, palette, buttonBox, splitPane, statusLabel);
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

    public void updateErrors(List<Diagnostic> diagnostics) {
        if (diagnostics == null) {
            errorListView.getItems().clear();
        } else {
            errorListView.getItems().setAll(diagnostics);
        }
        // If there are errors, automatically switch to the errors tab
        if (diagnostics != null && !diagnostics.isEmpty()) {
            bottomTabPane.getSelectionModel().select(1); // Select the second tab (Errors)
        }
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