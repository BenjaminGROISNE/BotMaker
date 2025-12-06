// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\services\CodeEditorService.java
package com.botmaker.services;

import com.botmaker.blocks.MainBlock;
import com.botmaker.config.ApplicationConfig;
import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.StatementBlock;
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
import com.botmaker.util.BlockLookupHelper;
import com.botmaker.validation.DiagnosticsManager;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.eclipse.jdt.core.dom.ASTNode;

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
        eventBus.subscribe(CoreApplicationEvents.UIRefreshRequestedEvent.class, event ->
                Platform.runLater(() -> refreshUI(event.getCode())), false);

        eventBus.subscribe(CoreApplicationEvents.BreakpointToggledEvent.class,
                this::handleBreakpointToggle, false);

        eventBus.subscribe(CoreApplicationEvents.CodeUpdatedEvent.class, event -> {
            handleCodeUpdateForHistory(event);
            if (!isRestoringHistory) {
                Platform.runLater(() -> refreshUI(event.getNewCode()));
            }
        }, false);

        eventBus.subscribe(CoreApplicationEvents.UndoRequestedEvent.class,
                e -> undo(), false);

        eventBus.subscribe(CoreApplicationEvents.RedoRequestedEvent.class,
                e -> redo(), false);
        eventBus.subscribe(CoreApplicationEvents.CopyRequestedEvent.class, e -> copySelectedBlock(), true);
        eventBus.subscribe(CoreApplicationEvents.PasteRequestedEvent.class, e -> pasteFromClipboard(), true);
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

    // --- FIX: LOAD ALL FILES INCLUDING LIBRARY FILES ---
    public void loadInitialCode() {
        try {
            Path mainFile = config.getSourceFilePath();

            // Get the source root (src/main/java)
            Path sourceRoot = mainFile.getParent();
            while (sourceRoot != null && !sourceRoot.getFileName().toString().equals("java")) {
                sourceRoot = sourceRoot.getParent();
            }

            if (sourceRoot == null) {
                sourceRoot = mainFile.getParent();
            }

            // Load ALL java files recursively, including library files
            loadFilesRecursively(sourceRoot);

            // Set Active File to Main and refresh UI
            switchToFile(mainFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recursively loads all .java files in the directory tree
     */
    private void loadFilesRecursively(Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            // Check if already loaded
                            boolean alreadyLoaded = state.getAllFiles().stream()
                                    .anyMatch(f -> f.getPath().equals(path));

                            if (!alreadyLoaded) {
                                String content = Files.readString(path);
                                ProjectFile pf = new ProjectFile(path, content);
                                state.addFile(pf);

                                // Open file in Language Server
                                languageServerService.openFile(path, content);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading file: " + path);
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToFile(Path path) {
        ProjectFile file = state.getAllFiles().stream()
                .filter(f -> f.getPath().equals(path))
                .findFirst().orElse(null);

        if (file == null) {
            System.err.println("File not found in state: " + path);
            return;
        }

        state.setActiveFile(path);
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

    public void deleteFile(Path path) {
        try {
            // 1. Delete from disk
            Files.deleteIfExists(path);

            // 2. Remove from state
            state.removeFile(path);

            // 3. Update UI if active file was deleted
            if (state.getActiveFile() != null && state.getActiveFile().getPath().equals(path)) {
                // Reload main file
                switchToFile(config.getSourceFilePath());
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Deleted active file. Switched to Main."));
            } else {
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("File deleted."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Error deleting file: " + e.getMessage()));
        }
    }

    private void refreshUI(String javaCode) {
        state.setCurrentCode(javaCode);
        state.clearNodeToBlockMap();

        if (diagnosticsManager != null) {
            diagnosticsManager.updateSource(state.getMutableNodeToBlockMap(), javaCode);
        }

        // Determine if file is Read-Only (Library)
        boolean isReadOnly = false;
        if (state.getActiveFile() != null) {
            String path = state.getActiveFile().getPath().toString().replace("\\", "/");
            if (path.contains("com/botmaker/library")) {
                isReadOnly = true;
            }
        }

        AbstractCodeBlock rootBlock = blockFactory.convert(
                javaCode,
                state.getMutableNodeToBlockMap(),
                dragAndDropManager,
                isReadOnly // Pass the flag
        );

        for (CodeBlock block : state.getNodeToBlockMap().values()) {
            if (state.hasBreakpoint(block.getId())) {
                block.setBreakpoint(true);
            }
        }

        state.setCompilationUnit(blockFactory.getCompilationUnit());
        eventBus.publish(new CoreApplicationEvents.UIBlocksUpdatedEvent(rootBlock));

        if (state.getActiveFile() != null) {
            String fileName = state.getActiveFile().getPath().getFileName().toString();
            // Add [Lib] indicator for library files
            if (isReadOnly) {
                fileName += " [Library - Read Only]";
            }
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Loaded: " + fileName));
        }
    }
    private void copySelectedBlock() {
        state.getHighlightedBlock().ifPresent(block -> {
            ASTNode node = block.getAstNode();
            if (node != null) {
                String source = node.toString();
                ClipboardContent content = new ClipboardContent();
                content.putString(source);
                Clipboard.getSystemClipboard().setContent(content);
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Copied block to clipboard."));
            }
        });
    }

    private void pasteFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) return;

        String codeToPaste = clipboard.getString();

        // Determine insertion point based on highlighted block
        state.getHighlightedBlock().ifPresentOrElse(selectedBlock -> {
            if (selectedBlock instanceof StatementBlock) {
                StatementBlock stmtBlock = (StatementBlock) selectedBlock;
                BodyBlock parentBody = BlockLookupHelper.findParentBody(stmtBlock, state.getNodeToBlockMap());

                if (parentBody != null) {
                    int index = parentBody.getStatements().indexOf(stmtBlock);
                    // Paste AFTER the selected block
                    codeEditor.pasteCode(parentBody, index + 1, codeToPaste);
                }
            }
        }, () -> {
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Select a block to paste after."));
        });
    }
    public CompletionContext createCompletionContext() {
        return new CompletionContext(
                codeEditor,
                languageServerService.getServer(),
                state.getDocUri(),
                state.getCurrentCode(),
                state.getDocVersion(),
                dragAndDropManager,
                state,
                eventBus // Pass EventBus
        );
    }

    public CodeEditor getCodeEditor() { return codeEditor; }
    public BlockFactory getBlockFactory() { return blockFactory; }
}