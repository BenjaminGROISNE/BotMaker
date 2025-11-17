package com.botmaker.events;

import com.botmaker.core.CodeBlock;
import org.eclipse.lsp4j.Diagnostic;

import java.util.Collections;
import java.util.List;

/**
 * Core application events that drive the application's behavior.
 *
 * Phase 1: Initial 13 events
 * Phase 2: Added 4 more events (UIBlocksUpdated, Output events)
 */
public class CoreApplicationEvents {

    // ============================================
    // PHASE 1 EVENTS (original 13)
    // ============================================

    /**
     * Fired when the source code has been updated
     */
    public static class CodeUpdatedEvent extends AbstractApplicationEvent {
        private final String newCode;
        private final String previousCode;

        public CodeUpdatedEvent(String newCode, String previousCode) {
            this.newCode = newCode;
            this.previousCode = previousCode;
        }

        public String getNewCode() {
            return newCode;
        }

        public String getPreviousCode() {
            return previousCode;
        }
    }

    /**
     * Fired when diagnostics have been updated from the language server
     */
    public static class DiagnosticsUpdatedEvent extends AbstractApplicationEvent {
        private final List<Diagnostic> diagnostics;

        public DiagnosticsUpdatedEvent(List<Diagnostic> diagnostics) {
            this.diagnostics = diagnostics != null ?
                    List.copyOf(diagnostics) : Collections.emptyList();
        }

        public List<Diagnostic> getDiagnostics() {
            return diagnostics;
        }
    }

    /**
     * Fired when the UI needs to be refreshed with new blocks
     */
    public static class UIRefreshRequestedEvent extends AbstractApplicationEvent {
        private final String code;

        public UIRefreshRequestedEvent(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * Fired when a block should be highlighted (e.g., during debugging)
     */
    public static class BlockHighlightEvent extends AbstractApplicationEvent {
        private final CodeBlock block;

        public BlockHighlightEvent(CodeBlock block) {
            this.block = block;
        }

        public CodeBlock getBlock() {
            return block;
        }
    }

    /**
     * Fired when compilation is requested
     */
    public static class CompilationRequestedEvent extends AbstractApplicationEvent {
        public CompilationRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when code execution is requested
     */
    public static class ExecutionRequestedEvent extends AbstractApplicationEvent {
        public ExecutionRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when debugging should start
     */
    public static class DebugStartRequestedEvent extends AbstractApplicationEvent {
        public DebugStartRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when debugger should resume
     */
    public static class DebugResumeRequestedEvent extends AbstractApplicationEvent {
        public DebugResumeRequestedEvent() {
            super("User");
        }
    }

    /**
     * Fired when debugging has started
     */
    public static class DebugSessionStartedEvent extends AbstractApplicationEvent {
        public DebugSessionStartedEvent() {
            super("DebuggingManager");
        }
    }

    /**
     * Fired when debugger has paused
     */
    public static class DebugSessionPausedEvent extends AbstractApplicationEvent {
        private final int lineNumber;
        private final CodeBlock block;

        public DebugSessionPausedEvent(int lineNumber, CodeBlock block) {
            super("DebuggingManager");
            this.lineNumber = lineNumber;
            this.block = block;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public CodeBlock getBlock() {
            return block;
        }
    }

    /**
     * Fired when debugger has resumed
     */
    public static class DebugSessionResumedEvent extends AbstractApplicationEvent {
        public DebugSessionResumedEvent() {
            super("DebuggingManager");
        }
    }

    /**
     * Fired when debugging has finished
     */
    public static class DebugSessionFinishedEvent extends AbstractApplicationEvent {
        public DebugSessionFinishedEvent() {
            super("DebuggingManager");
        }
    }

    /**
     * Fired when there's a status message to display
     */
    public static class StatusMessageEvent extends AbstractApplicationEvent {
        private final String message;

        public StatusMessageEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // ============================================
    // PHASE 2 EVENTS (new - 4 events added)
    // ============================================

    /**
     * Fired when the UI blocks have been updated and need to be rendered
     * PHASE 2: NEW
     */
    public static class UIBlocksUpdatedEvent extends AbstractApplicationEvent {
        private final com.botmaker.blocks.MainBlock rootBlock;

        public UIBlocksUpdatedEvent(com.botmaker.blocks.MainBlock rootBlock) {
            super("CodeEditorService");
            this.rootBlock = rootBlock;
        }

        public com.botmaker.blocks.MainBlock getRootBlock() {
            return rootBlock;
        }
    }

    /**
     * Fired when output should be appended to the terminal
     * PHASE 2: NEW
     */
    public static class OutputAppendedEvent extends AbstractApplicationEvent {
        private final String text;

        public OutputAppendedEvent(String text) {
            super("System");
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Fired when output should be cleared
     * PHASE 2: NEW
     */
    public static class OutputClearedEvent extends AbstractApplicationEvent {
        public OutputClearedEvent() {
            super("System");
        }
    }

    /**
     * Fired when output should be set (replace all)
     * PHASE 2: NEW
     */
    public static class OutputSetEvent extends AbstractApplicationEvent {
        private final String text;

        public OutputSetEvent(String text) {
            super("System");
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}