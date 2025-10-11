package com.botmaker;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.LocatableEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private LanguageServer jdtServer;
    private final BlockFactory factory = new BlockFactory();
    private final VBox blocksContainer = new VBox(10);
    private final Label statusLabel = new Label("Ready");
    private TextArea outputArea;

    private Button debugButton;
    private Button resumeButton;

    private DebuggerService debuggerService;

    private String docUri;
    private String currentCode;
    private long docVersion = 1;

    // Fields for debugging UI
    private Map<ASTNode, CodeBlock> nodeToBlockMap;
    private CodeBlock highlightedBlock;

    @Override
    public void start(Stage primaryStage) throws Exception {
        JdtLanguageServerLauncher launcher = new JdtLanguageServerLauncher(Paths.get("tools/jdt-language-server"));
        jdtServer = launcher.getServer();

        Path docPath = Paths.get("projects/Demo.java").toAbsolutePath();
        docUri = docPath.toUri().toString();
        currentCode = Files.readString(docPath);

        jdtServer.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(docUri, "java", (int) docVersion, currentCode)
        ));

        refreshUI(currentCode);

        Button compileButton = new Button("Compile");
        compileButton.setOnAction(e -> compileCode());

        Button runButton = new Button("Run");
        runButton.setOnAction(e -> runCode());

        debugButton = new Button("Debug");
        debugButton.setOnAction(e -> startDebugging());

        resumeButton = new Button("Resume");
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> {
            if (debuggerService != null) {
                resumeButton.setDisable(true); // Disable until next pause event
                debuggerService.resume();
            }
        });

        HBox buttonBox = new HBox(10, compileButton, runButton, debugButton, resumeButton);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        ScrollPane scrollPane = new ScrollPane(blocksContainer);
        scrollPane.setFitToWidth(true);
        VBox root = new VBox(10, buttonBox, scrollPane, outputArea, statusLabel);
        root.setPadding(new javafx.geometry.Insets(10));
        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.setTitle("BotMaker Blocks");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            launcher.stop();
            Platform.exit();
        });
    }

    private void handleCodeUpdate(String newCode) {
        this.docVersion++;
        jdtServer.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(docUri, (int) docVersion),
                List.of(new TextDocumentContentChangeEvent(newCode))
        ));
        refreshUI(newCode);
    }

    private void refreshUI(String javaCode) {
        this.currentCode = javaCode;
        this.nodeToBlockMap = new HashMap<>(); // Reset map
        blocksContainer.getChildren().clear();

        CompletionContext context = new CompletionContext(
                jdtServer,
                docUri,
                currentCode,
                docVersion,
                this::handleCodeUpdate
        );

        BodyBlock rootBlock = factory.convert(javaCode, nodeToBlockMap);

        if (rootBlock != null) {
            blocksContainer.getChildren().add(rootBlock.getUINode(context));
        }
        statusLabel.setText("UI Refreshed.");
    }

    private void startDebugging() {
        new Thread(() -> {
            try {
                if (!compileAndWait()) {
                    Platform.runLater(() -> statusLabel.setText("Debug aborted due to compilation failure."));
                    return;
                }

                // Get all unique line numbers from the code blocks to set breakpoints
                CompilationUnit cu = factory.getCompilationUnit();
                if (cu == null) {
                    Platform.runLater(() -> statusLabel.setText("Error: Could not parse code to get breakpoints."));
                    return;
                }
                Set<Integer> breakpointLines = nodeToBlockMap.values().stream()
                        .map(block -> cu.getLineNumber(block.getAstNode().getStartPosition()))
                        .collect(Collectors.toSet());

                Platform.runLater(() -> {
                    statusLabel.setText("Starting debugger...");
                    debugButton.setDisable(true);
                    outputArea.clear();
                });

                String classPath = "build/compiled";
                String className = "Demo";
                String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
                String debugAgent = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000";

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, debugAgent, "-cp", classPath, className);
                Process process = pb.start();

                new Thread(() -> {
                    try (Scanner s = new Scanner(process.getInputStream())) {
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            Platform.runLater(() -> outputArea.appendText(line + "\n"));
                        }
                    }
                }).start();
                new Thread(() -> {
                    try (Scanner s = new Scanner(process.getErrorStream())) {
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            Platform.runLater(() -> outputArea.appendText(line + "\n"));
                        }
                    }
                }).start();

                debuggerService = new DebuggerService();
                debuggerService.setOnPause(this::handlePauseEvent);
                debuggerService.setOnDisconnect(this::onDebugSessionFinished);
                debuggerService.connectAndRun(className, new ArrayList<>(breakpointLines));

            } catch (IOException | IllegalConnectorArgumentsException | InterruptedException e) {
                Platform.runLater(() -> statusLabel.setText("Debugger Error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void runCode() {
        new Thread(() -> {
            try {
                if (!compileAndWait()) {
                    Platform.runLater(() -> statusLabel.setText("Run aborted due to compilation failure."));
                    return;
                }

                Platform.runLater(() -> {
                    statusLabel.setText("Running code...");
                    outputArea.clear();
                });

                String classPath = "build/compiled";
                String className = "Demo";
                String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, "-cp", classPath, className);
                Process process = pb.start();

                new Thread(() -> {
                    try (Scanner s = new Scanner(process.getInputStream())) {
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            Platform.runLater(() -> outputArea.appendText(line + "\n"));
                        }
                    }
                }).start();
                new Thread(() -> {
                    try (Scanner s = new Scanner(process.getErrorStream())) {
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            Platform.runLater(() -> outputArea.appendText(line + "\n"));
                        }
                    }
                }).start();

                int exitCode = process.waitFor();
                Platform.runLater(() -> statusLabel.setText("Run finished with exit code: " + exitCode));

            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> statusLabel.setText("Run Error: " + e.getMessage()));
            }
        }).start();
    }

    private void handlePauseEvent(LocatableEvent event) {
        Platform.runLater(() -> {
            System.out.println("UI Thread: Pause event received. Enabling resume button.");
            resumeButton.setDisable(false); // Re-enable the button

            CompilationUnit cu = factory.getCompilationUnit();
            if (cu == null) {
                System.out.println("UI Thread: CompilationUnit is null, cannot update highlight.");
                return;
            }

            int lineNumber = event.location().lineNumber();
            int offset = cu.getPosition(lineNumber, 0);

            ASTNode node = NodeFinder.perform(cu, offset, 1);

            if (node != null) {
                CodeBlock block = findBlockForNode(node);
                highlightBlock(block);
                statusLabel.setText("Paused at line: " + lineNumber);
            }
        });
    }

    private void onDebugSessionFinished() {
        Platform.runLater(() -> {
            statusLabel.setText("Debug session finished.");
            debugButton.setDisable(false);
            resumeButton.setDisable(true);
            highlightBlock(null); // Clear highlight
        });
    }

    private CodeBlock findBlockForNode(ASTNode node) {
        ASTNode currentNode = node;
        while (currentNode != null) {
            if (nodeToBlockMap.containsKey(currentNode)) {
                return nodeToBlockMap.get(currentNode);
            }
            currentNode = currentNode.getParent();
        }
        return null;
    }

    private void highlightBlock(CodeBlock block) {
        if (highlightedBlock != null) {
            highlightedBlock.unhighlight();
        }
        highlightedBlock = block;
        if (highlightedBlock != null) {
            highlightedBlock.highlight();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void compileCode() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> outputArea.setText("Saving and compiling..."));
                if (compileAndWait()) {
                    Platform.runLater(() -> outputArea.setText("Compilation successful."));
                } else {
                    Platform.runLater(() -> outputArea.setText("Compilation Failed."));
                }
            } catch (IOException | InterruptedException e) {
                Platform.runLater(() -> outputArea.setText("Compilation Error: " + e.getMessage()));
            }
        }).start();
    }

    private boolean compileAndWait() throws IOException, InterruptedException {
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
            return true;
        }
        else {
            Platform.runLater(() -> outputArea.setText("Compilation Failed:\n" + errors));
            return false;
        }
    }
}
