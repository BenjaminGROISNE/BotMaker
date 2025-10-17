package com.botmaker.lsp;

import com.botmaker.Main;
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.function.Consumer;

// Using a record for a simple, immutable data carrier to pass to UI creation.
public record CompletionContext(
        Main mainApp, // Reference to the main application
        LanguageServer server,
        String docUri,
        String sourceCode,
        long docVersion,
        Consumer<String> onCodeUpdate, // A callback to trigger a refresh
        BlockDragAndDropManager dragAndDropManager
) {}