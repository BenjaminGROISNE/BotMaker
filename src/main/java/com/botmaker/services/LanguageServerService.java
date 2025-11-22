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

    // New flag
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

    public void setShouldClearCache(boolean shouldClear) {
        this.shouldClearCache = shouldClear;
    }

    private void setupEventHandlers() {
        eventBus.subscribe(
                CoreApplicationEvents.CodeUpdatedEvent.class,
                this::handleCodeUpdate,
                false
        );
    }

    public void initialize() throws Exception {
        // NEW: Check if we need to clear cache
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

        // ... rest of the file stays the same ...
        server = launcher.getServer();

        Path docPath = config.getSourceFilePath().toAbsolutePath().normalize();

        // Ensure the file exists
        if (!Files.exists(docPath)) {
            Files.createDirectories(docPath.getParent());

            // Extract project name and package from main class name
            String mainClassName = config.getMainClassName();
            String[] parts = mainClassName.split("\\.");
            String packageName = parts.length > 1 ? String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1)) : "com.demo";
            String className = parts[parts.length - 1];

            // Create a default file with correct package and class name
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

        String docUri = docPath.toUri().toString();
        String currentCode = Files.readString(docPath);

        state.setDocUri(docUri);
        state.setCurrentCode(currentCode);
        state.setDocVersion(1);

        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(docUri, "java", (int) state.getDocVersion(), currentCode)
        ));
    }

    // ... shutdown/handlers stay same ...
    private void handleCodeUpdate(CoreApplicationEvents.CodeUpdatedEvent event) {
        try {
            // Write to file
            Path docPath = Path.of(new java.net.URI(state.getDocUri()));
            Files.writeString(docPath, event.getNewCode());

            // Update state
            state.incrementDocVersion();
            state.setCurrentCode(event.getNewCode());

            // Notify LSP server
            server.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                    new VersionedTextDocumentIdentifier(state.getDocUri(), (int) state.getDocVersion()),
                    List.of(new TextDocumentContentChangeEvent(event.getNewCode()))
            ));

            eventBus.publish(new CoreApplicationEvents.UIRefreshRequestedEvent(event.getNewCode()));

        } catch (Exception e) {
            e.printStackTrace();
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                    "Error saving file: " + e.getMessage()
            ));
        }
    }

    public void openFile(Path path, String content) {
        if (server == null) return;
        String uri = path.toUri().toString();
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
            } catch (java.util.concurrent.TimeoutException e) {
                System.err.println("Server shutdown timed out, forcing stop...");
            } catch (Exception e) {
                System.err.println("Error during server shutdown: " + e.getMessage());
            } finally {
                launcher.stop();
            }
        }
    }

    public LanguageServer getServer() {
        return server;
    }
}