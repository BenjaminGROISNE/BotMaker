package com.botmaker.services;

import com.botmaker.blocks.MainBlock;
import com.botmaker.config.ApplicationConfig;
import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.CodeEditor;
import com.botmaker.project.ProjectFile;
import com.botmaker.state.ApplicationState;
import com.botmaker.state.HistoryManager;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.validation.DiagnosticsManager;
import javafx.application.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CodeEditorService {

    private final ApplicationConfig config;
    private final ApplicationState state;
    private final EventBus eventBus;
    private final BlockFactory blockFactory;
    private final AstRewriter astRewriter;
    private final CodeEditor codeEditor;
    private final BlockDragAndDropManager dragAndDropManager;
    private final LanguageServerService languageServerService;
    private final DiagnosticsManager diagnosticsManager;
    private final HistoryManager historyManager;
    private boolean isRestoringHistory = false;

    public CodeEditorService(
            ApplicationConfig config,
            ApplicationState state,
            EventBus eventBus,
            BlockFactory blockFactory,
            AstRewriter astRewriter,
            BlockDragAndDropManager dragAndDropManager,
            LanguageServerService languageServerService,
            DiagnosticsManager diagnosticsManager) {

        this.config = config;
        this.state = state;
        this.eventBus = eventBus;
        this.blockFactory = blockFactory;
        this.astRewriter = astRewriter;
        this.dragAndDropManager = dragAndDropManager;
        this.languageServerService = languageServerService;
        this.diagnosticsManager = diagnosticsManager;
        this.historyManager = new HistoryManager();
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

        // 1. Record History on Code Updates
        eventBus.subscribe(CoreApplicationEvents.CodeUpdatedEvent.class, this::handleCodeUpdateForHistory, false);

        // 2. Handle Requests
        eventBus.subscribe(CoreApplicationEvents.UndoRequestedEvent.class, e -> undo(), false);
        eventBus.subscribe(CoreApplicationEvents.RedoRequestedEvent.class, e -> redo(), false);

    }

    // Called when block drag-drop or text edit happens
    private void handleCodeUpdateForHistory(CoreApplicationEvents.CodeUpdatedEvent event) {
        // If this update is triggered BY the undo button, don't record it again!
        if (isRestoringHistory) return;

        // Save the PREVIOUS code state before the update happened
        String previousCode = event.getPreviousCode();
        if (previousCode != null && !previousCode.isEmpty()) {
            historyManager.pushState(previousCode);
            broadcastHistoryState();
        }
    }

    private void undo() {
        if (!historyManager.canUndo()) return;
        applyHistoryState(historyManager.undo(state.getCurrentCode()));
    }

    private void redo() {
        if (!historyManager.canRedo()) return;
        applyHistoryState(historyManager.redo(state.getCurrentCode()));
    }

    private void applyHistoryState(String code) {
        isRestoringHistory = true; // Lock recording
        try {
            // This triggers the standard refresh flow (UI update, LSP sync, etc.)
            // We fake a CodeUpdatedEvent so the LanguageServerService picks it up
            eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(code, state.getCurrentCode()));
            broadcastHistoryState();
        } finally {
            isRestoringHistory = false; // Unlock
        }
    }

    private void broadcastHistoryState() {
        eventBus.publish(new CoreApplicationEvents.HistoryStateChangedEvent(
                historyManager.canUndo(),
                historyManager.canRedo()
        ));
    }

    private void handleBreakpointToggle(CoreApplicationEvents.BreakpointToggledEvent event) {
        if (event.isEnabled()) {
            state.addBreakpoint(event.getBlock().getId());
        } else {
            state.removeBreakpoint(event.getBlock().getId());
        }
    }

    public void loadInitialCode() {
        try {
            Path mainFile = config.getSourceFilePath();
            Path sourceDir = mainFile.getParent();

            // Load all java files in the directory
            try (Stream<Path> files = Files.list(sourceDir)) {
                files.filter(p -> p.toString().endsWith(".java")).forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        ProjectFile pf = new ProjectFile(path, content);
                        state.addFile(pf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // Set Active File to Main
            switchToFile(mainFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToFile(Path path) {
        ProjectFile file = state.getAllFiles().stream()
                .filter(f -> f.getPath().equals(path))
                .findFirst().orElse(null);

        if (file == null) return;

        // 1. Refresh UI
        state.setActiveFile(path);

        // Clear history when switching (optional, or keep separate history per file)
        // historyManager.clear();

        refreshUI(file.getContent());
    }


    public void createFile(String className) {
        try {
            String packageName = config.getMainClassName().substring(0, config.getMainClassName().lastIndexOf('.'));
            Path dir = config.getSourceFilePath().getParent();
            Path newPath = dir.resolve(className + ".java");

            String template = "package " + packageName + ";\n\n" +
                    "public class " + className + " {\n" +
                    "    // Add functions here\n" +
                    "    public static void action() {\n" +
                    "        System.out.println(\"Action from " + className + "\");\n" +
                    "    }\n" +
                    "}";

            Files.writeString(newPath, template);

            ProjectFile pf = new ProjectFile(newPath, template);
            state.addFile(pf);

            // Notify LSP
            languageServerService.openFile(newPath, template);

            switchToFile(newPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void refreshUI(String javaCode) {
        state.setCurrentCode(javaCode);
        state.clearNodeToBlockMap();

        if (diagnosticsManager != null) {
            diagnosticsManager.updateSource(state.getMutableNodeToBlockMap(), javaCode);
        }

        AbstractCodeBlock rootBlock = blockFactory.convert(
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
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Loaded: " + state.getActiveFile().getClassName()));
    }

    public CompletionContext createCompletionContext() {
        return new CompletionContext(
                codeEditor,
                languageServerService.getServer(),
                state.getDocUri(),
                state.getCurrentCode(),
                state.getDocVersion(),
                dragAndDropManager,
                state // Pass the state instance
        );
    }

    public CodeEditor getCodeEditor() { return codeEditor; }
    public BlockFactory getBlockFactory() { return blockFactory; }
}