package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.lsp.JdtLanguageServerLauncher;
import com.botmaker.state.ApplicationState;
import javafx.application.Platform;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Manages the Language Server Protocol (LSP) integration.
 * Handles initialization, document synchronization, and diagnostics.
 */
public class LanguageServerService {

    private final ApplicationConfig config;
    private final ApplicationState state;
    private final EventBus eventBus;
    private final com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    private LanguageServer server;
    private JdtLanguageServerLauncher launcher;

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

    private void setupEventHandlers() {
        // Subscribe to code updates to sync with LSP
        eventBus.subscribe(
                CoreApplicationEvents.CodeUpdatedEvent.class,
                this::handleCodeUpdate,
                false
        );
    }

    /**
     * Initializes the language server
     */
    public void initialize() throws Exception {
        launcher = new JdtLanguageServerLauncher(
                config.getJdtServerPath(),
                (params) -> {
                    Platform.runLater(() -> {
                        eventBus.publish(new CoreApplicationEvents.DiagnosticsUpdatedEvent(
                                params.getDiagnostics()
                        ));
                    });
                }
        );

        server = launcher.getServer();

        // Open the initial document
        Path docPath = config.getSourceFilePath().toAbsolutePath();
        String docUri = docPath.toUri().toString();
        String currentCode = Files.readString(docPath);

        state.setDocUri(docUri);
        state.setCurrentCode(currentCode);
        state.setDocVersion(1);

        server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(docUri, "java", (int) state.getDocVersion(), currentCode)
        ));
    }

    /**
     * Handles code update events by syncing with LSP
     */
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

    /**
     * Shuts down the language server gracefully
     */
    public void shutdown() {
        if (launcher != null) {
            try {
                System.out.println("Requesting server shutdown...");

                // Give the server a chance to shutdown gracefully
                if (server != null) {
                    server.shutdown().get(2, java.util.concurrent.TimeUnit.SECONDS);
                    server.exit();
                }

                // Small delay to let LSP process the exit
                Thread.sleep(500);

            } catch (java.util.concurrent.TimeoutException e) {
                System.err.println("Server shutdown timed out, forcing stop...");
            } catch (Exception e) {
                System.err.println("Error during server shutdown: " + e.getMessage());
            } finally {
                // Force stop the launcher/process
                launcher.stop();
            }
        }
    }

    /**
     * Get the language server instance
     */
    public LanguageServer getServer() {
        return server;
    }
}