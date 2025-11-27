package com.botmaker.runtime;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.project.ProjectFile;
import com.botmaker.state.ApplicationState;
import com.botmaker.validation.DiagnosticsManager;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CodeExecutionService {

    private final Consumer<String> appendOutputConsumer;
    private final Runnable clearOutputConsumer;
    private final Consumer<String> setOutputConsumer;
    private final Consumer<String> statusConsumer;
    private final DiagnosticsManager diagnosticsManager;
    private final ApplicationConfig config;
    private final ApplicationState state;

    private volatile Process currentRunningProcess;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private static final int MAX_UI_BUFFER_SIZE = 4096;
    private static final int UI_UPDATE_RATE_MS = 100;

    public CodeExecutionService(
            Consumer<String> appendOutputConsumer,
            Runnable clearOutputConsumer,
            Consumer<String> setOutputConsumer,
            Consumer<String> statusConsumer,
            DiagnosticsManager diagnosticsManager,
            ApplicationConfig config,
            ApplicationState state) {
        this.appendOutputConsumer = appendOutputConsumer;
        this.clearOutputConsumer = clearOutputConsumer;
        this.setOutputConsumer = setOutputConsumer;
        this.statusConsumer = statusConsumer;
        this.diagnosticsManager = diagnosticsManager;
        this.config = config;
        this.state = state;
    }

    public void runCode(String currentEditorCode) {
        if (diagnosticsManager.hasErrors()) {
            Platform.runLater(() -> statusConsumer.accept("Run aborted due to errors."));
            return;
        }

        if (isRunning.get()) {
            Platform.runLater(() -> statusConsumer.accept("Program is already running. Stop it first."));
            return;
        }

        try {
            Path compiledOutputPath = config.getCompiledOutputPath();

            // 1. Compile (Blocking)
            if (!compileAndWait(currentEditorCode, compiledOutputPath)) {
                Platform.runLater(() -> statusConsumer.accept("Run aborted due to compilation failure."));
                return;
            }

            // 2. Setup Execution
            Platform.runLater(() -> {
                statusConsumer.accept("Running... (Press Stop to terminate)");
                clearOutputConsumer.run();
            });

            isRunning.set(true);

            String classPath = compiledOutputPath.toString();
            String className = config.getMainClassName();
            String javaExecutable = config.getJavaExecutable();

            ProcessBuilder pb = new ProcessBuilder(javaExecutable, "-cp", classPath, className);
            currentRunningProcess = pb.start();

            // 3. Start IO Readers and keep references to them
            Thread outReader = startLeakyBucketReader(currentRunningProcess.getInputStream());
            Thread errReader = startLeakyBucketReader(currentRunningProcess.getErrorStream());

            // 4. Wait for process to exit
            int exitCode = currentRunningProcess.waitFor();

            // 5. CRITICAL FIX: Wait for readers to drain the stream before stopping UI updates
            try {
                outReader.join(1000); // Wait up to 1s for streams to flush
                errReader.join(1000);
            } catch (InterruptedException ignored) {}

            Platform.runLater(() -> {
                if (exitCode == 0) statusConsumer.accept("Program completed successfully.");
                else if (exitCode == 143 || exitCode == 130 || exitCode == 1 || exitCode == -1) statusConsumer.accept("Program stopped.");
                else statusConsumer.accept("Program exited with code: " + exitCode);
            });

        } catch (InterruptedException e) {
            Platform.runLater(() -> statusConsumer.accept("Program stopped by user."));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusConsumer.accept("Error: " + e.getMessage()));
        } finally {
            isRunning.set(false); // NOW safe to stop UI updates
            currentRunningProcess = null;
        }
    }

    public void compileCode(String code) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> setOutputConsumer.accept("Saving and compiling..."));
                Path compiledOutputPath = config.getCompiledOutputPath();
                if (compileAndWait(code, compiledOutputPath)) {
                    Platform.runLater(() -> setOutputConsumer.accept("Compilation successful."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> setOutputConsumer.accept("Compilation Error: " + e.getMessage()));
            }
        }).start();
    }

    public boolean compileAndWait(String currentActiveCode, Path compiledOutputPath) throws IOException, InterruptedException {
        // Sync memory
        state.setCurrentCode(currentActiveCode);

        // Save ALL files to disk
        for (ProjectFile file : state.getAllFiles()) {
            Path path = file.getPath();
            if (path != null) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, file.getContent());
            }
        }

        Files.createDirectories(compiledOutputPath);

        String javacExecutable = Paths.get(System.getProperty("java.home"), "bin", "javac").toString();

        // Calculate source root (3 levels up from Main file: com/pkg/Main.java -> src/main/java)
        Path sourcePathRoot = config.getSourceFilePath().getParent().getParent().getParent();

        ProcessBuilder pb = new ProcessBuilder(
                javacExecutable,
                "-g",
                "-d", compiledOutputPath.toString(),
                "-sourcepath", sourcePathRoot.toString(),
                config.getSourceFilePath().toString()
        );

        Process process = pb.start();

        String errors = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            Platform.runLater(() -> setOutputConsumer.accept("Compilation Failed:\n" + errors));
            return false;
        }
        return true;
    }

    public void stopRunningProgram() {
        if (currentRunningProcess != null && currentRunningProcess.isAlive()) {
            currentRunningProcess.destroyForcibly();
        }
    }

    public boolean isRunning() { return isRunning.get(); }

    // CHANGED: Returns the Thread so we can join() it
    private Thread startLeakyBucketReader(InputStream inputStream) {
        final StringBuilder buffer = new StringBuilder();
        final ScheduledExecutorService uiUpdater = Executors.newSingleThreadScheduledExecutor();

        // UI Pusher Loop
        uiUpdater.scheduleAtFixedRate(() -> {
            // Only stop if process is dead AND buffer is empty
            if (!isRunning.get() && buffer.length() == 0) {
                uiUpdater.shutdown();
                return;
            }

            String textToSend = "";
            synchronized (buffer) {
                if (buffer.length() > 0) {
                    textToSend = buffer.toString();
                    buffer.setLength(0);
                }
            }

            if (!textToSend.isEmpty()) {
                String finalTx = textToSend;
                Platform.runLater(() -> appendOutputConsumer.accept(finalTx));
            }
        }, UI_UPDATE_RATE_MS, UI_UPDATE_RATE_MS, TimeUnit.MILLISECONDS);

        // Stream Reader Thread
        Thread readerThread = new Thread(() -> {
            byte[] readBuf = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(readBuf)) != -1) {
                    synchronized (buffer) {
                        if (buffer.length() < MAX_UI_BUFFER_SIZE) {
                            buffer.append(new String(readBuf, 0, len, StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                // Do not shut down UI updater here; let the scheduler decide based on buffer/isRunning state
            }
        }, "Leaky-Reader");

        readerThread.start();
        return readerThread;
    }
}