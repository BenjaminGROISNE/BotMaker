package com.botmaker.lsp;

import com.botmaker.config.Constants;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class JdtLanguageServerLauncher {

    private final Process process;
    private final LanguageServer server;

    public JdtLanguageServerLauncher(
            Path jdtlsPath,
            Path projectPath,
            Path workspaceData,
            Consumer<PublishDiagnosticsParams> diagnosticsConsumer) throws Exception {

        // --- LOGGING START ---
        System.out.println("Initializing JDT LS...");
        System.out.println("  JDT Path: " + jdtlsPath);
        System.out.println("  Project: " + projectPath);
        System.out.println("  Workspace: " + workspaceData);
        // --- LOGGING END ---

        // Suppress LSP4J warnings about unsupported notifications
        Logger.getLogger("org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint").setLevel(Level.SEVERE);
        Logger.getLogger("org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer").setLevel(Level.SEVERE);

        // Find the Equinox launcher JAR
        Path launcherJar = Files.list(jdtlsPath.resolve("plugins"))
                .filter(p -> p.getFileName().toString().startsWith("org.eclipse.equinox.launcher_"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Launcher JAR not found"));

        String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

        // USE the projectPath parameter (not hardcoded)
        Path projectDir = projectPath.toAbsolutePath().normalize();

        // USE the workspaceData parameter (not hardcoded)
        Files.createDirectories(workspaceData);

        // Build command with all necessary flags from VS Code implementation
        List<String> command = new ArrayList<>(Arrays.asList(
                javaExecutable,
                // Java 25 specific flags
                Constants.JVM_ENTITY_SIZE_LIMIT,
                Constants.JVM_TOTAL_ENTITY_SIZE_LIMIT,
                // Module system flags
                "--add-modules=ALL-SYSTEM",
                "--add-opens", "java.base/java.util=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/sun.nio.fs=ALL-UNNAMED",
                // Eclipse/JDT configuration
                "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                "-Dosgi.bundles.defaultStartLevel=4",
                "-Declipse.product=org.eclipse.jdt.ls.core.product",
                Constants.LSP_DETECT_VM_DISABLED,
                Constants.LSP_FILE_ENCODING,
                Constants.LSP_LOG_DISABLE,
                Constants.JVM_MAX_HEAP,
                Constants.LSP_DEPENDENCY_COLLECTOR,
                "--enable-native-access=javafx.graphics"
        ));

        // Add debug logging conditionally
        if (Constants.LSP_LOG_PROTOCOL) {
            command.add("-Dlog.protocol=true");
            command.add("-Dlog.level=" + Constants.LSP_LOG_LEVEL);
        }

        command.addAll(Arrays.asList(
                "-jar", launcherJar.toString(),
                "-configuration", jdtlsPath.resolve("config_linux").toString(),
                "-data", workspaceData.toAbsolutePath().normalize().toString()
        ));

        ProcessBuilder pb = new ProcessBuilder(command);
        process = pb.start();

        // Log the error stream separately to see any server-side issues.
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println("[JDT LS ERR] " + line);
                }
            } catch (IOException ignored) {}
        }).start();

        // Connect LSP4J client to the process
        Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(
                new SimpleLanguageClient(diagnosticsConsumer),
                process.getInputStream(),
                process.getOutputStream()
        );

        launcher.startListening();
        server = launcher.getRemoteProxy();

        // Initialize LSP params
        InitializeParams init = new InitializeParams();
        init.setProcessId((int) ProcessHandle.current().pid());

        // Set up client capabilities
        ClientCapabilities capabilities = new ClientCapabilities();
        WorkspaceClientCapabilities workspaceCaps = new WorkspaceClientCapabilities();
        workspaceCaps.setDidChangeConfiguration(new DidChangeConfigurationCapabilities(true));
        workspaceCaps.setWorkspaceFolders(true);
        capabilities.setWorkspace(workspaceCaps);

        TextDocumentClientCapabilities textDocCaps = new TextDocumentClientCapabilities();
        capabilities.setTextDocument(textDocCaps);

        init.setCapabilities(capabilities);

        // Add workspace folder - this should point to your actual project
        WorkspaceFolder folder = new WorkspaceFolder(projectDir.toUri().toString());
        init.setWorkspaceFolders(List.of(folder));
        init.setRootUri(folder.getUri());

        System.out.println("Workspace root: " + folder.getUri());
        System.out.println("Workspace data: " + workspaceData);

        // Initialize the server
        InitializeResult result = server.initialize(init).get();
        System.out.println("Server initialized: " + result.getCapabilities());

        // Send initialized notification
        server.initialized(new InitializedParams());
    }

    public LanguageServer getServer() {
        return server;
    }

    public void stop() {
        try { server.shutdown().get(); } catch (Exception ignored) {}
        server.exit();
        process.destroy();
    }

    /**
     * Forcefully deletes the workspace cache directory.
     * Call this BEFORE creating the launcher if you suspect corruption.
     */
    public static void cleanupWorkspace(Path workspaceData) {
        if (!Files.exists(workspaceData)) return;

        System.out.println("Cleaning up JDT workspace cache: " + workspaceData);
        try (Stream<Path> walk = Files.walk(workspaceData)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            System.out.println("Cache cleared successfully.");
        } catch (IOException e) {
            System.err.println("Failed to clear workspace cache: " + e.getMessage());
            // Try to delete at least the lock file
            try {
                Files.deleteIfExists(workspaceData.resolve(".metadata/.lock"));
                System.out.println("Deleted .lock file as fallback.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Minimal LSP client
    static class SimpleLanguageClient implements LanguageClient {
        private final Consumer<PublishDiagnosticsParams> diagnosticsConsumer;

        public SimpleLanguageClient(Consumer<PublishDiagnosticsParams> diagnosticsConsumer) {
            this.diagnosticsConsumer = diagnosticsConsumer;
        }

        @Override
        public void telemetryEvent(Object o) {
            System.out.println("[Telemetry] " + o);
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            System.out.println("[Diagnostics] " + diagnostics.getUri() + " -> " + diagnostics.getDiagnostics().size() + " issues");
            if (diagnosticsConsumer != null) {
                diagnosticsConsumer.accept(diagnostics);
            }
        }

        @Override
        public void showMessage(MessageParams messageParams) {
            System.out.println("[Message] " + messageParams.getMessage());
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
            System.out.println("[MessageRequest] " + showMessageRequestParams.getMessage());
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void logMessage(MessageParams messageParams) {
            System.out.println("[Log] " + messageParams.getMessage());
        }

        @Override
        public CompletableFuture<Void> registerCapability(RegistrationParams params) {
            System.out.println("[RegisterCapability] " + params.getRegistrations().size() + " capabilities");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
            System.out.println("[UnregisterCapability] " + params.getUnregisterations().size() + " capabilities");
            return CompletableFuture.completedFuture(null);
        }
    }
}