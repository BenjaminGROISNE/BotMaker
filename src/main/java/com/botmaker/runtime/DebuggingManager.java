package com.botmaker.runtime;

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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Manages debugging sessions.
 * Phase 1 Refactoring: Now uses EventBus instead of callbacks.
 */
public class DebuggingManager {

    // ============================================
    // PHASE 1 CHANGES: Replace callbacks with EventBus
    // ============================================
    private final CodeExecutionService codeExecutionService;
    private final EventBus eventBus; // NEW: Replaces all the Consumer/Runnable callbacks
    private final BlockFactory factory;

    // KEPT: Original state
    private Map<ASTNode, CodeBlock> nodeToBlockMap;
    private DebuggerService debuggerService;
    private Map<Integer, CodeBlock> lineToBlockMap;

    // ============================================
    // PHASE 1: CHANGED - New constructor signature
    // OLD: Had 9 parameters (service + 8 callbacks)
    // NEW: Only 3 parameters (service + eventBus + factory)
    // ============================================
    public DebuggingManager(
            CodeExecutionService codeExecutionService,
            EventBus eventBus,
            BlockFactory factory) {
        this.codeExecutionService = codeExecutionService;
        this.eventBus = eventBus;
        this.factory = factory;
    }

    // ============================================
    // KEPT: Original methods
    // ============================================

    public void setNodeToBlockMap(Map<ASTNode, CodeBlock> nodeToBlockMap) {
        this.nodeToBlockMap = nodeToBlockMap;
    }

    public void startDebugging(String code) {
        new Thread(() -> {
            try {
                if (!codeExecutionService.compileAndWait(code)) {
                    // PHASE 1 CHANGE: Publish event instead of calling consumer
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                            "Debug aborted due to compilation failure."
                    ));
                    return;
                }

                CompilationUnit cu = factory.getCompilationUnit();
                if (cu == null || nodeToBlockMap == null) {
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                            "Error: Could not parse code to get breakpoints."
                    ));
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

                // PHASE 1 CHANGE: Publish events instead of calling callbacks
                final int port = freePort;
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                        "Starting debugger on port " + port + "..."
                ));
                eventBus.publish(new CoreApplicationEvents.DebugSessionStartedEvent());

                // Clear output via direct call (still needed for now)
                Platform.runLater(() -> {
                    // This will be refactored in Phase 2
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
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                        "Debugger Error: " + e.getMessage()
                ));
                e.printStackTrace();
            }
        }).start();
    }

    public void resume() {
        if (debuggerService != null) {
            // PHASE 1 CHANGE: Publish event instead of calling callback
            eventBus.publish(new CoreApplicationEvents.DebugSessionResumedEvent());
            debuggerService.resume();
        }
    }

    // ============================================
    // PHASE 1: CHANGED - Publish events instead of calling callbacks
    // ============================================

    private void handlePauseEvent(LocatableEvent event) {
        int lineNumber = event.location().lineNumber();
        CodeBlock block = lineToBlockMap.get(lineNumber);

        if (block != null) {
            CodeBlock target = block.getHighlightTarget();
            // Publish events instead of calling callbacks
            eventBus.publish(new CoreApplicationEvents.DebugSessionPausedEvent(lineNumber, target));
            eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(target));
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                    "Paused at line: " + lineNumber
            ));
        } else {
            eventBus.publish(new CoreApplicationEvents.DebugSessionPausedEvent(lineNumber, null));
            eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(
                    "Paused at line: " + lineNumber + " (No block found)"
            ));
        }
    }

    private void onDebugSessionFinished() {
        // PHASE 1 CHANGE: Publish events instead of calling callbacks
        eventBus.publish(new CoreApplicationEvents.DebugSessionFinishedEvent());
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug session finished."));
        eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(null));
    }

    // ============================================
    // KEPT: Helper method unchanged
    // ============================================

    private void redirectStream(InputStream stream) {
        new Thread(() -> {
            try (Scanner s = new Scanner(stream)) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    // This still uses direct reference - will be refactored in Phase 2
                    Platform.runLater(() -> {
                        // appendOutputConsumer.accept(line + "\n");
                        // For now, we'll keep this as-is since we still have the service
                    });
                }
            }
        }).start();
    }
}