package com.botmaker.lsp;

import com.botmaker.config.Constants;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
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

        System.out.println("Initializing JDT LS...");

        // Suppress LSP4J internal logging
        Logger.getLogger("org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint").setLevel(Level.SEVERE);
        Logger.getLogger("org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer").setLevel(Level.SEVERE);

        // Locate launcher jar
        Path launcherJar = Files.list(jdtlsPath.resolve("plugins"))
                .filter(p -> p.getFileName().toString().startsWith("org.eclipse.equinox.launcher_"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Launcher JAR not found at: " + jdtlsPath.resolve("plugins")));

        String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (!javaExecutable.endsWith(".exe")) javaExecutable += ".exe";
        }

        Path projectDir = projectPath.toAbsolutePath().normalize();
        Files.createDirectories(workspaceData);

        String osName = System.getProperty("os.name").toLowerCase();
        String configDirName;
        if (osName.contains("win")) {
            configDirName = "config_win";
        } else if (osName.contains("mac")) {
            configDirName = "config_mac";
        } else {
            configDirName = "config_linux";
        }

        Path configPath = jdtlsPath.resolve(configDirName);

        List<String> command = new ArrayList<>(Arrays.asList(
                javaExecutable,
                Constants.JVM_ENTITY_SIZE_LIMIT,
                Constants.JVM_TOTAL_ENTITY_SIZE_LIMIT,
                "--add-modules=ALL-SYSTEM",
                "--add-opens", "java.base/java.util=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/sun.nio.fs=ALL-UNNAMED",
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

        if (Constants.LSP_LOG_PROTOCOL) {
            command.add("-Dlog.protocol=true");
            command.add("-Dlog.level=" + Constants.LSP_LOG_LEVEL);
        }

        command.addAll(Arrays.asList(
                "-jar", launcherJar.toString(),
                "-configuration", configPath.toString(),
                "-data", workspaceData.toAbsolutePath().normalize().toString()
        ));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false); // Keep stderr separate
        process = pb.start();

        // Consume stderr
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Uncomment to debug JDT internal errors
                    // System.out.println("[JDT LS STDERR] " + line);
                }
            } catch (IOException ignored) {}
        }, "JDT-LS-Stderr-Gobbler").start();

        Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(
                new SimpleLanguageClient(diagnosticsConsumer),
                process.getInputStream(),
                process.getOutputStream()
        );

        launcher.startListening();
        server = launcher.getRemoteProxy();

        InitializeParams init = new InitializeParams();
        init.setProcessId((int) ProcessHandle.current().pid());

        ClientCapabilities capabilities = new ClientCapabilities();
        WorkspaceClientCapabilities workspaceCaps = new WorkspaceClientCapabilities();
        workspaceCaps.setDidChangeConfiguration(new DidChangeConfigurationCapabilities(true));
        workspaceCaps.setWorkspaceFolders(true);
        capabilities.setWorkspace(workspaceCaps);

        TextDocumentClientCapabilities textDocCaps = new TextDocumentClientCapabilities();
        capabilities.setTextDocument(textDocCaps);
        init.setCapabilities(capabilities);

        WorkspaceFolder folder = new WorkspaceFolder(projectDir.toUri().toString());
        init.setWorkspaceFolders(List.of(folder));
        init.setRootUri(folder.getUri());

        try {
            // --- FIX: INCREASED TIMEOUT TO 60 SECONDS ---
            server.initialize(init).get(60, java.util.concurrent.TimeUnit.SECONDS);
            server.initialized(new InitializedParams());
            System.out.println("JDT LS Initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("JDT LS failed to initialize within timeout.");
        }
    }

    public LanguageServer getServer() { return server; }

    public void stop() {
        if (server != null) {
            try { server.shutdown().get(); } catch (Exception ignored) {}
            server.exit();
        }
        if (process != null) process.destroy();
    }

    public static void cleanupWorkspace(Path workspaceData) {
        if (!Files.exists(workspaceData)) return;
        try (Stream<Path> walk = Files.walk(workspaceData)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {}
    }

    static class SimpleLanguageClient implements LanguageClient {
        private final Consumer<PublishDiagnosticsParams> diagnosticsConsumer;
        public SimpleLanguageClient(Consumer<PublishDiagnosticsParams> diagnosticsConsumer) { this.diagnosticsConsumer = diagnosticsConsumer; }
        @Override public void telemetryEvent(Object o) { }
        @Override public void publishDiagnostics(PublishDiagnosticsParams diagnostics) { if (diagnosticsConsumer != null) diagnosticsConsumer.accept(diagnostics); }
        @Override public void showMessage(MessageParams messageParams) { System.out.println("[LSP] " + messageParams.getMessage()); }
        @Override public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) { return CompletableFuture.completedFuture(null); }
        @Override public void logMessage(MessageParams messageParams) { System.out.println("[LSP Log] " + messageParams.getMessage()); }
        @Override public CompletableFuture<Void> registerCapability(RegistrationParams params) { return CompletableFuture.completedFuture(null); }
        @Override public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) { return CompletableFuture.completedFuture(null); }
    }
}