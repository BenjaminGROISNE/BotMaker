// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\state\ApplicationState.java
package com.botmaker.state;

import com.botmaker.core.CodeBlock;
import com.botmaker.project.ProjectFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.nio.file.Path;
import java.util.*;

public class ApplicationState {

    // Multi-file state
    private final Map<Path, ProjectFile> openFiles = new HashMap<>();
    private ProjectFile activeFile;

    // AST and block mappings (For the ACTIVE file)
    private Map<ASTNode, CodeBlock> nodeToBlockMap = new HashMap<>();

    // UI state
    private CodeBlock highlightedBlock;
    private boolean isDebugging;
    private final Set<String> breakpointIds = new HashSet<>();
    private long docVersion = 1;
    private final Set<String> collapsedMethods = new HashSet<>();

    // --- File Management ---

    public void addFile(ProjectFile file) {
        openFiles.put(file.getPath(), file);
    }

    public void removeFile(Path path) {
        openFiles.remove(path);
    }

    public boolean isMethodCollapsed(String methodKey) {
        return collapsedMethods.contains(methodKey);
    }

    public void setMethodCollapsed(String methodKey, boolean collapsed) {
        if (collapsed) {
            collapsedMethods.add(methodKey);
        } else {
            collapsedMethods.remove(methodKey);
        }
    }

    public void clearCollapsedMethods() {
        collapsedMethods.clear();
    }

    public void setActiveFile(Path path) {
        this.activeFile = openFiles.get(path);
        // Reset or sync doc version when switching if necessary,
        // though usually we track version per file.
        // For now, we keep a global version counter for LSP sync simplicity.
    }

    public ProjectFile getActiveFile() {
        return activeFile;
    }

    public Collection<ProjectFile> getAllFiles() {
        return Collections.unmodifiableCollection(openFiles.values());
    }

    // --- Helpers (Delegate to Active File) ---

    public String getCurrentCode() {
        return activeFile != null ? activeFile.getContent() : "";
    }

    public void setCurrentCode(String code) {
        if (activeFile != null) activeFile.setContent(code);
    }

    public String getDocUri() {
        return activeFile != null ? activeFile.getUri() : "";
    }

    /**
     * Set Doc URI.
     * Note: In multi-file mode, the URI is derived from the ProjectFile path.
     * This method exists for backward compatibility with LanguageServerService.
     */
    public void setDocUri(String docUri) {
        // No-op: The URI is determined by the active file's path.
        // We accept the call to satisfy the compiler, but rely on setActiveFile() being called previously.
    }

    public Optional<CompilationUnit> getCompilationUnit() {
        return activeFile != null ? Optional.ofNullable(activeFile.getAst()) : Optional.empty();
    }

    public void setCompilationUnit(CompilationUnit cu) {
        if (activeFile != null) activeFile.setAst(cu);
    }

    // --- Versioning ---

    public long getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(long version) {
        this.docVersion = version;
    }

    public void incrementDocVersion() {
        this.docVersion++;
    }

    // --- Mappings & UI State ---

    public Map<ASTNode, CodeBlock> getNodeToBlockMap() {
        return Collections.unmodifiableMap(nodeToBlockMap);
    }

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

    public Optional<CodeBlock> getHighlightedBlock() {
        return Optional.ofNullable(highlightedBlock);
    }

    public void setHighlightedBlock(CodeBlock block) {
        if (this.highlightedBlock != null) {
            this.highlightedBlock.unhighlight();
        }
        this.highlightedBlock = block;
        if (this.highlightedBlock != null) {
            this.highlightedBlock.highlight();
        }
    }

    public void clearHighlight() {
        setHighlightedBlock(null);
    }

    // --- Debugging ---

    public boolean isDebugging() {
        return isDebugging;
    }

    public void setDebugging(boolean debugging) {
        this.isDebugging = debugging;
    }

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

    /**
     * Snapshot for debugging/logging
     */
    public StateSnapshot createSnapshot() {
        return new StateSnapshot(
                getCurrentCode(),
                getDocUri(),
                docVersion,
                nodeToBlockMap.size(),
                highlightedBlock != null,
                isDebugging
        );
    }

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
                    docVersion, currentCode != null ? currentCode.length() : 0, blockCount, hasHighlight, isDebugging
            );
        }
    }
}