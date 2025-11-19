package com.botmaker.state;

import com.botmaker.core.CodeBlock;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.*;

/**
 * Central application state container.
 * Manages the current state of the code editor and related metadata.
 * Thread-safe through immutability and defensive copying.
 */
public class ApplicationState {

    // Code-related state
    private String currentCode;
    private String docUri;
    private long docVersion;

    // AST and block mappings
    private CompilationUnit compilationUnit;
    private Map<ASTNode, CodeBlock> nodeToBlockMap;

    // UI state
    private CodeBlock highlightedBlock;

    // Debug state
    private boolean isDebugging;
    private final Set<String> breakpointIds = new HashSet<>();
    public ApplicationState() {
        this.currentCode = "";
        this.docUri = "";
        this.docVersion = 1;
        this.nodeToBlockMap = new HashMap<>();
        this.isDebugging = false;
    }

    // Code state

    public String getCurrentCode() {
        return currentCode;
    }

    public void setCurrentCode(String currentCode) {
        this.currentCode = currentCode != null ? currentCode : "";
    }

    public String getDocUri() {
        return docUri;
    }

    public void setDocUri(String docUri) {
        this.docUri = docUri != null ? docUri : "";
    }

    public long getDocVersion() {
        return docVersion;
    }

    public void incrementDocVersion() {
        this.docVersion++;
    }

    public void setDocVersion(long docVersion) {
        this.docVersion = docVersion;
    }

    // AST and block mappings

    public Optional<CompilationUnit> getCompilationUnit() {
        return Optional.ofNullable(compilationUnit);
    }

    public void setCompilationUnit(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    /**
     * Returns an unmodifiable view of the node-to-block map
     */
    public Map<ASTNode, CodeBlock> getNodeToBlockMap() {
        return Collections.unmodifiableMap(nodeToBlockMap);
    }

    /**
     * Returns the mutable node-to-block map for updates
     */
    public Map<ASTNode, CodeBlock> getMutableNodeToBlockMap() {
        return nodeToBlockMap;
    }

    public void setNodeToBlockMap(Map<ASTNode, CodeBlock> nodeToBlockMap) {
        this.nodeToBlockMap = nodeToBlockMap != null ?
                new HashMap<>(nodeToBlockMap) : new HashMap<>();
    }

    public void clearNodeToBlockMap() {
        this.nodeToBlockMap.clear();
    }

    public Optional<CodeBlock> getBlockForNode(ASTNode node) {
        return Optional.ofNullable(nodeToBlockMap.get(node));
    }

    // UI state

    public Optional<CodeBlock> getHighlightedBlock() {
        return Optional.ofNullable(highlightedBlock);
    }

    public void setHighlightedBlock(CodeBlock block) {
        // Clear previous highlight
        if (this.highlightedBlock != null) {
            this.highlightedBlock.unhighlight();
        }

        this.highlightedBlock = block;

        // Apply new highlight
        if (this.highlightedBlock != null) {
            this.highlightedBlock.highlight();
        }
    }

    public void clearHighlight() {
        setHighlightedBlock(null);
    }

    // Debug state

    public boolean isDebugging() {
        return isDebugging;
    }

    public void setDebugging(boolean debugging) {
        this.isDebugging = debugging;
    }
    // NEW: Breakpoint Management
    public Set<String> getBreakpointIds() {
        return Collections.unmodifiableSet(breakpointIds);
    }

    public void addBreakpoint(String blockId) {
        breakpointIds.add(blockId);
    }

    public void removeBreakpoint(String blockId) {
        breakpointIds.remove(blockId);
    }

    public boolean hasBreakpoint(String blockId) {
        return breakpointIds.contains(blockId);
    }
    // Utility methods

    /**
     * Creates a snapshot of the current state for debugging/logging
     */
    public StateSnapshot createSnapshot() {
        return new StateSnapshot(
                currentCode,
                docUri,
                docVersion,
                nodeToBlockMap.size(),
                highlightedBlock != null,
                isDebugging
        );
    }

    /**
     * Immutable snapshot of state for debugging
     */
    public static class StateSnapshot {
        public final String currentCode;
        public final String docUri;
        public final long docVersion;
        public final int blockCount;
        public final boolean hasHighlight;
        public final boolean isDebugging;

        private StateSnapshot(String currentCode, String docUri, long docVersion,
                              int blockCount, boolean hasHighlight, boolean isDebugging) {
            this.currentCode = currentCode;
            this.docUri = docUri;
            this.docVersion = docVersion;
            this.blockCount = blockCount;
            this.hasHighlight = hasHighlight;
            this.isDebugging = isDebugging;
        }

        @Override
        public String toString() {
            return String.format(
                    "StateSnapshot{docVersion=%d, codeLength=%d, blockCount=%d, hasHighlight=%s, isDebugging=%s}",
                    docVersion, currentCode.length(), blockCount, hasHighlight, isDebugging
            );
        }
    }
}