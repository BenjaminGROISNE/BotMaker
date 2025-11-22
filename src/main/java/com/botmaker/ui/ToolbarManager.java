package com.botmaker.ui;

import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class ToolbarManager {

    private final EventBus eventBus;

    // Controls
    private Button undoButton, redoButton;
    private Button runButton, debugButton, unifiedStopButton;
    private Button stepOverButton, continueButton;

    private enum AppState { IDLE, RUNNING, DEBUGGING }
    private AppState currentAppState = AppState.IDLE;

    public ToolbarManager(EventBus eventBus) {
        this.eventBus = eventBus;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.ProgramStartedEvent.class, e -> setAppState(AppState.RUNNING), true);
        eventBus.subscribe(CoreApplicationEvents.ProgramStoppedEvent.class, e -> setAppState(AppState.IDLE), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionStartedEvent.class, e -> setAppState(AppState.DEBUGGING), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionFinishedEvent.class, e -> setAppState(AppState.IDLE), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionPausedEvent.class, e -> updateDebugControls(true), true);
        eventBus.subscribe(CoreApplicationEvents.DebugSessionResumedEvent.class, e -> updateDebugControls(false), true);
        eventBus.subscribe(CoreApplicationEvents.HistoryStateChangedEvent.class, event -> {
            if (undoButton != null) undoButton.setDisable(!event.canUndo());
            if (redoButton != null) redoButton.setDisable(!event.canRedo());
        }, true);
    }

    public HBox createToolBar() {
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
            if (currentAppState == AppState.RUNNING) eventBus.publish(new CoreApplicationEvents.StopRunRequestedEvent());
            else if (currentAppState == AppState.DEBUGGING) eventBus.publish(new CoreApplicationEvents.DebugStopRequestedEvent());
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        stepOverButton = new Button("Step ⤵");
        stepOverButton.setDisable(true);
        stepOverButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugStepOverRequestedEvent()));

        continueButton = new Button("Continue ⏩");
        continueButton.setDisable(true);
        continueButton.setOnAction(e -> eventBus.publish(new CoreApplicationEvents.DebugContinueRequestedEvent()));

        HBox toolbar = new HBox(10, undoButton, redoButton, spacer1, compileButton, runButton, debugButton, unifiedStopButton, spacer2, stepOverButton, continueButton);
        toolbar.setPadding(new Insets(10));
        toolbar.getStyleClass().add("main-toolbar");
        return toolbar;
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
        } else if (currentAppState == AppState.RUNNING) {
            unifiedStopButton.setText("Stop Run ⏹");
            unifiedStopButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;");
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
}