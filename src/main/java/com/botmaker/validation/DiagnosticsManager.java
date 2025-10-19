package com.botmaker.validation;

import com.botmaker.core.CodeBlock;
import javafx.application.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DiagnosticsManager {

    private Map<ASTNode, CodeBlock> nodeToBlockMap;
    private String sourceCode;
    private final Set<CodeBlock> blocksWithErrors = new HashSet<>();

    public void updateSource(Map<ASTNode, CodeBlock> nodeToBlockMap, String sourceCode) {
        this.nodeToBlockMap = nodeToBlockMap;
        this.sourceCode = sourceCode;
    }

    public void handleDiagnostics(PublishDiagnosticsParams params) {
        Platform.runLater(() -> {
            // Clear previous errors
            for (CodeBlock block : blocksWithErrors) {
                block.clearError();
            }
            blocksWithErrors.clear();

            if (nodeToBlockMap == null) return;

            // Process new diagnostics
            for (Diagnostic diagnostic : params.getDiagnostics()) {
                findBlockForDiagnostic(diagnostic).ifPresent(block -> {
                    block.setError(diagnostic.getMessage());
                    blocksWithErrors.add(block);
                });
            }
        });
    }

    private Optional<CodeBlock> findBlockForDiagnostic(Diagnostic diagnostic) {
        int startOffset = getOffsetFromPosition(diagnostic.getRange().getStart());
        int endOffset = getOffsetFromPosition(diagnostic.getRange().getEnd());

        // Find the most specific (smallest) block that contains the diagnostic range
        ASTNode bestNode = null;
        for (ASTNode node : nodeToBlockMap.keySet()) {
            int nodeStart = node.getStartPosition();
            int nodeEnd = nodeStart + node.getLength();

            if (nodeStart <= startOffset && nodeEnd >= endOffset) {
                if (bestNode == null || (node.getLength() < bestNode.getLength())) {
                    bestNode = node;
                }
            }
        }

        return Optional.ofNullable(bestNode).map(nodeToBlockMap::get);
    }

    private int getOffsetFromPosition(org.eclipse.lsp4j.Position pos) {
        int line = pos.getLine();
        int character = pos.getCharacter();
        int offset = 0;
        int currentLine = 0;
        if (sourceCode == null) return 0;
        while (currentLine < line && offset < sourceCode.length()) {
            if (sourceCode.charAt(offset) == '\n') {
                currentLine++;
            }
            offset++;
        }
        return offset + character;
    }
}
