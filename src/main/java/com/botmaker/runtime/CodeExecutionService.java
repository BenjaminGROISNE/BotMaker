package com.botmaker.runtime;

import com.botmaker.config.ApplicationConfig;
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
    private final Consumer<String> statusConsumer;
    private final Consumer<String> setOutputConsumer;
    private final DiagnosticsManager diagnosticsManager;
    private final ApplicationConfig config;

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
            ApplicationConfig config) {
        this.appendOutputConsumer = appendOutputConsumer;
        this.clearOutputConsumer = clearOutputConsumer;
        this.setOutputConsumer = setOutputConsumer;
        this.statusConsumer = statusConsumer;
        this.diagnosticsManager = diagnosticsManager;
        this.config = config;
    }

    /**
     * BLOCKING method to run code.
     * Must be called from a background thread.
     */
    public void runCode(String code) {
        // 1. Validation
        if (diagnosticsManager.hasErrors()) {
            Platform.runLater(() -> statusConsumer.accept("Run aborted due to errors."));
            return;
        }

        if (isRunning.get()) {
            Platform.runLater(() -> statusConsumer.accept("Program is already running. Stop it first."));
            return;
        }

        try {
            Path sourceFilePath = config.getSourceFilePath();
            Path compiledOutputPath = config.getCompiledOutputPath();

            // 2. Compile (Blocking)
            if (!compileAndWait(code, sourceFilePath, compiledOutputPath)) {
                Platform.runLater(() -> statusConsumer.accept("Run aborted due to compilation failure."));
                return;
            }

            // 3. Setup Execution
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

            // 4. Start IO (Leaky Bucket)
            startLeakyBucketReader(currentRunningProcess.getInputStream());
            startLeakyBucketReader(currentRunningProcess.getErrorStream());

            // 5. WAIT FOR PROCESS (BLOCKING)
            // This keeps the caller thread alive until the process finishes or is killed.
            int exitCode = currentRunningProcess.waitFor();

            Platform.runLater(() -> {
                if (exitCode == 0) {
                    statusConsumer.accept("Program completed successfully.");
                } else if (exitCode == 143 || exitCode == 130 || exitCode == 1 || exitCode == -1) {
                    statusConsumer.accept("Program stopped.");
                } else {
                    statusConsumer.accept("Program exited with code: " + exitCode);
                }
            });

        } catch (InterruptedException e) {
            Platform.runLater(() -> statusConsumer.accept("Program stopped by user."));
        } catch (Exception e) {
            Platform.runLater(() -> statusConsumer.accept("Error: " + e.getMessage()));
        } finally {
            // Cleanup
            isRunning.set(false);
            currentRunningProcess = null;
        }
    }

    // ... (compileCode, compileAndWait, stopRunningProgram, isRunning, startLeakyBucketReader remain unchanged)

    public void compileCode(String code) {
        // Need to wrap in thread because we made runCode blocking, but compileCode is usually called from UI
        new Thread(() -> {
            try {
                Platform.runLater(() -> setOutputConsumer.accept("Saving and compiling..."));
                Path sourceFilePath = config.getSourceFilePath();
                Path compiledOutputPath = config.getCompiledOutputPath();
                if (compileAndWait(code, sourceFilePath, compiledOutputPath)) {
                    Platform.runLater(() -> setOutputConsumer.accept("Compilation successful."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> setOutputConsumer.accept("Compilation Error: " + e.getMessage()));
            }
        }).start();
    }

    public boolean compileAndWait(String code, Path sourceFilePath, Path compiledOutputPath) throws IOException, InterruptedException {
        Files.createDirectories(sourceFilePath.getParent());
        Files.writeString(sourceFilePath, code);
        Files.createDirectories(compiledOutputPath);

        String javacExecutable = Paths.get(System.getProperty("java.home"), "bin", "javac").toString();
        ProcessBuilder pb = new ProcessBuilder(javacExecutable, "-g", "-d", compiledOutputPath.toString(), sourceFilePath.toString());
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
            // Force kill is safest for infinite loops
            currentRunningProcess.destroyForcibly();
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    private void startLeakyBucketReader(InputStream inputStream) {
        final StringBuilder buffer = new StringBuilder();
        final ScheduledExecutorService uiUpdater = Executors.newSingleThreadScheduledExecutor();

        uiUpdater.scheduleAtFixedRate(() -> {
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

        new Thread(() -> {
            byte[] readBuf = new byte[1024];
            int len;
            long droppedBytes = 0;
            try {
                while ((len = inputStream.read(readBuf)) != -1) {
                    synchronized (buffer) {
                        if (buffer.length() >= MAX_UI_BUFFER_SIZE) {
                            droppedBytes += len;
                        } else {
                            if (droppedBytes > 0) {
                                buffer.append("\n[... SKIPPED " + droppedBytes + " BYTES ...]\n");
                                droppedBytes = 0;
                            }
                            int spaceLeft = MAX_UI_BUFFER_SIZE - buffer.length();
                            int writeLen = Math.min(len, spaceLeft);
                            buffer.append(new String(readBuf, 0, writeLen, StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                uiUpdater.shutdown();
            }
        }, "Leaky-Reader").start();
    }
}