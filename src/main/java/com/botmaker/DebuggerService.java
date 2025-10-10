package com.botmaker;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class DebuggerService {

    private VirtualMachine vm;
    private ThreadReference mainThread;
    private Consumer<StepEvent> onStep;

    public void setOnStep(Consumer<StepEvent> onStep) {
        this.onStep = onStep;
    }

    public void connectAndRun() throws IOException, IllegalConnectorArgumentsException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector connector = vmMgr.attachingConnectors().stream()
                .filter(c -> c.transport().name().equals("dt_socket"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Socket attaching connector not found"));

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("port").setValue("8000");
        arguments.get("hostname").setValue("localhost");

        System.out.println("Attaching to process on port 8000...");
        vm = connector.attach(arguments);
        System.out.println("Attached to VM: " + vm.name());

        new Thread(this::listenForEvents).start();
    }

    private void listenForEvents() {
        if (vm == null) return;
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            try {
                EventSet eventSet = eventQueue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    Event event = it.next();
                    System.out.println("Received JDI Event: " + event);

                    if (event instanceof VMStartEvent) {
                        System.out.println("VM Started. Finding main thread...");
                        mainThread = findMainThread();
                        if (mainThread != null) {
                            System.out.println("Main thread found. Resuming VM to hit the first executable line.");
                            vm.resume();
                        }
                    } else if (event instanceof StepEvent) {
                        StepEvent stepEvent = (StepEvent) event;
                        int lineNumber = stepEvent.location().lineNumber();
                        System.out.println("STEPPED to line: " + lineNumber);
                        if (onStep != null) {
                            onStep.accept(stepEvent);
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Event listener interrupted.");
                return;
            } catch (Exception e) {
                System.out.println("Exception in event loop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stepOver() {
        if (vm == null || mainThread == null) {
            System.out.println("Cannot step: VM not connected or main thread not found.");
            return;
        }

        System.out.println("Creating step over request...");
        EventRequestManager erm = vm.eventRequestManager();
        StepRequest stepRequest = erm.createStepRequest(mainThread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
        stepRequest.addCountFilter(1); // Make it a one-off event
        stepRequest.enable();

        vm.resume(); // Resume execution until the step completes
        System.out.println("VM resumed for one step.");
    }

    private ThreadReference findMainThread() {
        if (vm == null) return null;
        for (ThreadReference thread : vm.allThreads()) {
            if ("main".equals(thread.name())) {
                return thread;
            }
        }
        return null;
    }

    public void highlightLine(int lineNumber) {
        // In the future, this will be used to highlight the current line in the UI.
        System.out.println("Highlighting line: " + lineNumber);
    }
}