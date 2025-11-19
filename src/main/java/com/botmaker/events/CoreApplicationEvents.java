package com.botmaker.events;

import com.botmaker.core.CodeBlock;
import org.eclipse.lsp4j.Diagnostic;
import java.util.Collections;
import java.util.List;

public class CoreApplicationEvents {

    // ... (Keep existing events: CodeUpdatedEvent, DiagnosticsUpdatedEvent, UIRefreshRequestedEvent, BlockHighlightEvent) ...

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

    public static class UIBlocksUpdatedEvent extends AbstractApplicationEvent {
        private final com.botmaker.blocks.MainBlock rootBlock;
        public UIBlocksUpdatedEvent(com.botmaker.blocks.MainBlock rootBlock) {
            super("CodeEditorService");
            this.rootBlock = rootBlock;
        }
        public com.botmaker.blocks.MainBlock getRootBlock() { return rootBlock; }
    }

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

    // --- NEW EVENTS ---

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
}