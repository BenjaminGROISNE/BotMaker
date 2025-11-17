package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.state.ApplicationState;

/**
 * Service wrapper for code execution (compilation and running).
 * Bridges between the event system and the existing CodeExecutionService.
 */
public class ExecutionService {

    private final ApplicationConfig config;
    private final ApplicationState state;
    private final EventBus eventBus;
    private final CodeExecutionService codeExecutionService;
    private final com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    public ExecutionService(
            ApplicationConfig config,
            ApplicationState state,
            EventBus eventBus,
            CodeExecutionService codeExecutionService,
            com.botmaker.validation.DiagnosticsManager diagnosticsManager) {

        this.config = config;
        this.state = state;
        this.eventBus = eventBus;
        this.codeExecutionService = codeExecutionService;
        this.diagnosticsManager = diagnosticsManager;

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Subscribe to compilation requests
        eventBus.subscribe(
                CoreApplicationEvents.CompilationRequestedEvent.class,
                event -> compile(),
                false
        );

        // Subscribe to execution requests
        eventBus.subscribe(
                CoreApplicationEvents.ExecutionRequestedEvent.class,
                event -> run(),
                false
        );
    }

    /**
     * Compiles the current code
     */
    public void compile() {
        codeExecutionService.compileCode(state.getCurrentCode());
    }

    /**
     * Runs the current code
     */
    public void run() {
        codeExecutionService.runCode(state.getCurrentCode());
    }

    /**
     * Get the underlying code execution service
     */
    public CodeExecutionService getCodeExecutionService() {
        return codeExecutionService;
    }
}