package com.botmaker.services;

import com.botmaker.blocks.MainBlock;
import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.CodeEditor;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.application.Platform;

/**
 * Service responsible for code editing operations and UI synchronization.
 * Manages the relationship between code, AST, and visual blocks.
 */
public class CodeEditorService {

    private final ApplicationConfig config;
    private final ApplicationState state;
    private final EventBus eventBus;
    private final BlockFactory blockFactory;
    private final AstRewriter astRewriter;
    private final CodeEditor codeEditor;
    private final BlockDragAndDropManager dragAndDropManager;
    private final LanguageServerService languageServerService;
    private final com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    public CodeEditorService(
            ApplicationConfig config,
            ApplicationState state,
            EventBus eventBus,
            BlockFactory blockFactory,
            AstRewriter astRewriter,
            BlockDragAndDropManager dragAndDropManager,
            LanguageServerService languageServerService,
            com.botmaker.validation.DiagnosticsManager diagnosticsManager) {

        this.config = config;
        this.state = state;
        this.eventBus = eventBus;
        this.blockFactory = blockFactory;
        this.astRewriter = astRewriter;
        this.dragAndDropManager = dragAndDropManager;
        this.languageServerService = languageServerService;
        this.diagnosticsManager = diagnosticsManager;

        // Initialize code editor
        this.codeEditor = new CodeEditor(state, eventBus, astRewriter, blockFactory);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Subscribe to UI refresh requests
        eventBus.subscribe(
                CoreApplicationEvents.UIRefreshRequestedEvent.class,
                event -> Platform.runLater(() -> refreshUI(event.getCode())),
                false
        );
    }

    /**
     * Loads the initial code and triggers UI refresh
     */
    public void loadInitialCode() {
        String currentCode = state.getCurrentCode();
        Platform.runLater(() -> refreshUI(currentCode));
    }

    /**
     * Refreshes the UI based on the current code
     */
    private void refreshUI(String javaCode) {
        state.setCurrentCode(javaCode);
        state.clearNodeToBlockMap();

        if (diagnosticsManager != null) {
            diagnosticsManager.updateSource(state.getMutableNodeToBlockMap(), javaCode);
        }

        // Parse code to blocks
        MainBlock rootBlock = blockFactory.convert(
                javaCode,
                state.getMutableNodeToBlockMap(),
                dragAndDropManager
        );

        // Update compilation unit in state
        state.setCompilationUnit(blockFactory.getCompilationUnit());

        // Publish event with the root block (UI will handle rendering)
        eventBus.publish(new CoreApplicationEvents.UIBlocksUpdatedEvent(rootBlock));
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("UI Refreshed."));
    }

    /**
     * Creates a completion context for block rendering
     * PHASE 3: No longer includes Main reference
     */
    public CompletionContext createCompletionContext() {
        return new CompletionContext(
                codeEditor,
                languageServerService.getServer(),
                state.getDocUri(),
                state.getCurrentCode(),
                state.getDocVersion(),
                dragAndDropManager
        );
    }

    /**
     * Get the code editor instance
     */
    public CodeEditor getCodeEditor() {
        return codeEditor;
    }

    /**
     * Get the block factory instance
     */
    public BlockFactory getBlockFactory() {
        return blockFactory;
    }
}