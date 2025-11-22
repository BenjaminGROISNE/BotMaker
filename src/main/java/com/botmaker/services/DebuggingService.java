package com.botmaker.services;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.config.Constants;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.BlockFactory;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.state.ApplicationState;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import javafx.application.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Handles the entire debugging lifecycle:
 * 1. Mapping AST nodes to line numbers.
 * 2. Launching the JVM in debug mode.
 * 3. Attaching via JDI (Java Debug Interface).
 * 4. Managing Breakpoints, Stepping, and Resuming.
 */
public class DebuggingService {

    // Console Coloring for internal logs
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";

    private final ApplicationState state;
    private final EventBus eventBus;
    private final CodeExecutionService codeExecutionService;
    private final BlockFactory factory;
    private final ApplicationConfig config;

    // Debug Session State
    private volatile Process currentProcess;
    private VirtualMachine vm;
    private ThreadReference currentDebugThread;
    private Map<Integer, CodeBlock> lineToBlockMap;

    public DebuggingService(
            ApplicationState state,
            EventBus eventBus,
            CodeExecutionService codeExecutionService,
            BlockFactory factory,
            ApplicationConfig config) {
        this.state = state;
        this.eventBus = eventBus;
        this.codeExecutionService = codeExecutionService;
        this.factory = factory;
        this.config = config;

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        eventBus.subscribe(CoreApplicationEvents.DebugStartRequestedEvent.class, e -> startDebugging(), false);
        eventBus.subscribe(CoreApplicationEvents.DebugStepOverRequestedEvent.class, e -> stepOver(), false);
        eventBus.subscribe(CoreApplicationEvents.DebugContinueRequestedEvent.class, e -> continueExecution(), false);
        eventBus.subscribe(CoreApplicationEvents.DebugStopRequestedEvent.class, e -> stopDebugging(), false);
    }

    /**
     * Kicks off the debugging session on a separate thread.
     */
    public void startDebugging() {
        new Thread(() -> {
            try {
                String code = state.getCurrentCode();

                // 1. Compile
                if (!codeExecutionService.compileAndWait(code, config.getSourceFilePath(), config.getCompiledOutputPath())) {
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug aborted due to compilation failure."));
                    return;
                }

                // 2. Map Breakpoints (AST -> Line Numbers)
                CompilationUnit cu = factory.getCompilationUnit();
                if (cu == null || state.getNodeToBlockMap().isEmpty()) {
                    eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Error: Could not parse code to get breakpoints."));
                    return;
                }

                this.lineToBlockMap = new HashMap<>();
                List<Integer> activeBreakpointLines = new ArrayList<>();

                for (CodeBlock block : state.getNodeToBlockMap().values()) {
                    int line = block.getBreakpointLine(cu);
                    if (line > 0) {
                        // Only map StatementBlocks (executable lines) to avoid mapping expressions mid-line awkwardly
                        if (!lineToBlockMap.containsKey(line) || block instanceof StatementBlock) {
                            lineToBlockMap.put(line, block);
                        }
                        if (block.isBreakpoint()) {
                            activeBreakpointLines.add(line);
                        }
                    }
                }

                // If no breakpoints, add one at start so it doesn't just run to finish immediately
                if (activeBreakpointLines.isEmpty() && !lineToBlockMap.isEmpty()) {
                    lineToBlockMap.keySet().stream().min(Integer::compareTo).ifPresent(firstLine -> {
                        activeBreakpointLines.add(firstLine);
                        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("No breakpoints set. Pausing at start (Line " + firstLine + ")."));
                    });
                }

                // 3. Find Free Port
                int freePort;
                try (ServerSocket socket = new ServerSocket(0)) {
                    freePort = socket.getLocalPort();
                }

                // 4. Launch Target Process
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Starting debugger on port " + freePort + "..."));
                eventBus.publish(new CoreApplicationEvents.DebugSessionStartedEvent());
                Platform.runLater(() -> eventBus.publish(new CoreApplicationEvents.OutputClearedEvent()));

                String classPath = config.getCompiledOutputPath().toString();
                String className = config.getMainClassName();
                String javaExecutable = config.getJavaExecutable();
                // Suspend=y waits for us to attach before running main()
                String debugAgent = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", freePort);

                ProcessBuilder pb = new ProcessBuilder(javaExecutable, debugAgent, "-cp", classPath, className);
                this.currentProcess = pb.start();

                // Redirect output to UI
                redirectStream(currentProcess.getInputStream());
                redirectStream(currentProcess.getErrorStream());

                // 5. Attach JDI
                attachJdi(className, freePort, activeBreakpointLines);

            } catch (Exception e) {
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debugger Error: " + e.getMessage()));
                e.printStackTrace();
                stopDebugging(); // Cleanup if fail
            }
        }).start();
    }

    /**
     * Connects the JDI VirtualMachine to the running process.
     */
    private void attachJdi(String mainClassName, int port, List<Integer> breakpointLines) throws Exception {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector connector = vmMgr.attachingConnectors().stream()
                .filter(c -> c.transport().name().equals("dt_socket"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Socket attaching connector not found"));

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue(String.valueOf(port));
        arguments.get("hostname").setValue("localhost");

        // Retry logic for connection (Process might take a moment to open port)
        int maxRetries = Constants.DEBUGGER_MAX_CONNECT_RETRIES;
        for (int i = 0; i < maxRetries; i++) {
            try {
                vm = connector.attach(arguments);
                System.out.println(ANSI_BLUE + "Attached to VM: " + vm.name() + ANSI_RESET);
                break;
            } catch (IOException e) {
                if (i == maxRetries - 1) throw e;
                Thread.sleep(Constants.DEBUGGER_RETRY_DELAY_MS);
            }
        }

        EventRequestManager erm = vm.eventRequestManager();

        // Handle Breakpoints (Class might not be loaded yet, so check both)
        List<ReferenceType> classes = vm.classesByName(mainClassName);
        if (!classes.isEmpty()) {
            applyBreakpointsToClass(classes.get(0), breakpointLines);
        } else {
            // Class not loaded yet -> Listen for preparation
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.addClassFilter(mainClassName);
            classPrepareRequest.enable();
        }

        // Start Event Loop
        CountDownLatch listenerReadyLatch = new CountDownLatch(1);
        new Thread(() -> jdiEventLoop(listenerReadyLatch, mainClassName, breakpointLines)).start();

        // Wait for listener to be ready before resuming VM (so we don't miss events)
        listenerReadyLatch.await();
        vm.resume();
    }

    /**
     * The main loop that listens for events coming from the JVM (Breakpoints, Steps, etc).
     */
    private void jdiEventLoop(CountDownLatch listenerReadyLatch, String mainClassName, List<Integer> breakpointLines) {
        EventQueue eventQueue = vm.eventQueue();

        // Signal that we are listening
        listenerReadyLatch.countDown();

        while (true) {
            try {
                EventSet eventSet = eventQueue.remove();
                boolean shouldResume = true;

                for (Event event : eventSet) {
                    if (event instanceof VMDisconnectEvent) {
                        System.out.println(ANSI_RED + "VM Disconnected." + ANSI_RESET);
                        handleDisconnect();
                        return; // Exit loop
                    }

                    if (event instanceof ClassPrepareEvent) {
                        // Class loaded, now we can set breakpoints
                        ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                        if (cpe.referenceType().name().equals(mainClassName)) {
                            applyBreakpointsToClass(cpe.referenceType(), breakpointLines);
                        }
                    }
                    else if (event instanceof LocatableEvent) {
                        // BreakpointEvent or StepEvent -> PAUSE UI
                        handleLocatableEvent((LocatableEvent) event);
                        shouldResume = false; // Don't resume VM, wait for user input
                    }
                }

                if (shouldResume) {
                    eventSet.resume();
                }
            } catch (InterruptedException | VMDisconnectedException e) {
                handleDisconnect();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyBreakpointsToClass(ReferenceType refType, List<Integer> lines) {
        if (lines == null || lines.isEmpty()) return;
        try {
            EventRequestManager erm = vm.eventRequestManager();
            for (int lineNumber : lines) {
                List<Location> locations = refType.locationsOfLine(lineNumber);
                if (!locations.isEmpty()) {
                    BreakpointRequest bpReq = erm.createBreakpointRequest(locations.get(0));
                    bpReq.enable();
                }
            }
        } catch (AbsentInformationException e) {
            System.err.println("No debug info available (compiled without -g?).");
        }
    }

    private void handleLocatableEvent(LocatableEvent event) {
        this.currentDebugThread = event.thread();

        // Remove step request if this was a step event
        if (event instanceof StepEvent) {
            vm.eventRequestManager().deleteEventRequest(event.request());
        }

        int lineNumber = event.location().lineNumber();
        CodeBlock block = lineToBlockMap.get(lineNumber);
        CodeBlock target = (block != null) ? block.getHighlightTarget() : null;

        System.out.println(ANSI_GREEN + "---> Paused at line: " + lineNumber + ANSI_RESET);

        eventBus.publish(new CoreApplicationEvents.DebugSessionPausedEvent(lineNumber, target));
        eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(target));
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Paused at line: " + lineNumber));
    }

    private void handleDisconnect() {
        this.currentDebugThread = null;
        this.vm = null;
        this.currentProcess = null;

        eventBus.publish(new CoreApplicationEvents.DebugSessionFinishedEvent());
        eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug session finished."));
        eventBus.publish(new CoreApplicationEvents.BlockHighlightEvent(null));
    }

    // --- Public Control Methods ---

    public void stepOver() {
        if (vm == null || currentDebugThread == null) return;
        try {
            eventBus.publish(new CoreApplicationEvents.DebugSessionResumedEvent());
            EventRequestManager erm = vm.eventRequestManager();

            // Clear previous step requests
            erm.stepRequests().stream()
                    .filter(r -> r.thread().equals(currentDebugThread))
                    .forEach(erm::deleteEventRequest);

            // Create new StepOver
            StepRequest request = erm.createStepRequest(currentDebugThread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
            request.addCountFilter(1);
            request.enable();

            vm.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void continueExecution() {
        if (vm != null) {
            eventBus.publish(new CoreApplicationEvents.DebugSessionResumedEvent());
            vm.resume();
        }
    }

    public void stopDebugging() {
        // 1. Disconnect JDI nicely
        if (vm != null) {
            try {
                vm.dispose(); // Triggers VMDisconnectEvent loop exit
            } catch (VMDisconnectedException ignored) {
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 2. Kill process forcefully
        if (currentProcess != null && currentProcess.isAlive()) {
            try {
                currentProcess.destroyForcibly();
                eventBus.publish(new CoreApplicationEvents.StatusMessageEvent("Debug process terminated."));
            } catch (Exception e) { e.printStackTrace(); }
        }

        handleDisconnect(); // Ensure UI state is reset
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