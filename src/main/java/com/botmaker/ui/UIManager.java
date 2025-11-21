package com.botmaker.ui;

import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.services.CodeEditorService;
import com.botmaker.ui.AddableBlock.BlockCategory;
import com.botmaker.validation.DiagnosticsManager;
import com.botmaker.validation.ErrorTranslator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UIManager {

    private final BlockDragAndDropManager dragAndDropManager;
    private final EventBus eventBus;
    private final CodeEditorService codeEditorService;
    private final DiagnosticsManager diagnosticsManager;
    private final Stage primaryStage;

    private VBox blocksContainer;
    private Label statusLabel;
    private TextArea outputArea;
    private ListView<Diagnostic> errorListView;
    private TabPane bottomTabPane;
    private Tab terminalTab;
    private ScrollPane scrollPane;
    private MenuBarManager menuBarManager;
    private Consumer<Void> onSelectProject;

    // NEW: Event Log Manager
    private EventLogManager eventLogManager;

    // Controls
    private Button debugButton;
    private Button runButton;
    private Button unifiedStopButton;
    private Button stepOverButton;
    private Button continueButton;
    private Button undoButton;
    private Button redoButton;

    // Application State Tracking
    private enum AppState { IDLE, RUNNING, DEBUGGING }
    private AppState currentAppState = AppState.IDLE;

    public UIManager(BlockDragAndDropManager dragAndDropManager,
                     EventBus eventBus,
                     CodeEditorService codeEditorService,
                     DiagnosticsManager diagnosticsManager,
                     Stage primaryStage) {
        this.dragAndDropManager = dragAndDropManager;
        this.eventBus = eventBus;
        this.codeEditorService = codeEditorService;
        this.diagnosticsManager = diagnosticsManager;
        this.primaryStage = primaryStage;

        // Initialize the EventLogManager immediately
        this.eventLogManager = new EventLogManager(eventBus);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.UIBlocksUpdatedEvent.class, this::handleBlocksUpdate, true);
        eventBus.subscribe(CoreApplicationEvents.OutputAppendedEvent.class, event -> {
            // Protect the UI Component from holding too much text
            if (outputArea.getText().length() > 10_000) {
                // If too long, cut the top half
                String current = outputArea.getText();
                outputArea.setText("[...Old Output Trimmed...]\n" + current.substring(current.length() - 5000) + event.getText());
                outputArea.positionCaret(outputArea.getLength());
            } else {
                outputArea.appendText(event.getText());
            }
        }, true);

        eventBus.subscribe(CoreApplicationEvents.OutputClearedEvent.class, event -> outputArea.clear(), true);
        eventBus.subscribe(CoreApplicationEvents.OutputSetEvent.class, event -> outputArea.setText(event.getText()), true);
        eventBus.subscribe(CoreApplicationEvents.StatusMessageEvent.class, event -> statusLabel.setText(event.getMessage()), true);

        eventBus.subscribe(CoreApplicationEvents.DiagnosticsUpdatedEvent.class, event -> {
            diagnosticsManager.processDiagnostics(event.getDiagnostics());
            updateErrors(diagnosticsManager.getDiagnostics());
            statusLabel.setText(diagnosticsManager.getErrorSummary());
        }, true);


        // State Management Handlers
        eventBus.subscribe(CoreApplicationEvents.ProgramStartedEvent.class, e -> setAppState(AppState.RUNNING), true);
        eventBus.subscribe(CoreApplicationEvents.ProgramStoppedEvent.class, e -> setAppState(AppState.IDLE), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionStartedEvent.class, e -> setAppState(AppState.DEBUGGING), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionFinishedEvent.class, e -> setAppState(AppState.IDLE), true);

        // Debugging specific controls
        eventBus.subscribe(CoreApplicationEvents.DebugSessionPausedEvent.class, e -> updateDebugControls(true), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionResumedEvent.class, e -> updateDebugControls(false), true);

        eventBus.subscribe(CoreApplicationEvents.HistoryStateChangedEvent.class, event -> {
            Platform.runLater(() -> {
                if (undoButton != null) undoButton.setDisable(!event.canUndo());
                if (redoButton != null) redoButton.setDisable(!event.canRedo());
            });
        }, true);
    }

    private void setAppState(AppState state) {
        this.currentAppState = state;
        updateToolbarState();
    }

    private void updateToolbarState() {
        boolean isBusy = (currentAppState != AppState.IDLE);

        runButton.setDisable(isBusy);
        debugButton.setDisable(isBusy);
        unifiedStopButton.setDisable(!isBusy);

        if (currentAppState == AppState.DEBUGGING) {
            unifiedStopButton.setText("Stop Debugging ⏹");
            unifiedStopButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
            bottomTabPane.getSelectionModel().select(terminalTab);
        } else if (currentAppState == AppState.RUNNING) {
            unifiedStopButton.setText("Stop Run ⏹");
            unifiedStopButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
            bottomTabPane.getSelectionModel().select(terminalTab);

            stepOverButton.setDisable(true);
            continueButton.setDisable(true);
        } else {
            unifiedStopButton.setText("Stop ⏹");
            unifiedStopButton.setStyle("");
            stepOverButton.setDisable(true);
            continueButton.setDisable(true);
        }
    }

    private void updateDebugControls(boolean isPaused) {
        if (currentAppState == AppState.DEBUGGING) {
            stepOverButton.setDisable(!isPaused);
            continueButton.setDisable(!isPaused);
        }
    }

    private void handleBlocksUpdate(CoreApplicationEvents.UIBlocksUpdatedEvent event) {
        blocksContainer.getChildren().clear();
        if (event.getRootBlock() != null) {
            CompletionContext context = codeEditorService.createCompletionContext();
            Node rootNode = event.getRootBlock().getUINode(context);

            rootNode.addEventHandler(BlockEvent.BreakpointToggleEvent.TOGGLE_BREAKPOINT, e -> {
                eventBus.publish(new CoreApplicationEvents.BreakpointToggledEvent(e.getBlock(), e.isEnabled()));
            });

            blocksContainer.getChildren().add(rootNode);
        }
    }

    public Scene createScene() {
        menuBarManager = new MenuBarManager(primaryStage);
        menuBarManager.setEventBus(eventBus);
        menuBarManager.setOnSelectProject(v -> { if (onSelectProject != null) onSelectProject.accept(null); });

        blocksContainer = new VBox(10);
        blocksContainer.getStyleClass().add("blocks-canvas");

        scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("code-scroll-pane");

        Accordion paletteAccordion = createCategorizedPalette();
        VBox paletteContainer = new VBox(paletteAccordion);
        paletteContainer.setPrefWidth(220);
        paletteContainer.getStyleClass().add("palette-sidebar");

        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");

        // Terminal Area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("console-area");

        // Error List Area
        errorListView = new ListView<>();
        configureErrorList(errorListView);

        // NEW: Event Log Area
        ListView<String> eventView = eventLogManager.getView();

        // Tabs
        bottomTabPane = new TabPane();
        terminalTab = new Tab("Terminal", outputArea);
        terminalTab.setClosable(false);

        Tab errorsTab = new Tab("Errors", errorListView);
        errorsTab.setClosable(false);

        // New Event Log Tab
        Tab eventsTab = new Tab("Event Log", eventView);
        eventsTab.setClosable(false);

        bottomTabPane.getTabs().addAll(terminalTab, errorsTab, eventsTab);

        HBox toolBar = createToolBar();

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(scrollPane, bottomTabPane);
        verticalSplit.setDividerPositions(0.75);
        VBox.setVgrow(verticalSplit, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(toolBar);
        mainLayout.setLeft(paletteContainer);
        mainLayout.setCenter(verticalSplit);
        mainLayout.setBottom(statusLabel);

        VBox root = new VBox(menuBarManager.getMenuBar(), mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        root.getStyleClass().add("light-theme");

        // Cleanup executor on close
        primaryStage.setOnHidden(e -> eventLogManager.shutdown());

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());

        return scene;
    }

    private HBox createToolBar() {
        undoButton = new Button("Undo");
        undoButton.setDisable(true);
        undoButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.UndoRequestedEvent()));

        redoButton = new Button("Redo");
        redoButton.setDisable(true);
        redoButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.RedoRequestedEvent()));

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Button compileButton = new Button("Compile");
        compileButton.getStyleClass().add("toolbar-btn");
        compileButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.CompilationRequestedEvent()));

        runButton = new Button("Run ▶");
        runButton.getStyleClass().addAll("toolbar-btn", "btn-run");
        runButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.ExecutionRequestedEvent()));

        debugButton = new Button("Debug 🐞");
        debugButton.getStyleClass().addAll("toolbar-btn", "btn-debug");
        debugButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugStartRequestedEvent()));

        unifiedStopButton = new Button("Stop ⏹");
        unifiedStopButton.getStyleClass().addAll("toolbar-btn", "btn-stop");
        unifiedStopButton.setDisable(true);
        unifiedStopButton.setOnAction(e -> {
            if (currentAppState == AppState.RUNNING) {
                eventBus.publish(new CoreApplicationEvents.StopRunRequestedEvent());
            } else if (currentAppState == AppState.DEBUGGING) {
                eventBus.publish(new CoreApplicationEvents.DebugStopRequestedEvent());
            }
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        stepOverButton = new Button("Step ⤵");
        stepOverButton.setDisable(true);
        stepOverButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugStepOverRequestedEvent()));

        continueButton = new Button("Continue ⏩");
        continueButton.setDisable(true);
        continueButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugContinueRequestedEvent()));

        HBox toolbar = new HBox(10,
                undoButton, redoButton,
                spacer1,
                compileButton, runButton, debugButton, unifiedStopButton,
                spacer2,
                stepOverButton, continueButton
        );
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("main-toolbar");
        return toolbar;
    }

    private Accordion createCategorizedPalette() {
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

    private void configureErrorList(ListView<Diagnostic> lv) {
        lv.setPlaceholder(new Label("No errors to display."));
        lv.setCellFactory(list -> new ListCell<>() {
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
                    if (diagnostic.getSeverity() == DiagnosticSeverity.Error) getStyleClass().add("error-cell");
                    else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) getStyleClass().add("warning-cell");

                    setOnMouseClicked(event -> {
                        if (event.getClickCount() >= 1) {
                            diagnosticsManager.findBlockForDiagnostic(diagnostic).ifPresent(block -> {
                                Node uiNode = block.getUINode();
                                if (uiNode != null) uiNode.requestFocus();
                            });
                        }
                    });
                }
            }
        });
    }

    private void updateErrors(List<Diagnostic> diagnostics) {
        if (diagnostics == null) errorListView.getItems().clear();
        else errorListView.getItems().setAll(diagnostics);
        if (diagnostics != null && !diagnostics.isEmpty()) bottomTabPane.getSelectionModel().select(1);
    }

    public void setOnSelectProject(Consumer<Void> callback) { this.onSelectProject = callback; }
}