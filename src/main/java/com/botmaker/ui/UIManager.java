package com.botmaker.ui;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.services.CodeEditorService;
import com.botmaker.state.ApplicationState;
import com.botmaker.validation.DiagnosticsManager;
import com.botmaker.validation.ErrorTranslator;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.List;
import java.util.function.Consumer;

public class UIManager {

    private final EventBus eventBus;
    private final CodeEditorService codeEditorService;
    private final DiagnosticsManager diagnosticsManager;
    private final Stage primaryStage;

    private final PaletteManager paletteManager;
    private final ToolbarManager toolbarManager;
    private final EventLogManager eventLogManager;
    private final MenuBarManager menuBarManager;
    private final FileExplorerManager fileExplorerManager;

    private VBox blocksContainer;
    private Label statusLabel;
    private TextArea outputArea;
    private ListView<Diagnostic> errorListView;
    private TabPane bottomTabPane;
    private Consumer<Void> onSelectProject;

    public UIManager(BlockDragAndDropManager dragAndDropManager,
                     EventBus eventBus,
                     CodeEditorService codeEditorService,
                     DiagnosticsManager diagnosticsManager,
                     Stage primaryStage,
                     ApplicationConfig config,
                     ApplicationState state) {
        this.eventBus = eventBus;
        this.codeEditorService = codeEditorService;
        this.diagnosticsManager = diagnosticsManager;
        this.primaryStage = primaryStage;

        this.paletteManager = new PaletteManager(dragAndDropManager);
        this.toolbarManager = new ToolbarManager(eventBus);
        this.eventLogManager = new EventLogManager(eventBus);
        this.menuBarManager = new MenuBarManager(primaryStage);
        this.menuBarManager.setEventBus(eventBus);
        this.fileExplorerManager = new FileExplorerManager(config, codeEditorService, state);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.UIBlocksUpdatedEvent.class, this::handleBlocksUpdate, true);
        eventBus.subscribe(CoreApplicationEvents.OutputAppendedEvent.class, event -> {
            if (outputArea.getText().length() > 10_000) {
                String current = outputArea.getText();
                outputArea.setText("[...Trimmed...]\n" + current.substring(current.length() - 5000) + event.getText());
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
        eventBus.subscribe(CoreApplicationEvents.ProgramStartedEvent.class, e -> selectBottomTab(0), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionStartedEvent.class, e -> selectBottomTab(0), true);
    }

    private void handleBlocksUpdate(CoreApplicationEvents.UIBlocksUpdatedEvent event) {
        blocksContainer.getChildren().clear();
        if (event.getRootBlock() != null) {
            CompletionContext context = codeEditorService.createCompletionContext();
            Node rootNode = event.getRootBlock().getUINode(context);
            rootNode.addEventHandler(BlockEvent.BreakpointToggleEvent.TOGGLE_BREAKPOINT, e ->
                    eventBus.publish(new CoreApplicationEvents.BreakpointToggledEvent(e.getBlock(), e.isEnabled())));
            blocksContainer.getChildren().add(rootNode);
        }
    }

    public Scene createScene() {
        menuBarManager.setOnSelectProject(v -> { if (onSelectProject != null) onSelectProject.accept(null); });

        // --- 1. Top Bar Construction ---

        // Left
        HBox editControls = toolbarManager.createEditGroup();
        Separator leftSep = new Separator(Orientation.VERTICAL);
        leftSep.setPadding(new Insets(0, 5, 0, 5));
        HBox leftContainer = new HBox(editControls, leftSep);
        leftContainer.setAlignment(Pos.CENTER_LEFT);

        // Center: Palette
        HBox paletteControls = paletteManager.createHorizontalPalette();

        ScrollPane paletteScroll = new ScrollPane(paletteControls);
        paletteScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paletteScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide Horizontal Bar too, let it shrink

        // Ensure content fills the scroll pane area
        paletteScroll.setFitToWidth(true);
        paletteScroll.setFitToHeight(true);

        paletteScroll.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        paletteScroll.getStyleClass().add("edge-to-edge");

        // Right
        HBox executionControls = toolbarManager.createExecutionGroup();
        Separator rightSep = new Separator(Orientation.VERTICAL);
        rightSep.setPadding(new Insets(0, 5, 0, 5));
        HBox rightContainer = new HBox(rightSep, executionControls);
        rightContainer.setAlignment(Pos.CENTER_RIGHT);

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(6)); // Comfortable padding around the whole bar
        topBar.setLeft(leftContainer);
        topBar.setCenter(paletteScroll);
        topBar.setRight(rightContainer);
        topBar.getStyleClass().add("main-toolbar");

        // FIXED HEIGHT CONSTRAINTS
        // This stops the "Expand as much as I want" issue.
        // 45px content + 12px padding = ~57px total height
        topBar.setMinHeight(50);
        topBar.setPrefHeight(50);
        topBar.setMaxHeight(50);

        topBar.setStyle("-fx-border-color: #dcdcdc; -fx-border-width: 0 0 1 0; -fx-background-color: #f4f4f4;");

        // --- 2. Left Panel: File Explorer ---
        VBox fileExplorer = fileExplorerManager.createView();
        fileExplorer.setMinWidth(150);
        fileExplorer.setMaxWidth(400);

        // --- 3. Center: Code Canvas ---
        blocksContainer = new VBox(10);
        blocksContainer.getStyleClass().add("blocks-canvas");
        blocksContainer.setPadding(new Insets(20));

        ScrollPane canvasScroll = new ScrollPane(blocksContainer);
        canvasScroll.setFitToWidth(true);
        canvasScroll.setFitToHeight(true);
        canvasScroll.getStyleClass().add("code-scroll-pane");

        // --- 4. Bottom Panel: Terminal/Errors ---
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("console-area");
        addContextMenu(outputArea);

        errorListView = new ListView<>();
        configureErrorList(errorListView);
        addContextMenu(errorListView);

        bottomTabPane = new TabPane();
        Tab terminalTab = new Tab("Terminal", outputArea); terminalTab.setClosable(false);
        Tab errorsTab = new Tab("Errors", errorListView); errorsTab.setClosable(false);
        Tab eventsTab = new Tab("Event Log", eventLogManager.getView()); eventsTab.setClosable(false);
        bottomTabPane.getTabs().addAll(terminalTab, errorsTab, eventsTab);

        // --- 5. Layout Assembly ---

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(canvasScroll, bottomTabPane);
        verticalSplit.setDividerPositions(0.82);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.HORIZONTAL);
        mainSplit.getItems().addAll(fileExplorer, verticalSplit);
        mainSplit.setDividerPositions(0.25);

        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");
        statusLabel.setPadding(new Insets(2, 5, 2, 5));

        VBox root = new VBox(menuBarManager.getMenuBar(), topBar, mainSplit, statusLabel);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);
        root.getStyleClass().add("light-theme");

        primaryStage.setOnHidden(e -> eventLogManager.shutdown());

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());
        return scene;
    }

    private void configureErrorList(ListView<Diagnostic> lv) {
        lv.setPlaceholder(new Label("No errors."));
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

    private void addContextMenu(Control control) {
        ContextMenu cm = new ContextMenu();
        if (control instanceof TextArea) {
            TextArea ta = (TextArea) control;
            MenuItem copy = new MenuItem("Copy");
            copy.setOnAction(e -> ta.copy());
            MenuItem clear = new MenuItem("Clear");
            clear.setOnAction(e -> ta.clear());
            cm.getItems().addAll(copy, new SeparatorMenuItem(), clear);
            ta.setContextMenu(cm);
        } else if (control instanceof ListView) {
            ListView<?> lv = (ListView<?>) control;
            MenuItem copy = new MenuItem("Copy Selection");
            copy.setOnAction(e -> {
                Object selected = lv.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(selected.toString());
                    javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
                }
            });
            cm.getItems().add(copy);
            lv.setContextMenu(cm);
        }
    }

    private void updateErrors(List<Diagnostic> diagnostics) {
        if (diagnostics == null) errorListView.getItems().clear();
        else errorListView.getItems().setAll(diagnostics);
        if (diagnostics != null && !diagnostics.isEmpty()) selectBottomTab(1);
    }

    private void selectBottomTab(int index) {
        if (bottomTabPane != null && index < bottomTabPane.getTabs().size()) {
            bottomTabPane.getSelectionModel().select(index);
        }
    }

    public void setOnSelectProject(Consumer<Void> callback) { this.onSelectProject = callback; }
}