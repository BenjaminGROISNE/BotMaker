package com.botmaker.events;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.ui.AddableBlock;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoreApplicationEvents {



    public static class CodeUpdatedEvent extends AbstractApplicationEvent {
        private final String newCode;
        private final String previousCode;
        public CodeUpdatedEvent(String newCode, String previousCode) {
            this.newCode = newCode;
            this.previousCode = previousCode;
        }
        public String getNewCode() { return newCode; }
        public String getPreviousCode() { return previousCode; }
    }

    public static class DiagnosticsUpdatedEvent extends AbstractApplicationEvent {
        private final List<Diagnostic> diagnostics;
        public DiagnosticsUpdatedEvent(List<Diagnostic> diagnostics) {
            this.diagnostics = diagnostics != null ? List.copyOf(diagnostics) : Collections.emptyList();
        }
        public List<Diagnostic> getDiagnostics() { return diagnostics; }
    }

    public static class UIRefreshRequestedEvent extends AbstractApplicationEvent {
        private final String code;
        public UIRefreshRequestedEvent(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    public static class BlockHighlightEvent extends AbstractApplicationEvent {
        private final CodeBlock block;
        public BlockHighlightEvent(CodeBlock block) { this.block = block; }
        public CodeBlock getBlock() { return block; }
    }



    public static class UIBlocksUpdatedEvent extends AbstractApplicationEvent {
        private final AbstractCodeBlock rootBlock;

        public UIBlocksUpdatedEvent(AbstractCodeBlock rootBlock) {
            super("CodeEditorService");
            this.rootBlock = rootBlock;
        }

        public AbstractCodeBlock getRootBlock() { return rootBlock; }
    }

    public static class CompilationRequestedEvent extends AbstractApplicationEvent {}
    public static class ExecutionRequestedEvent extends AbstractApplicationEvent {}
    public static class DebugStartRequestedEvent extends AbstractApplicationEvent {}

    public static class DebugSessionStartedEvent extends AbstractApplicationEvent {}
    public static class DebugSessionResumedEvent extends AbstractApplicationEvent {}
    public static class DebugSessionFinishedEvent extends AbstractApplicationEvent {}

    public static class DebugSessionPausedEvent extends AbstractApplicationEvent {
        private final int lineNumber;
        private final CodeBlock block;
        public DebugSessionPausedEvent(int lineNumber, CodeBlock block) {
            this.lineNumber = lineNumber;
            this.block = block;
        }
        public int getLineNumber() { return lineNumber; }
        public CodeBlock getBlock() { return block; }
    }

    public static class StatusMessageEvent extends AbstractApplicationEvent {
        private final String message;
        public StatusMessageEvent(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    // Inside CoreApplicationEvents class


    public static class OutputAppendedEvent extends AbstractApplicationEvent {
        private final String text;
        public OutputAppendedEvent(String text) { this.text = text; }
        public String getText() { return text; }
    }

    public static class OutputClearedEvent extends AbstractApplicationEvent {}
    public static class OutputSetEvent extends AbstractApplicationEvent {
        private final String text;
        public OutputSetEvent(String text) { this.text = text; }
        public String getText() { return text; }
    }

    public static class UndoRequestedEvent extends AbstractApplicationEvent {
        public UndoRequestedEvent() { super("User"); }
    }

    public static class RedoRequestedEvent extends AbstractApplicationEvent {
        public RedoRequestedEvent() { super("User"); }
    }

    /**
     * Fired whenever the history stack changes (to enable/disable UI buttons)
     */
    public static class HistoryStateChangedEvent extends AbstractApplicationEvent {
        private final boolean canUndo;
        private final boolean canRedo;

        public HistoryStateChangedEvent(boolean canUndo, boolean canRedo) {
            this.canUndo = canUndo;
            this.canRedo = canRedo;
        }

        public boolean canUndo() { return canUndo; }
        public boolean canRedo() { return canRedo; }
    }


    // ADD THESE EVENT CLASSES TO CoreApplicationEvents.java:

    /**
     * Fired when a user requests to stop the currently running program
     */
    public static class StopRunRequestedEvent extends AbstractApplicationEvent {
        public StopRunRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when a program starts executing (not debugging)
     */
    public static class ProgramStartedEvent extends AbstractApplicationEvent {
        public ProgramStartedEvent() {
            super("ExecutionService");
        }
    }

    /**
     * Fired when a program stops executing (completed or terminated)
     */
    public static class ProgramStoppedEvent extends AbstractApplicationEvent {
        public ProgramStoppedEvent() {
            super("ExecutionService");
        }
    }
    /**
     * Fired when a user wants to step over the current line
     */
    public static class DebugStepOverRequestedEvent extends AbstractApplicationEvent {
        public DebugStepOverRequestedEvent() {
            super("User");
        }
    }

    public static class DebugStopRequestedEvent extends AbstractApplicationEvent {
        public DebugStopRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when a user wants to continue execution until the next breakpoint
     */
    public static class DebugContinueRequestedEvent extends AbstractApplicationEvent {
        public DebugContinueRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when a user toggles a breakpoint on a block
     */
    public static class BreakpointToggledEvent extends AbstractApplicationEvent {
        private final CodeBlock block;
        private final boolean isEnabled;

        public BreakpointToggledEvent(CodeBlock block, boolean isEnabled) {
            super("User");
            this.block = block;
            this.isEnabled = isEnabled;
        }

        public CodeBlock getBlock() { return block; }
        public boolean isEnabled() { return isEnabled; }
    }

    public static class BlockAddedEvent extends AbstractApplicationEvent {
        private final AddableBlock blockType;
        public BlockAddedEvent(AddableBlock blockType) {
            super("CodeEditorService");
            this.blockType = blockType;
        }
        public AddableBlock getBlockType() { return blockType; }
    }

    /**
     * Fired when the user requests to copy the currently selected block.
     */
    public static class CopyRequestedEvent extends AbstractApplicationEvent {
        public CopyRequestedEvent() { super("User"); }
    }

    /**
     * Fired when the user requests to paste content relative to the currently selected block.
     */
    public static class PasteRequestedEvent extends AbstractApplicationEvent {
        public PasteRequestedEvent() { super("User"); }
    }
}