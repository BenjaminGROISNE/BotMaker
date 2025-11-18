package com.botmaker.runtime;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.validation.DiagnosticsManager;
import com.botmaker.validation.ErrorTranslator;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Consumer;

public class CodeExecutionService {

    private final Consumer<String> appendOutputConsumer;
    private final Runnable clearOutputConsumer;
    private final Consumer<String> statusConsumer;
    private final Consumer<String> setOutputConsumer;
    private final DiagnosticsManager diagnosticsManager;
    private final ApplicationConfig config;
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

    public void runCode(String code) {
        if (diagnosticsManager.hasErrors()) {
            String translatedErrors = com.botmaker.validation.ErrorTranslator.translate(diagnosticsManager.getDiagnostics());
            System.err.println(translatedErrors);
            Platform.runLater(() -> {
                statusConsumer.accept("Run aborted due to errors.");
            });
            return;
        }

        new Thread(() -> {
            try {
                // ADD: Get paths from config
                Path sourceFilePath = config.getSourceFilePath();
                Path compiledOutputPath = config.getCompiledOutputPath();

                if (!compileAndWait(code, sourceFilePath, compiledOutputPath)) {
                    Platform.runLater(() -> statusConsumer.accept("Run aborted due to compilation failure."));
                    return;
                }

                Platform.runLater(() -> {
                    statusConsumer.accept("Running code...");
                    clearOutputConsumer.run();
                });

                String classPath = compiledOutputPath.toString();
                String className = config.getMainClassName();
                String javaExecutable = config.getJavaExecutable();

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, "-cp", classPath, className);
                Process process = pb.start();

                redirectStream(process.getInputStream());
                redirectStream(process.getErrorStream());

                int exitCode = process.waitFor();
                Platform.runLater(() -> statusConsumer.accept("Run finished with exit code: " + exitCode));

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> statusConsumer.accept("Run Error: " + e.getMessage()));
            }
        }).start();
    }

    public void compileCode(String code) {
        if (diagnosticsManager.hasErrors()) {
            String translatedErrors = com.botmaker.validation.ErrorTranslator.translate(diagnosticsManager.getDiagnostics());
            System.err.println(translatedErrors);
            Platform.runLater(() -> {
                statusConsumer.accept("Compilation failed. See errors above.");
            });
            return;
        }

        new Thread(() -> {
            try {
                Platform.runLater(() -> setOutputConsumer.accept("Saving and compiling..."));

                // ADD: Get paths from config
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
        // Ensure parent directories exist
        Files.createDirectories(sourceFilePath.getParent());
        Files.writeString(sourceFilePath, code);

        // Ensure output directory exists
        Files.createDirectories(compiledOutputPath);

        String javacExecutable = Paths.get(System.getProperty("java.home"), "bin", "javac").toString();
        // Add -g to include debug information for the debugger
        ProcessBuilder pb = new ProcessBuilder(javacExecutable, "-g", "-d", compiledOutputPath.toString(), sourceFilePath.toString());
        Process process = pb.start();

        String errors = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return true;
        } else {
            final String errorMessage = "Compilation Failed:\n" + errors;
            System.err.println(errorMessage);
            Platform.runLater(() -> {
                setOutputConsumer.accept(errorMessage);
            });
            return false;
        }
    }

    private void redirectStream(InputStream stream) {
        new Thread(() -> {
            try (Scanner s = new Scanner(stream)) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    Platform.runLater(() -> appendOutputConsumer.accept(line + "\n"));
                }
            }
        }).start();
    }
}
