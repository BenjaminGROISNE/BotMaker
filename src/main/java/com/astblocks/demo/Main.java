package com.astblocks.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main extends Application {

    private LanguageServer jdtServer;
    private AstToBlocksConverter converter = new AstToBlocksConverter();
    private VBox blocksContainer = new VBox(10);
    private Label statusLabel = new Label("Ready");
    private TextArea outputArea;

    private String docUri;
    private String currentCode;
    private long docVersion = 1;
    private boolean isDirty = true;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Start JDT Language Server
        JdtLanguageServerLauncher launcher = new JdtLanguageServerLauncher(Paths.get("tools/jdt-language-server"));
        jdtServer = launcher.getServer();

        // 2. Load the project file
        Path docPath = Paths.get("projects/Demo.java").toAbsolutePath();
        docUri = docPath.toUri().toString();
        currentCode = Files.readString(docPath);

        // 3. Tell JDT LS that the file exists
        jdtServer.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(docUri, "java", (int) docVersion++, currentCode)
        ));

        // 4. Initial UI Render
        refreshUI(currentCode);

        // 5. Layout
        Button compileButton = new Button("Compile");
        compileButton.setOnAction(e -> compileCode());
        Button runButton = new Button("Run");
        runButton.setOnAction(e -> runCode());
        HBox buttonBox = new HBox(10, compileButton, runButton);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        ScrollPane scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        VBox root = new VBox(10, buttonBox, scrollPane, outputArea, statusLabel);
        root.setPadding(new javafx.geometry.Insets(10));
        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.setTitle("AST Blocks Demo");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            launcher.stop();
            Platform.exit();
        });
    }

    private void refreshUI(String javaCode) {
        this.currentCode = javaCode;
        blocksContainer.getChildren().clear();

        CodeBlock rootBlock = converter.convert(javaCode);
        BlockUI rootBlockUI = buildBlockUI(rootBlock);

        blocksContainer.getChildren().add(rootBlockUI);
        statusLabel.setText("UI Refreshed.");
    }

    private BlockUI buildBlockUI(CodeBlock block) {
        BlockUI ui = new BlockUI(block, this::requestCompletionsForBlock);
        for (CodeBlock child : block.getChildren()) {
            BlockUI childUi = buildBlockUI(child);
            ui.add_child_ui(childUi);
        }
        return ui;
    }

    private void requestCompletionsForBlock(BlockUI blockUI) {
        try {
            // Position for completion is the start of the block's node
            int offset = blockUI.getCodeBlock().getAstNode().getStartPosition();
            Position pos = getPositionFromOffset(currentCode, offset);

            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(docUri), pos);

            jdtServer.getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) || (result.isRight() && result.getRight().getItems().isEmpty())) {
                    Platform.runLater(() -> statusLabel.setText("No completions found."));
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    for (CompletionItem item : items) {
                        // Filter for variables and keywords for simplicity
                        if (item.getKind() == CompletionItemKind.Variable || item.getKind() == CompletionItemKind.Keyword) {
                            MenuItem mi = new MenuItem(item.getLabel());
                            mi.setOnAction(e -> applyCompletion(blockUI, item));
                            menu.getItems().add(mi);
                        }
                    }
                    if (!menu.getItems().isEmpty()) {
                        blockUI.showCompletions(menu);
                        statusLabel.setText("Showing suggestions for: " + blockUI.getCodeBlock().getCode());
                    } else {
                        statusLabel.setText("No relevant suggestions found.");
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error getting completions: " + e.getMessage());
        }
    }

    private void applyCompletion(BlockUI blockUI, CompletionItem item) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            int start = blockUI.getCodeBlock().getAstNode().getStartPosition();
            int end = start + blockUI.getCodeBlock().getAstNode().getLength();

            String newCode = currentCode.substring(0, start) + insertText + currentCode.substring(end);

            // Notify server of change
            jdtServer.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                    new VersionedTextDocumentIdentifier(docUri, (int) docVersion++),
                    List.of(new TextDocumentContentChangeEvent(newCode))
            ));

            // Re-render the entire UI
            refreshUI(newCode);
            statusLabel.setText("Applied suggestion: " + item.getLabel());
            isDirty = true;

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error applying completion: " + e.getMessage());
        }
    }

    private Position getPositionFromOffset(String code, int offset) {
        int line = 0;
        int lastNewline = -1;
        for (int i = 0; i < offset; i++) {
            if (code.charAt(i) == '\n') {
                line++;
                lastNewline = i;
            }
        }
        int character = offset - lastNewline - 1;
        return new Position(line, character);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void compileCode() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> outputArea.setText("Saving and compiling..."));

                // Save the current in-memory code to the file before compiling
                Path sourceFile = Paths.get("projects/Demo.java");
                Files.writeString(sourceFile, currentCode);

                String sourcePath = sourceFile.toString();
                String outDir = "build/compiled";
                Files.createDirectories(Paths.get(outDir));

                String javacExecutable = Paths.get(System.getProperty("java.home"), "bin", "javac").toString();
                ProcessBuilder pb = new ProcessBuilder(javacExecutable, "-d", outDir, sourcePath);
                Process process = pb.start();

                String errors = new String(process.getErrorStream().readAllBytes());
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    Platform.runLater(() -> outputArea.setText("Compilation successful."));
                    isDirty = false;
                } else {
                    Platform.runLater(() -> outputArea.setText("Compilation Failed:\n" + errors));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> outputArea.setText("Compilation Error: " + e.getMessage()));
            }
        }).start();
    }

    private void runCode() {
        new Thread(() -> {
            try {
                // First, compile if the code is dirty
                if (isDirty) {
                    Platform.runLater(() -> outputArea.setText("Changes detected, compiling..."));
                    if (!compileAndWait()) {
                        Platform.runLater(() -> outputArea.setText("Run aborted due to compilation failure."));
                        return; // Abort if compilation fails
                    }
                }

                Platform.runLater(() -> outputArea.setText("Running..."));

                String classPath = "build/compiled";
                String className = "Demo";

                String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
                ProcessBuilder pb = new ProcessBuilder(javaExecutable, "-cp", classPath, className);
                Process process = pb.start();

                String output = new String(process.getInputStream().readAllBytes());
                String errors = new String(process.getErrorStream().readAllBytes());
                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    if (exitCode == 0) {
                        outputArea.setText("--- Run Output ---\n" + output);
                    } else {
                        outputArea.setText("--- Run Failed ---\n" + errors);
                    }
                });

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> outputArea.setText("Execution Error: " + e.getMessage()));
            }
        }).start();
    }

    // A synchronous helper method for compilation
    private boolean compileAndWait() throws IOException, InterruptedException {
        Path sourceFile = Paths.get("projects/Demo.java");
        Files.writeString(sourceFile, currentCode);

        String sourcePath = sourceFile.toString();
        String outDir = "build/compiled";
        Files.createDirectories(Paths.get(outDir));

        String javacExecutable = Paths.get(System.getProperty("java.home"), "bin", "javac").toString();
        ProcessBuilder pb = new ProcessBuilder(javacExecutable, "-d", outDir, sourcePath);
        Process process = pb.start();

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            isDirty = false;
            return true;
        } else {
            String errors = new String(process.getErrorStream().readAllBytes());
            Platform.runLater(() -> outputArea.setText("Compilation Failed:\n" + errors));
            return false;
        }
    }
}