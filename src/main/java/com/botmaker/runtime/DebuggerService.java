package com.botmaker.runtime;

import com.botmaker.config.Constants;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DebuggerService {

    // ANSI escape codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";

    private VirtualMachine vm;
    private Consumer<LocatableEvent> onPause;
    private Runnable onDisconnect;

    // NEW: Keep track of the thread we are debugging
    private ThreadReference currentThread;

    public void setOnPause(Consumer<LocatableEvent> onPause) { this.onPause = onPause; }
    public void setOnDisconnect(Runnable onDisconnect) { this.onDisconnect = onDisconnect; }

    public void connectAndRun(String mainClassName, int port, List<Integer> breakpointLines) throws IOException, IllegalConnectorArgumentsException, InterruptedException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector connector = vmMgr.attachingConnectors().stream()
                .filter(c -> c.transport().name().equals("dt_socket"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Socket attaching connector not found"));

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue(String.valueOf(port));
        arguments.get("hostname").setValue("localhost");

        int maxRetries = Constants.DEBUGGER_MAX_CONNECT_RETRIES;
        int retryDelayMs = Constants.DEBUGGER_RETRY_DELAY_MS;
        for (int i = 0; i < maxRetries; i++) {
            try {
                System.out.println(ANSI_BLUE + "Attaching to process on port " + port + " (Attempt " + (i + 1) + ")..." + ANSI_RESET);
                vm = connector.attach(arguments);
                System.out.println(ANSI_BLUE + "Attached to VM: " + vm.name() + ANSI_RESET);
                break;
            } catch (IOException e) {
                if (e instanceof java.net.ConnectException && i < maxRetries - 1) {
                    System.out.println(ANSI_YELLOW + "Connection refused. Retrying in " + retryDelayMs + "ms..." + ANSI_RESET);
                    Thread.sleep(retryDelayMs);
                } else {
                    throw e;
                }
            }
        }

        EventRequestManager erm = vm.eventRequestManager();

        List<ReferenceType> classes = vm.classesByName(mainClassName);
        if (!classes.isEmpty()) {
            setBreakpoints(classes.getFirst(), breakpointLines);
        } else {
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.addClassFilter(mainClassName);
            classPrepareRequest.enable();
        }

        CountDownLatch listenerReadyLatch = new CountDownLatch(1);
        new Thread(() -> listenForEvents(listenerReadyLatch, mainClassName, breakpointLines)).start();
        listenerReadyLatch.await();

        vm.resume();
    }

    private void listenForEvents(CountDownLatch listenerReadyLatch, String mainClassName, List<Integer> breakpointLines) {
        if (vm == null) return;
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            try {
                if (listenerReadyLatch != null) {
                    listenerReadyLatch.countDown();
                    listenerReadyLatch = null;
                }

                EventSet eventSet = eventQueue.remove();
                boolean shouldResume = true;

                try {
                    for (Event event : eventSet) {
                        if (event instanceof LocatableEvent) {
                            // BreakpointEvent or StepEvent
                            System.out.println(ANSI_GREEN + "---> Paused at: " + event + ANSI_RESET);

                            // Capture the thread context
                            if (event instanceof BreakpointEvent) {
                                currentThread = ((BreakpointEvent) event).thread();
                            } else if (event instanceof StepEvent) {
                                currentThread = ((StepEvent) event).thread();
                                // Clean up the step request once hit
                                vm.eventRequestManager().deleteEventRequest(event.request());
                            }

                            if (onPause != null) {
                                onPause.accept((LocatableEvent) event);
                            }
                            shouldResume = false; // Keep paused
                        } else if (event instanceof ClassPrepareEvent) {
                            System.out.println(ANSI_YELLOW + "Class Prepared: " + ((ClassPrepareEvent) event).referenceType().name() + ANSI_RESET);
                            ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                            if (cpe.referenceType().name().equals(mainClassName)) {
                                setBreakpoints(cpe.referenceType(), breakpointLines);
                            }
                        } else if (event instanceof VMDisconnectEvent) {
                            System.out.println(ANSI_RED + "VM Disconnected." + ANSI_RESET);
                            if (onDisconnect != null) onDisconnect.run();
                            return;
                        }
                    }
                } finally {
                    if (shouldResume) {
                        eventSet.resume();
                    }
                }
            } catch (InterruptedException | VMDisconnectedException e) {
                if (onDisconnect != null) onDisconnect.run();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setBreakpoints(ReferenceType refType, List<Integer> lines) {
        if (lines == null || lines.isEmpty()) return;
        try {
            EventRequestManager erm = vm.eventRequestManager();
            // Clear existing breakpoints on this class first if re-setting
            // (Not implementing clear logic here for simplicity, assuming fresh session)

            for (int lineNumber : lines) {
                List<Location> locations = refType.locationsOfLine(lineNumber);
                if (!locations.isEmpty()) {
                    Location loc = locations.get(0);
                    BreakpointRequest bpReq = erm.createBreakpointRequest(loc);
                    bpReq.enable();
                }
            }
        } catch (AbsentInformationException e) {
            System.err.println("No debug info available.");
        }
    }

    // NEW: Step Over Implementation
    public void stepOver() {
        if (vm == null || currentThread == null) return;
        try {
            EventRequestManager erm = vm.eventRequestManager();

            // Clear any existing step requests on this thread
            List<StepRequest> steps = erm.stepRequests();
            for (StepRequest step : steps) {
                if (step.thread().equals(currentThread)) {
                    erm.deleteEventRequest(step);
                }
            }

            // Create new StepOver request
            StepRequest request = erm.createStepRequest(currentThread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
            request.addCountFilter(1); // Step 1 line
            request.enable();

            vm.resume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (vm != null) {
            System.out.println(ANSI_BLUE + "Resuming VM..." + ANSI_RESET);
            vm.resume();
        }
    }
}