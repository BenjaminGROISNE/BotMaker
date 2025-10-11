package com.botmaker;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DebuggerService {

    // ANSI escape codes for colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";

    private VirtualMachine vm;
    private Consumer<LocatableEvent> onPause;
    private Runnable onDisconnect;

    public void setOnPause(Consumer<LocatableEvent> onPause) {
        this.onPause = onPause;
    }

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void connectAndRun(String mainClassName, List<Integer> breakpointLines) throws IOException, IllegalConnectorArgumentsException, InterruptedException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector connector = vmMgr.attachingConnectors().stream()
                .filter(c -> c.transport().name().equals("dt_socket"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Socket attaching connector not found"));

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue("8000");
        arguments.get("hostname").setValue("localhost");

        System.out.println(ANSI_BLUE + "Attaching to process on port 8000..." + ANSI_RESET);
        vm = connector.attach(arguments);
        System.out.println(ANSI_BLUE + "Attached to VM: " + vm.name() + ANSI_RESET);

        EventRequestManager erm = vm.eventRequestManager();

        List<ReferenceType> classes = vm.classesByName(mainClassName);
        if (!classes.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Class " + mainClassName + " is already loaded. Setting breakpoints immediately." + ANSI_RESET);
            setBreakpoints(classes.get(0), breakpointLines);
        } else {
            System.out.println(ANSI_YELLOW + "Class " + mainClassName + " is not loaded yet. Requesting notification for when it is." + ANSI_RESET);
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.addClassFilter(mainClassName);
            classPrepareRequest.enable();
        }

        CountDownLatch listenerReadyLatch = new CountDownLatch(1);
        new Thread(() -> listenForEvents(listenerReadyLatch, mainClassName, breakpointLines)).start();

        System.out.println(ANSI_BLUE + "Waiting for debugger event listener to be ready..." + ANSI_RESET);
        listenerReadyLatch.await();

        System.out.println(ANSI_BLUE + "Listener is ready. Resuming VM to trigger class loading and hit initial breakpoint." + ANSI_RESET);
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
                        if (event instanceof BreakpointEvent) {
                            System.out.println(ANSI_GREEN + "---> Hit Breakpoint: " + event + ANSI_RESET);
                            if (onPause != null) {
                                onPause.accept((BreakpointEvent) event);
                            }
                            shouldResume = false; // PAUSE the VM
                        } else if (event instanceof ClassPrepareEvent) {
                            System.out.println(ANSI_YELLOW + "Class Prepared: " + ((ClassPrepareEvent) event).referenceType().name() + ANSI_RESET);
                            ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                            if (cpe.referenceType().name().equals(mainClassName)) {
                                setBreakpoints(cpe.referenceType(), breakpointLines);
                            }
                        } else if (event instanceof VMDisconnectEvent) {
                            System.out.println(ANSI_RED + "VM Disconnected." + ANSI_RESET);
                            if (onDisconnect != null) {
                                onDisconnect.run();
                            }
                            return; // Exit thread
                        } else {
                            // Other events like VMStartEvent, ThreadStartEvent etc.
                            System.out.println(ANSI_BLUE + "JDI Event: " + event + ANSI_RESET);
                        }
                    }
                } finally {
                    if (shouldResume) {
                        eventSet.resume();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(ANSI_RED + "Event listener interrupted." + ANSI_RESET);
                return;
            } catch (VMDisconnectedException e) {
                System.out.println(ANSI_RED + "VM Disconnected. Exiting event listener." + ANSI_RESET);
                if (onDisconnect != null) {
                    onDisconnect.run();
                }
                return;
            } catch (Exception e) {
                System.out.println(ANSI_RED + "Exception in event loop: " + e.getMessage() + ANSI_RESET);
                e.printStackTrace();
            }
        }
    }

    private void setBreakpoints(ReferenceType refType, List<Integer> lines) {
        try {
            for (int lineNumber : lines) {
                List<Location> locations = refType.locationsOfLine(lineNumber);
                if (!locations.isEmpty()) {
                    Location loc = locations.get(0);
                    System.out.println(ANSI_YELLOW + "Setting breakpoint at: " + loc + ANSI_RESET);
                    BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(loc);
                    bpReq.enable();
                } else {
                    System.out.println(ANSI_YELLOW + "Warning: Could not find a location for line " + lineNumber + ANSI_RESET);
                }
            }
        } catch (AbsentInformationException e) {
            System.err.println(ANSI_RED + "Could not set breakpoints. Compile the source with debug information (-g)." + ANSI_RESET);
        }
    }

    public void resume() {
        if (vm != null) {
            System.out.println(ANSI_BLUE + "Resuming VM..." + ANSI_RESET);
            vm.resume();
        }
    }
}
