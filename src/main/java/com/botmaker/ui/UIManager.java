package com.botmaker.ui;

import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.services.CodeEditorService;
import com.botmaker.ui.AddableBlock.BlockCategory;
import com.botmaker.validation.DiagnosticsManager;
import com.botmaker.validation.ErrorTranslator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UIManager {

    // ... (Keep existing fields: dragAndDropManager, eventBus, etc.) ...
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
    private boolean isDarkMode = false;
    private MenuBarManager menuBarManager;
    private Consumer<Void> onSelectProject;

    // Controls
    private Button debugButton;
    private Button stepOverButton;
    private Button continueButton;
    private Button stopDebugButton;
    private Button undoButton;
    private Button redoButton;

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

        setupEventHandlers();
    }

    // ... (Keep setupEventHandlers and handleBlocksUpdate exactly as they were) ...
    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.UIBlocksUpdatedEvent.class, this::handleBlocksUpdate, true);
        eventBus.subscribe(CoreApplicationEvents.OutputAppendedEvent.class, event -> outputArea.appendText(event.getText()), true);
        eventBus.subscribe(CoreApplicationEvents.OutputClearedEvent.class, event -> outputArea.clear(), true);
        eventBus.subscribe(CoreApplicationEvents.OutputSetEvent.class, event -> outputArea.setText(event.getText()), true);
        eventBus.subscribe(CoreApplicationEvents.StatusMessageEvent.class, event -> statusLabel.setText(event.getMessage()), true);

        eventBus.subscribe(CoreApplicationEvents.DiagnosticsUpdatedEvent.class, event -> {
            diagnosticsManager.processDiagnostics(event.getDiagnostics());
            updateErrors(diagnosticsManager.getDiagnostics());
            statusLabel.setText(diagnosticsManager.getErrorSummary());
        }, true);

        // Debug Session UI States
        eventBus.subscribe(CoreApplicationEvents.DebugSessionStartedEvent.class, event -> onDebuggerStarted(), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionPausedEvent.class, event -> onDebuggerPaused(), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionResumedEvent.class, event -> onDebuggerResumed(), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionFinishedEvent.class, event -> onDebuggerFinished(), true);

        eventBus.subscribe(CoreApplicationEvents.HistoryStateChangedEvent.class, event -> {
            Platform.runLater(() -> {
                if (undoButton != null) undoButton.setDisable(!event.canUndo());
                if (redoButton != null) redoButton.setDisable(!event.canRedo());
            });
        }, true);
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
        if (scrollPane != null) {
            scrollPane.requestFocus();
        }
    }

    // --- UPDATED UI CREATION ---
    public Scene createScene() {
        menuBarManager = new MenuBarManager(primaryStage);
        menuBarManager.setEventBus(eventBus);
        menuBarManager.setOnSelectProject(v -> { if (onSelectProject != null) onSelectProject.accept(null); });

        // 1. Main Block Canvas
        blocksContainer = new VBox(10);
        blocksContainer.getStyleClass().add("blocks-canvas");

        scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("code-scroll-pane");

        // 2. Categorized Palette (Left Sidebar)
        Accordion paletteAccordion = createCategorizedPalette();
        VBox paletteContainer = new VBox(paletteAccordion);
        paletteContainer.setPrefWidth(220);
        paletteContainer.getStyleClass().add("palette-sidebar");

        // 3. Bottom Panels (Output/Errors)
        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("console-area");

        // Error list setup (Keep your existing ListCell implementation here)
        errorListView = new ListView<>();
        configureErrorList(errorListView); // Extracted to helper for brevity

        bottomTabPane = new TabPane();
        terminalTab = new Tab("Terminal", outputArea);
        terminalTab.setClosable(false);
        Tab errorsTab = new Tab("Errors", errorListView);
        errorsTab.setClosable(false);
        bottomTabPane.getTabs().addAll(terminalTab, errorsTab);

        // 4. Toolbar
        HBox toolBar = createToolBar();

        // 5. Layout Assembly
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

        // Root
        VBox root = new VBox(menuBarManager.getMenuBar(), mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        root.getStyleClass().add("light-theme");

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());

        return scene;
    }

    private Accordion createCategorizedPalette() {
        Accordion accordion = new Accordion();

        Map<BlockCategory, List<AddableBlock>> grouped = Arrays.stream(AddableBlock.values())
                .collect(Collectors.groupingBy(AddableBlock::getCategory));

        // Define order of categories
        BlockCategory[] order = {
                BlockCategory.OUTPUT,
                BlockCategory.INPUT,
                BlockCategory.VARIABLES,
                BlockCategory.FLOW,
                BlockCategory.LOOPS,
                BlockCategory.CONTROL,
                BlockCategory.UTILITY
        };

        for (BlockCategory category : order) {
            List<AddableBlock> blocks = grouped.get(category);
            if (blocks == null) continue;

            VBox content = new VBox(8); // Spacing between items
            content.setPadding(new Insets(10));

            for (AddableBlock blockType : blocks) {
                Label blockLabel = new Label(blockType.getDisplayName());
                blockLabel.setMaxWidth(Double.MAX_VALUE);

                // Style classes: "palette-item", "palette-output", etc.
                blockLabel.getStyleClass().addAll("palette-item", "palette-" + category.name().toLowerCase());

                dragAndDropManager.makeDraggable(blockLabel, blockType);
                content.getChildren().add(blockLabel);
            }

            TitledPane pane = new TitledPane(category.getLabel(), content);
            pane.getStyleClass().add("palette-pane");
            accordion.getPanes().add(pane);
        }

        // Expand the first one by default
        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }

        return accordion;
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

        Button runButton = new Button("Run ▶");
        runButton.getStyleClass().addAll("toolbar-btn", "btn-run");
        runButton.setOnAction(e -> {
            bottomTabPane.getSelectionModel().select(terminalTab);
            eventBus.publish(new CoreApplicationEvents.ExecutionRequestedEvent());
        });

        debugButton = new Button("Debug 🐞");
        debugButton.getStyleClass().addAll("toolbar-btn", "btn-debug");
        debugButton.setOnAction(e -> {
            bottomTabPane.getSelectionModel().select(terminalTab);
            eventBus.publish(new CoreApplicationEvents.DebugStartRequestedEvent());
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        stepOverButton = new Button("Step ⤵");
        stepOverButton.setDisable(true);
        stepOverButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugStepOverRequestedEvent()));

        continueButton = new Button("Continue ⏩");
        continueButton.setDisable(true);
        continueButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugContinueRequestedEvent()));

        stopDebugButton = new Button("Stop ⏹");
        stopDebugButton.setDisable(true);
        stopDebugButton.getStyleClass().add("btn-stop");
        stopDebugButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugStopRequestedEvent()));

        HBox toolbar = new HBox(10,
                undoButton, redoButton,
                spacer1,
                compileButton, runButton, debugButton,
                spacer2,
                stepOverButton, continueButton, stopDebugButton
        );
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("main-toolbar");
        return toolbar;
    }

    private void configureErrorList(ListView<Diagnostic> lv) {
        // (Paste your existing ListCell factory code here)
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
                                if (uiNode != null) {
                                    uiNode.requestFocus();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    // ... (Keep helper methods: updateErrors, onDebuggerStarted, etc.) ...
    private void updateErrors(List<Diagnostic> diagnostics) {
        if (diagnostics == null) errorListView.getItems().clear();
        else errorListView.getItems().setAll(diagnostics);
        if (diagnostics != null && !diagnostics.isEmpty()) bottomTabPane.getSelectionModel().select(1);
    }

    private void onDebuggerStarted() {
        debugButton.setDisable(true);
        stepOverButton.setDisable(true);
        continueButton.setDisable(true);
        stopDebugButton.setDisable(false);
    }

    private void onDebuggerPaused() {
        stepOverButton.setDisable(false);
        continueButton.setDisable(false);
        stopDebugButton.setDisable(false);
    }

    private void onDebuggerResumed() {
        stepOverButton.setDisable(true);
        continueButton.setDisable(true);
        stopDebugButton.setDisable(false);
    }

    private void onDebuggerFinished() {
        debugButton.setDisable(false);
        stepOverButton.setDisable(true);
        continueButton.setDisable(true);
        stopDebugButton.setDisable(true);
    }

    public void setOnSelectProject(Consumer<Void> callback) { this.onSelectProject = callback; }
}