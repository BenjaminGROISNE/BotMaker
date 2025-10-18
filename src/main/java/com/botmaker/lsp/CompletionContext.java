package com.botmaker.lsp;

import com.botmaker.Main;
import com.botmaker.parser.CodeEditor;
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.lsp4j.services.LanguageServer;

// Using a record for a simple, immutable data carrier to pass to UI creation.
public record CompletionContext(
        Main mainApp, // Reference to the main application
        CodeEditor codeEditor,
        LanguageServer server,
        String docUri,
        String sourceCode,
        long docVersion,
        BlockDragAndDropManager dragAndDropManager
) {}