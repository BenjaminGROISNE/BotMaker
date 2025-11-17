package com.botmaker.ui;

import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.services.CodeEditorService;
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

/**
 * Manages the UI components.
 * Phase 3: Completely independent - no Main dependency!
 */
public class UIManager {

    // ============================================
    // PHASE 3: Remove Main dependency
    // ============================================
    private final BlockDragAndDropManager dragAndDropManager;
    private final EventBus eventBus;
    private final CodeEditorService codeEditorService;
    private final com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    private VBox blocksContainer;
    private Label statusLabel;
    private TextArea outputArea;
    private Button debugButton;
    private Button resumeButton;
    private ScrollPane scrollPane;
    private ListView<Diagnostic> errorListView;
    private TabPane bottomTabPane;
    private Tab terminalTab;
    private boolean isDarkMode = false;

    // ============================================
    // PHASE 3: CHANGED - No more Main parameter
    // ============================================
    public UIManager(BlockDragAndDropManager dragAndDropManager,
                     EventBus eventBus,
                     CodeEditorService codeEditorService,
                     com.botmaker.validation.DiagnosticsManager diagnosticsManager) {
        this.dragAndDropManager = dragAndDropManager;
        this.eventBus = eventBus;
        this.codeEditorService = codeEditorService;
        this.diagnosticsManager = diagnosticsManager;

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Subscribe to block updates
        eventBus.subscribe(
                CoreApplicationEvents.UIBlocksUpdatedEvent.class,
                this::handleBlocksUpdate,
                true
        );

        // Subscribe to output events
        eventBus.subscribe(
                CoreApplicationEvents.OutputAppendedEvent.class,
                event -> outputArea.appendText(event.getText()),
                true
        );

        eventBus.subscribe(
                CoreApplicationEvents.OutputClearedEvent.class,
                event -> outputArea.clear(),
                true
        );

        eventBus.subscribe(
                CoreApplicationEvents.OutputSetEvent.class,
                event -> outputArea.setText(event.getText()),
                true
        );

        // Subscribe to status messages
        eventBus.subscribe(
                CoreApplicationEvents.StatusMessageEvent.class,
                event -> statusLabel.setText(event.getMessage()),
                true
        );

        // Subscribe to diagnostics updates
        eventBus.subscribe(
                CoreApplicationEvents.DiagnosticsUpdatedEvent.class,
                event -> {
                    diagnosticsManager.processDiagnostics(event.getDiagnostics());
                    updateErrors(diagnosticsManager.getDiagnostics());
                    statusLabel.setText(diagnosticsManager.getErrorSummary());
                },
                true
        );

        // Subscribe to debug session events
        eventBus.subscribe(
                CoreApplicationEvents.DebugSessionStartedEvent.class,
                event -> onDebuggerStarted(),
                true
        );

        eventBus.subscribe(
                CoreApplicationEvents.DebugSessionPausedEvent.class,
                event -> onDebuggerPaused(),
                true
        );

        eventBus.subscribe(
                CoreApplicationEvents.DebugSessionResumedEvent.class,
                event -> onDebuggerResumed(),
                true
        );

        eventBus.subscribe(
                CoreApplicationEvents.DebugSessionFinishedEvent.class,
                event -> onDebuggerFinished(),
                true
        );
    }

    private void handleBlocksUpdate(CoreApplicationEvents.UIBlocksUpdatedEvent event) {
        blocksContainer.getChildren().clear();

        if (event.getRootBlock() != null) {
            CompletionContext context = codeEditorService.createCompletionContext();
            blocksContainer.getChildren().add(event.getRootBlock().getUINode(context));
        }
    }

    public Scene createScene() {
        blocksContainer = new VBox(10);
        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");

        outputArea = new TextArea();
        outputArea.setEditable(false);

        errorListView = new ListView<>();
        errorListView.setPlaceholder(new Label("No errors to display."));

        // ============================================
        // PHASE 3: CHANGED - Use diagnosticsManager directly (no Main)
        // ============================================
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
                        if (event.getClickCount() >= 1) {
                            diagnosticsManager.findBlockForDiagnostic(diagnostic)
                                    .ifPresent(this::scrollToBlock);
                        }
                    });
                }
            }

            private void scrollToBlock(CodeBlock block) {
                Node uiNode = block.getUINode();
                if (uiNode == null) return;

                final String blinkStyle = "error-block-blink";
                if (!uiNode.getStyleClass().contains(blinkStyle)) {
                    uiNode.getStyleClass().add(blinkStyle);
                    PauseTransition blinkOff = new PauseTransition(Duration.seconds(1));
                    blinkOff.setOnFinished(event -> uiNode.getStyleClass().remove(blinkStyle));
                    blinkOff.play();
                }

                uiNode.requestFocus();
                double containerHeight = blocksContainer.getBoundsInLocal().getHeight();
                double blockY = uiNode.getBoundsInParent().getMinY();
                double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();
                double vValue = blockY / (containerHeight - scrollPaneHeight);
                scrollPane.setVvalue(Math.max(0, Math.min(1, vValue)));
            }
        });

        bottomTabPane = new TabPane();
        terminalTab = new Tab("Terminal", outputArea);
        terminalTab.setClosable(false);
        Tab errorsTab = new Tab("Errors", errorListView);
        errorsTab.setClosable(false);
        bottomTabPane.getTabs().addAll(terminalTab, errorsTab);

        HBox palette = createBlockPalette();
        palette.getStyleClass().add("palette");

        Button compileButton = new Button("Compile");
        compileButton.setOnAction(e ->
                eventBus.publish(new CoreApplicationEvents.CompilationRequestedEvent())
        );

        Button runButton = new Button("Run");
        runButton.setOnAction(e -> {
            bottomTabPane.getSelectionModel().select(terminalTab);
            eventBus.publish(new CoreApplicationEvents.ExecutionRequestedEvent());
        });

        debugButton = new Button("Debug");
        debugButton.setOnAction(e ->
                eventBus.publish(new CoreApplicationEvents.DebugStartRequestedEvent())
        );

        resumeButton = new Button("Resume");
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e ->
                eventBus.publish(new CoreApplicationEvents.DebugResumeRequestedEvent())
        );

        HBox buttonBox = new HBox(10, compileButton, runButton, debugButton, resumeButton);

        Button themeButton = new Button("Toggle Theme");
        HBox topBar = new HBox(10, themeButton);

        scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(scrollPane, bottomTabPane);
        splitPane.setDividerPositions(0.7);

        VBox.setVgrow(splitPane, Priority.ALWAYS);

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

    private void updateErrors(List<Diagnostic> diagnostics) {
        if (diagnostics == null) {
            errorListView.getItems().clear();
        } else {
            errorListView.getItems().setAll(diagnostics);
        }
        if (diagnostics != null && !diagnostics.isEmpty()) {
            bottomTabPane.getSelectionModel().select(1);
        }
    }

    private void onDebuggerStarted() {
        debugButton.setDisable(true);
    }

    private void onDebuggerPaused() {
        resumeButton.setDisable(false);
    }

    private void onDebuggerResumed() {
        resumeButton.setDisable(true);
    }

    private void onDebuggerFinished() {
        debugButton.setDisable(false);
        resumeButton.setDisable(true);
    }
}