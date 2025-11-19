package com.botmaker.services;

import com.botmaker.blocks.MainBlock;
import com.botmaker.config.ApplicationConfig;
import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.CodeEditor;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.application.Platform;

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

        this.codeEditor = new CodeEditor(state, eventBus, astRewriter, blockFactory);

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(
                CoreApplicationEvents.UIRefreshRequestedEvent.class,
                event -> Platform.runLater(() -> refreshUI(event.getCode())),
                false
        );

        // NEW: Listen for toggles to update state
        eventBus.subscribe(
                CoreApplicationEvents.BreakpointToggledEvent.class,
                this::handleBreakpointToggle,
                false
        );
    }

    private void handleBreakpointToggle(CoreApplicationEvents.BreakpointToggledEvent event) {
        if (event.isEnabled()) {
            state.addBreakpoint(event.getBlock().getId());
        } else {
            state.removeBreakpoint(event.getBlock().getId());
        }
    }

    public void loadInitialCode() {
        String currentCode = state.getCurrentCode();
        Platform.runLater(() -> refreshUI(currentCode));
    }

    private void refreshUI(String javaCode) {
        state.setCurrentCode(javaCode);
        state.clearNodeToBlockMap();

        if (diagnosticsManager != null) {
            diagnosticsManager.updateSource(state.getMutableNodeToBlockMap(), javaCode);
        }

        MainBlock rootBlock = blockFactory.convert(
                javaCode,
                state.getMutableNodeToBlockMap(),
                dragAndDropManager
        );

        // NEW: Restore breakpoints on newly created blocks
        for (CodeBlock block : state.getNodeToBlockMap().values()) {
            if (state.hasBreakpoint(block.getId())) {
                block.setBreakpoint(true);
            }
        }

        state.setCompilationUnit(blockFactory.getCompilationUnit());

        eventBus.publish(new CoreApplicationEvents.UIBlocksUpdatedEvent(rootBlock));
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("UI Refreshed."));
    }

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

    public CodeEditor getCodeEditor() { return codeEditor; }
    public BlockFactory getBlockFactory() { return blockFactory; }
}