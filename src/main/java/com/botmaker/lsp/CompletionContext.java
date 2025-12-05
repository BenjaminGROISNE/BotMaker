package com.botmaker.lsp;

import com.botmaker.parser.CodeEditor;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.events.EventBus; // Import EventBus
import org.eclipse.lsp4j.services.LanguageServer;

public record CompletionContext(
        CodeEditor codeEditor,
        LanguageServer server,
        String docUri,
        String sourceCode,
        long docVersion,
        BlockDragAndDropManager dragAndDropManager,
        ApplicationState applicationState,
        EventBus eventBus // Added field
) {
}