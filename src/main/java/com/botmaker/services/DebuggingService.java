package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.BlockFactory;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.runtime.DebuggingManager;
import com.botmaker.state.ApplicationState;

public class DebuggingService {

    private final ApplicationState state;
    private final EventBus eventBus;
    private final DebuggingManager debuggingManager;

    public DebuggingService(
            ApplicationState state,
            EventBus eventBus,
            CodeExecutionService codeExecutionService,
            BlockFactory blockFactory,
            ApplicationConfig config) {

        this.state = state;
        this.eventBus = eventBus;
        this.debuggingManager = new DebuggingManager(codeExecutionService, eventBus, blockFactory, config);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.DebugStartRequestedEvent.class, event -> startDebugging(), false);

        // Map StepOver and Continue events
        eventBus.subscribe(CoreApplicationEvents.DebugStepOverRequestedEvent.class, event -> stepOver(), false);
        eventBus.subscribe(CoreApplicationEvents.DebugContinueRequestedEvent.class, event -> continueExecution(), false);
    }

    public void startDebugging() {
        debuggingManager.setNodeToBlockMap(state.getNodeToBlockMap());
        debuggingManager.startDebugging(state.getCurrentCode());
    }

    public void stepOver() {
        debuggingManager.stepOver();
    }

    public void continueExecution() {
        debuggingManager.continueExecution();
    }
}