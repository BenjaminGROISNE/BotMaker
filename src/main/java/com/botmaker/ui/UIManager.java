package com.botmaker.ui;

import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.services.CodeEditorService;
import com.botmaker.validation.DiagnosticsManager;
import com.botmaker.validation.ErrorTranslator;
import javafx.geometry.Orientation;
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
                     Stage primaryStage) {
        this.eventBus = eventBus;
        this.codeEditorService = codeEditorService;
        this.diagnosticsManager = diagnosticsManager;
        this.primaryStage = primaryStage;

        // Delegate Managers
        this.paletteManager = new PaletteManager(dragAndDropManager);
        this.toolbarManager = new ToolbarManager(eventBus);
        this.eventLogManager = new EventLogManager(eventBus);
        this.menuBarManager = new MenuBarManager(primaryStage);
        this.menuBarManager.setEventBus(eventBus);

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

        blocksContainer = new VBox(10);
        blocksContainer.getStyleClass().add("blocks-canvas");
        ScrollPane scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("code-scroll-pane");

        VBox paletteContainer = new VBox(paletteManager.createCategorizedPalette());
        paletteContainer.setPrefWidth(220);
        paletteContainer.getStyleClass().add("palette-sidebar");

        statusLabel = new Label("Ready");
        statusLabel.setId("status-label");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.getStyleClass().add("console-area");

        errorListView = new ListView<>();
        configureErrorList(errorListView);

        bottomTabPane = new TabPane();
        Tab terminalTab = new Tab("Terminal", outputArea); terminalTab.setClosable(false);
        Tab errorsTab = new Tab("Errors", errorListView); errorsTab.setClosable(false);
        Tab eventsTab = new Tab("Event Log", eventLogManager.getView()); eventsTab.setClosable(false);
        bottomTabPane.getTabs().addAll(terminalTab, errorsTab, eventsTab);

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(scrollPane, bottomTabPane);
        verticalSplit.setDividerPositions(0.75);
        VBox.setVgrow(verticalSplit, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(toolbarManager.createToolBar());
        mainLayout.setLeft(paletteContainer);
        mainLayout.setCenter(verticalSplit);
        mainLayout.setBottom(statusLabel);

        VBox root = new VBox(menuBarManager.getMenuBar(), mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
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