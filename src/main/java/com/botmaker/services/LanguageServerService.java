package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.config.Constants;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.JdtLanguageServerLauncher;
import com.botmaker.state.ApplicationState;
import javafx.application.Platform;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LanguageServerService {

    private final ApplicationConfig config;
    private final ApplicationState state;
    private final EventBus eventBus;
    private final com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    private LanguageServer server;
    private JdtLanguageServerLauncher launcher;
    private boolean shouldClearCache = false;

    public LanguageServerService(
            ApplicationConfig config,
            ApplicationState state,
            EventBus eventBus,
            com.botmaker.validation.DiagnosticsManager diagnosticsManager) {
        this.config = config;
        this.state = state;
        this.eventBus = eventBus;
        this.diagnosticsManager = diagnosticsManager;
        setupEventHandlers();
    }

    public void setShouldClearCache(boolean shouldClear) { this.shouldClearCache = shouldClear; }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.CodeUpdatedEvent.class, this::handleCodeUpdate, false);
    }

    public void initialize() throws Exception {
        if (shouldClearCache) {
            JdtLanguageServerLauncher.cleanupWorkspace(config.getWorkspaceDataPath());
        }

        launcher = new JdtLanguageServerLauncher(
                config.getJdtServerPath(),
                config.getProjectPath(),
                config.getWorkspaceDataPath(),
                (PublishDiagnosticsParams params) -> {
                    Platform.runLater(() -> {
                        List<Diagnostic> diagnostics = params.getDiagnostics();
                        eventBus.publish(new CoreApplicationEvents.DiagnosticsUpdatedEvent(diagnostics));
                    });
                }
        );

        server = launcher.getServer();

        // Ensure the Main file exists on disk, but do NOT open it here.
        // CodeEditorService.loadInitialCode() will open it shortly after.
        Path docPath = config.getSourceFilePath().toAbsolutePath().normalize();

        if (!Files.exists(docPath)) {
            Files.createDirectories(docPath.getParent());
            String mainClassName = config.getMainClassName();
            String[] parts = mainClassName.split("\\.");
            String packageName = parts.length > 1 ? String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1)) : "com.demo";
            String className = parts[parts.length - 1];

            String defaultCode = String.format("""
            package %s;
            public class %s {
                public static void main(String[] args) {
                    System.out.println("Hello from %s!");
                }
            }
            """, packageName, className, className);
            Files.writeString(docPath, defaultCode);
        }

        // Setup initial state URIs
        String docUri = docPath.toUri().toString();
        state.setDocUri(docUri);
        state.setDocVersion(1);
    }

    private void handleCodeUpdate(CoreApplicationEvents.CodeUpdatedEvent event) {
        try {
            // Write to file
            Path docPath = Path.of(new java.net.URI(state.getDocUri()));
            Files.writeString(docPath, event.getNewCode());

            // Update state
            state.incrementDocVersion();
            state.setCurrentCode(event.getNewCode());

            // Notify LSP server
            if (server != null) {
                server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                        new VersionedTextDocumentIdentifier(state.getDocUri(), (int) state.getDocVersion()),
                        List.of(new TextDocumentContentChangeEvent(event.getNewCode()))
                ));
            }

            eventBus.publish(new CoreApplicationEvents.UIRefreshRequestedEvent(event.getNewCode()));

        } catch (Exception e) {
            e.printStackTrace();
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Error saving file: " + e.getMessage()));
        }
    }

    public void openFile(Path path, String content) {
        if (server == null) return;
        String uri = path.toUri().toString();
        // Use version 1 for newly opened files
        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(uri, "java", 1, content)
        ));
    }

    public void shutdown() {
        if (launcher != null) {
            try {
                System.out.println("Requesting server shutdown...");
                if (server != null) {
                    server.shutdown().get(Constants.DEBUGGER_SHUTDOWN_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
                    server.exit();
                }
                Thread.sleep(Constants.SHORT_SLEEP_MS);
            } catch (Exception e) {
                System.err.println("Error during server shutdown: " + e.getMessage());
            } finally {
                launcher.stop();
            }
        }
    }

    public LanguageServer getServer() { return server; }
}