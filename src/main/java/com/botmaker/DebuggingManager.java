package com.botmaker;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.LocatableEvent;
import javafx.application.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class DebuggingManager {

    private final CodeExecutionService codeExecutionService;
    private final Consumer<String> statusConsumer;
    private final Consumer<String> appendOutputConsumer;
    private final Runnable clearOutputConsumer;
    private final Runnable onDebugStart;
    private final Runnable onPause;
    private final Runnable onResume;
    private final Runnable onDebugFinish;
    private final Consumer<CodeBlock> highlightConsumer;
    private final BlockFactory factory;
    private Map<ASTNode, CodeBlock> nodeToBlockMap; // This needs to be updated from Main

    private DebuggerService debuggerService;
    private Map<Integer, CodeBlock> lineToBlockMap;

    public DebuggingManager(CodeExecutionService codeExecutionService,
                            Consumer<String> statusConsumer,
                            Consumer<String> appendOutputConsumer,
                            Runnable clearOutputConsumer,
                            Runnable onDebugStart,
                            Runnable onPause,
                            Runnable onResume,
                            Runnable onDebugFinish,
                            Consumer<CodeBlock> highlightConsumer,
                            BlockFactory factory) {
        this.codeExecutionService = codeExecutionService;
        this.statusConsumer = statusConsumer;
        this.appendOutputConsumer = appendOutputConsumer;
        this.clearOutputConsumer = clearOutputConsumer;
        this.onDebugStart = onDebugStart;
        this.onPause = onPause;
        this.onResume = onResume;
        this.onDebugFinish = onDebugFinish;
        this.highlightConsumer = highlightConsumer;
        this.factory = factory;
    }

    public void setNodeToBlockMap(Map<ASTNode, CodeBlock> nodeToBlockMap) {
        this.nodeToBlockMap = nodeToBlockMap;
    }

    public void startDebugging(String code) {
        new Thread(() -> {
            try {
                if (!codeExecutionService.compileAndWait(code)) {
                    Platform.runLater(() -> statusConsumer.accept("Debug aborted due to compilation failure."));
                    return;
                }

                CompilationUnit cu = factory.getCompilationUnit();
                if (cu == null || nodeToBlockMap == null) {
                    Platform.runLater(() -> statusConsumer.accept("Error: Could not parse code to get breakpoints."));
                    return;
                }

                this.lineToBlockMap = new HashMap<>();
                for (CodeBlock block : nodeToBlockMap.values()) {
                    int line = block.getBreakpointLine(cu);
                    if (line > 0) {
                        if (!lineToBlockMap.containsKey(line) || block instanceof StatementBlock) {
                            lineToBlockMap.put(line, block);
                        }
                    }
                }
                List<Integer> breakpointLines = new ArrayList<>(this.lineToBlockMap.keySet());

                int freePort;
                try (ServerSocket socket = new ServerSocket(0)) {
                    freePort = socket.getLocalPort();
                }

                Platform.runLater(() -> {
                    statusConsumer.accept("Starting debugger on port " + freePort + "...");
                    onDebugStart.run();
                    clearOutputConsumer.run();
                });

                String classPath = "build/compiled";
                String className = "Demo";
                String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
                String debugAgent = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", freePort);

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, debugAgent, "-cp", classPath, className);
                Process process = pb.start();

                redirectStream(process.getInputStream());
                redirectStream(process.getErrorStream());

                debuggerService = new DebuggerService();
                debuggerService.setOnPause(this::handlePauseEvent);
                debuggerService.setOnDisconnect(this::onDebugSessionFinished);
                debuggerService.connectAndRun(className, freePort, breakpointLines);

            } catch (IOException | IllegalConnectorArgumentsException | InterruptedException e) {
                Platform.runLater(() -> statusConsumer.accept("Debugger Error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    public void resume() {
        if (debuggerService != null) {
            Platform.runLater(onResume);
            debuggerService.resume();
        }
    }

    private void handlePauseEvent(LocatableEvent event) {
        Platform.runLater(() -> {
            onPause.run();
            int lineNumber = event.location().lineNumber();
            CodeBlock block = lineToBlockMap.get(lineNumber);

            if (block != null) {
                CodeBlock target = block.getHighlightTarget();
                highlightConsumer.accept(target);
                statusConsumer.accept("Paused at line: " + lineNumber);
            } else {
                statusConsumer.accept("Paused at line: " + lineNumber + " (No block found)");
            }
        });
    }

    private void onDebugSessionFinished() {
        Platform.runLater(() -> {
            statusConsumer.accept("Debug session finished.");
            onDebugFinish.run();
            highlightConsumer.accept(null); // Clear highlight
        });
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
