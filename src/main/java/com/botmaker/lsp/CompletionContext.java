package com.botmaker.lsp;

import com.botmaker.parser.CodeEditor;
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Context object passed to blocks for UI rendering and editing operations.
 * Phase 3: Removed Main dependency - blocks now use only what they need.
 */
public record CompletionContext(
        CodeEditor codeEditor,
        LanguageServer server,
        String docUri,
        String sourceCode,
        long docVersion,
        BlockDragAndDropManager dragAndDropManager
) {
    // All the data blocks need, without Main reference
}