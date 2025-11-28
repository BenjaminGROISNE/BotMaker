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

    // ... (Constructor and fields remain exactly the same) ...
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
        eventBus.subscribe(CoreApplicationEvents.UIRefreshRequestedEvent.class, event ->
                Platform.runLater(() -> refreshUI(event.getCode())), false);

        eventBus.subscribe(CoreApplicationEvents.BreakpointToggledEvent.class,
                this::handleBreakpointToggle, false);

        // Handle code updates: refresh UI + history
        eventBus.subscribe(CoreApplicationEvents.CodeUpdatedEvent.class, event -> {
            // First, handle history (but skip if we're restoring)
            handleCodeUpdateForHistory(event);

            // Then refresh UI with the new code (always, unless restoring)
            if (!isRestoringHistory) {
                Platform.runLater(() -> refreshUI(event.getNewCode()));
            }
        }, false);

        eventBus.subscribe(CoreApplicationEvents.UndoRequestedEvent.class,
                e -> undo(), false);

        eventBus.subscribe(CoreApplicationEvents.RedoRequestedEvent.class,
                e -> redo(), false);
    }

    private void handleCodeUpdateForHistory(CoreApplicationEvents.CodeUpdatedEvent event) {
        if (isRestoringHistory) return;
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
        isRestoringHistory = true;
        try {
            eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(code, state.getCurrentCode()));
            broadcastHistoryState();
        } finally {
            isRestoringHistory = false;
        }
    }

    private void broadcastHistoryState() {
        eventBus.publish(new CoreApplicationEvents.HistoryStateChangedEvent(historyManager.canUndo(), historyManager.canRedo()));
    }

    private void handleBreakpointToggle(CoreApplicationEvents.BreakpointToggledEvent event) {
        if (event.isEnabled()) {
            state.addBreakpoint(event.getBlock().getId());
        } else {
            state.removeBreakpoint(event.getBlock().getId());
        }
    }

    // --- FIX: LOAD AND OPEN ALL FILES ---
    public void loadInitialCode() {
        try {
            Path mainFile = config.getSourceFilePath();
            Path sourceDir = mainFile.getParent();

            // Load all java files in the directory
            if (Files.exists(sourceDir)) {
                try (Stream<Path> files = Files.list(sourceDir)) {
                    files.filter(p -> p.toString().endsWith(".java")).forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            ProjectFile pf = new ProjectFile(path, content);
                            state.addFile(pf);

                            // FIX: Explicitly open every file in the Language Server
                            // This ensures the LSP knows about secondary files immediately.
                            languageServerService.openFile(path, content);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            // Set Active File to Main and refresh UI
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

        state.setActiveFile(path);
        // Update doc version/uri in state for LSP sync context
        state.setDocUri(file.getUri());

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

        for (CodeBlock block : state.getNodeToBlockMap().values()) {
            if (state.hasBreakpoint(block.getId())) {
                block.setBreakpoint(true);
            }
        }

        state.setCompilationUnit(blockFactory.getCompilationUnit());
        eventBus.publish(new CoreApplicationEvents.UIBlocksUpdatedEvent(rootBlock));

        if (state.getActiveFile() != null) {
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Loaded: " + state.getActiveFile().getClassName()));
        }
    }

    public CompletionContext createCompletionContext() {
        return new CompletionContext(
                codeEditor,
                languageServerService.getServer(),
                state.getDocUri(),
                state.getCurrentCode(),
                state.getDocVersion(),
                dragAndDropManager,
                state
        );
    }

    public CodeEditor getCodeEditor() { return codeEditor; }
    public BlockFactory getBlockFactory() { return blockFactory; }
}