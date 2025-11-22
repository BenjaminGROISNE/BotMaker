package com.botmaker.lsp;

import com.botmaker.parser.CodeEditor;
import com.botmaker.state.ApplicationState; // Import State
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.lsp4j.services.LanguageServer;

public record CompletionContext(
        CodeEditor codeEditor,
        LanguageServer server,
        String docUri,
        String sourceCode,
        long docVersion,
        BlockDragAndDropManager dragAndDropManager,
        ApplicationState applicationState // Added field
) {
}