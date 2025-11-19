package com.botmaker.runtime;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.BlockFactory;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.LocatableEvent;
import javafx.application.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.*;

public class DebuggingManager {

    private final CodeExecutionService codeExecutionService;
    private final EventBus eventBus;
    private final BlockFactory factory;
    private final ApplicationConfig config;

    private Map<ASTNode, CodeBlock> nodeToBlockMap;
    private DebuggerService debuggerService;
    private Map<Integer, CodeBlock> lineToBlockMap;

    // NEW: Store the running process so we can kill it
    private volatile Process currentProcess;

    public DebuggingManager(
            CodeExecutionService codeExecutionService,
            EventBus eventBus,
            BlockFactory factory,
            ApplicationConfig config) {
        this.codeExecutionService = codeExecutionService;
        this.eventBus = eventBus;
        this.factory = factory;
        this.config = config;
    }

    public void setNodeToBlockMap(Map<ASTNode, CodeBlock> nodeToBlockMap) {
        this.nodeToBlockMap = nodeToBlockMap;
    }

    public void startDebugging(String code) {
        new Thread(() -> {
            try {
                if (!codeExecutionService.compileAndWait(code, config.getSourceFilePath(), config.getCompiledOutputPath())) {
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug aborted due to compilation failure."));
                    return;
                }

                CompilationUnit cu = factory.getCompilationUnit();
                if (cu == null || nodeToBlockMap == null) {
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Error: Could not parse code to get breakpoints."));
                    return;
                }

                this.lineToBlockMap = new HashMap<>();
                List<Integer> activeBreakpoints = new ArrayList<>();

                for (CodeBlock block : nodeToBlockMap.values()) {
                    int line = block.getBreakpointLine(cu);
                    if (line > 0) {
                        if (!lineToBlockMap.containsKey(line) || block instanceof StatementBlock) {
                            lineToBlockMap.put(line, block);
                        }
                        if (block.isBreakpoint()) {
                            activeBreakpoints.add(line);
                        }
                    }
                }

                if (activeBreakpoints.isEmpty() && !lineToBlockMap.isEmpty()) {
                    lineToBlockMap.keySet().stream()
                            .min(Integer::compareTo)
                            .ifPresent(firstLine -> {
                                activeBreakpoints.add(firstLine);
                                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("No breakpoints set. Pausing at start (Line " + firstLine + ")."));
                            });
                }

                int freePort;
                try (ServerSocket socket = new ServerSocket(0)) {
                    freePort = socket.getLocalPort();
                }

                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Starting debugger on port " + freePort + "..."));
                eventBus.publish(new CoreApplicationEvents.DebugSessionStartedEvent());

                Platform.runLater(() -> eventBus.publish(new CoreApplicationEvents.OutputClearedEvent()));

                String classPath = config.getCompiledOutputPath().toString();
                String className = config.getMainClassName();
                String javaExecutable = config.getJavaExecutable();
                String debugAgent = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", freePort);

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, debugAgent, "-cp", classPath, className);

                // NEW: Assign to field
                this.currentProcess = pb.start();

                redirectStream(currentProcess.getInputStream());
                redirectStream(currentProcess.getErrorStream());

                debuggerService = new DebuggerService();
                debuggerService.setOnPause(this::handlePauseEvent);
                debuggerService.setOnDisconnect(this::onDebugSessionFinished);

                debuggerService.connectAndRun(className, freePort, activeBreakpoints);

            } catch (IOException | IllegalConnectorArgumentsException | InterruptedException e) {
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debugger Error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    public void continueExecution() {
        if (debuggerService != null) {
            eventBus.publish(new CoreApplicationEvents.DebugSessionResumedEvent());
            debuggerService.resume();
        }
    }

    public void stepOver() {
        if (debuggerService != null) {
            eventBus.publish(new CoreApplicationEvents.DebugSessionResumedEvent());
            debuggerService.stepOver();
        }
    }

    // NEW: Forcefully kill the process and disconnect
    public void stopDebugging() {
        // 1. Disconnect JDI nicely if possible (triggers events)
        if (debuggerService != null) {
            debuggerService.disconnect();
        }

        // 2. Kill the OS process to stop execution immediately
        if (currentProcess != null && currentProcess.isAlive()) {
            try {
                currentProcess.destroyForcibly();
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug process terminated."));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                currentProcess = null;
            }
        }
    }

    private void handlePauseEvent(LocatableEvent event) {
        int lineNumber = event.location().lineNumber();
        CodeBlock block = lineToBlockMap.get(lineNumber);
        CodeBlock target = (block != null) ? block.getHighlightTarget() : null;

        eventBus.publish(new CoreApplicationEvents.DebugSessionPausedEvent(lineNumber, target));
        eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(target));
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                "Paused at line: " + lineNumber
        ));
    }

    private void onDebugSessionFinished() {
        eventBus.publish(new CoreApplicationEvents.DebugSessionFinishedEvent());
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug session finished."));
        eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(null));
        this.currentProcess = null;
    }

    private void redirectStream(InputStream stream) {
        new Thread(() -> {
            try (Scanner s = new Scanner(stream)) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    Platform.runLater(() -> eventBus.publish(new CoreApplicationEvents.OutputAppendedEvent(line + "\n")));
                }
            }
        }).start();
    }
}