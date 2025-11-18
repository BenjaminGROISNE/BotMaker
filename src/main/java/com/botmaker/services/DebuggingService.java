package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.BlockFactory;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.runtime.DebuggingManager;
import com.botmaker.state.ApplicationState;

/**
 * Service wrapper for debugging operations.
 * Bridges between the event system and the existing DebuggingManager.
 */
public class DebuggingService {

    private final ApplicationState state;
    private final EventBus eventBus;
    private final DebuggingManager debuggingManager;
    ApplicationConfig config;

    public DebuggingService(
            ApplicationState state,
            EventBus eventBus,
            CodeExecutionService codeExecutionService,
            BlockFactory blockFactory,
            ApplicationConfig config) {

        this.state = state;
        this.eventBus = eventBus;
        this.debuggingManager = new DebuggingManager(
                codeExecutionService,
                eventBus,
                blockFactory,
                config
        );

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Subscribe to debug start requests
        eventBus.subscribe(
                CoreApplicationEvents.DebugStartRequestedEvent.class,
                event -> startDebugging(),
                false
        );

        // Subscribe to debug resume requests
        eventBus.subscribe(
                CoreApplicationEvents.DebugResumeRequestedEvent.class,
                event -> resume(),
                false
        );
    }

    /**
     * Starts a debugging session
     */
    public void startDebugging() {
        debuggingManager.setNodeToBlockMap(state.getNodeToBlockMap());
        debuggingManager.startDebugging(state.getCurrentCode());
    }

    /**
     * Resumes the debugging session
     */
    public void resume() {
        debuggingManager.resume();
    }

    /**
     * Get the underlying debugging manager
     */
    public DebuggingManager getDebuggingManager() {
        return debuggingManager;
    }
}