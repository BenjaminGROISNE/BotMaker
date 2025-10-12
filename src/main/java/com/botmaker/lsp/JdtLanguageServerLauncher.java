package com.botmaker.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JdtLanguageServerLauncher {

    private final Process process;
    private final LanguageServer server;

    public JdtLanguageServerLauncher(Path jdtlsPath) throws Exception {
        // Find the Equinox launcher JAR
        Path launcherJar = Files.list(jdtlsPath.resolve("plugins"))
                .filter(p -> p.getFileName().toString().startsWith("org.eclipse.equinox.launcher_"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Launcher JAR not found"));

        String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

        // Build command
        ProcessBuilder pb = new ProcessBuilder(
                javaExecutable,
                "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                "-Dosgi.bundles.defaultStartLevel=4",
                "-Declipse.product=org.eclipse.jdt.ls.core.product",
                "-Dlog.protocol=true",
                "-Dlog.level=ALL",
                "-noverify",
                "-Xmx1G",
                "-jar", launcherJar.toString(),
                "-configuration", jdtlsPath.resolve("config_linux").toString(), // adjust for your OS
                "-data", "jdt-workspace-data"
        );
        // Do NOT redirect the error stream, as it will corrupt the JSON-RPC output
        // pb.redirectErrorStream(true);

        // Start the server process
        process = pb.start();

        // The LSP4J launcher needs exclusive access to the input stream.
        // Do NOT add a separate logger for process.getInputStream().

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
                new SimpleLanguageClient(),
                process.getInputStream(),
                process.getOutputStream()
        );

        launcher.startListening();
        server = launcher.getRemoteProxy();

        // Create a workspace folder path (where projects/code will live)
        Path workspace = Paths.get("projects");
        workspace.toFile().mkdirs();

        // Initialize LSP params
        InitializeParams init = new InitializeParams();
        init.setProcessId((int) ProcessHandle.current().pid());

        // Client capabilities (can be empty)
        init.setCapabilities(new ClientCapabilities());

        // Add workspace folder
        WorkspaceFolder folder = new WorkspaceFolder(workspace.toAbsolutePath().toUri().toString());
        init.setWorkspaceFolders(List.of(folder));

        // Now initialize the server
        server.initialize(init).get();
    }

    public LanguageServer getServer() {
        return server;
    }

    public void stop() {
        try { server.shutdown().get(); } catch (Exception ignored) {}
        process.destroy();
    }

    // Minimal LSP client (can handle notifications later)
    static class SimpleLanguageClient implements LanguageClient {
        @Override
        public void telemetryEvent(Object o) {}
        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            System.out.println("[Diagnostics] " + diagnostics);
        }
        @Override
        public void showMessage(MessageParams messageParams) {
            System.out.println("[Message] " + messageParams.getMessage());
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
            return null;
        }

        @Override
        public void logMessage(MessageParams messageParams) {

        }
    }
}
