package com.botmaker;

import com.botmaker.blocks.MainBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.lsp.JdtLanguageServerLauncher;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.CodeEditor;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.runtime.DebuggingManager;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.UIManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    private LanguageServer jdtServer;
    private final BlockFactory factory = new BlockFactory();
    private String docUri;
    private String currentCode;
    private long docVersion = 1;

    private Map<ASTNode, CodeBlock> nodeToBlockMap;
    private CodeBlock highlightedBlock;

    private UIManager uiManager;
    private CodeExecutionService executionService;
    private DebuggingManager debuggingManager;
    private BlockDragAndDropManager dragAndDropManager;
    private AstRewriter astRewriter;
    private CodeEditor codeEditor;
    private com.botmaker.validation.DiagnosticsManager diagnosticsManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        diagnosticsManager = new com.botmaker.validation.DiagnosticsManager();
        JdtLanguageServerLauncher launcher = new JdtLanguageServerLauncher(Paths.get("tools/jdt-language-server"), (params) -> {
            // This is called on a background thread.
            // We pass the params to the FX thread to do all UI work.
            Platform.runLater(() -> {
                diagnosticsManager.processDiagnostics(params.getDiagnostics());
                uiManager.updateErrors(diagnosticsManager.getDiagnostics());
                uiManager.getStatusLabel().setText(diagnosticsManager.getErrorSummary());
            });
        });
        jdtServer = launcher.getServer();

        astRewriter = new AstRewriter();
        codeEditor = new CodeEditor(this, astRewriter, factory);

        dragAndDropManager = new BlockDragAndDropManager(dropInfo ->
                codeEditor.addStatement(dropInfo.targetBody(), dropInfo.type(), dropInfo.insertionIndex()));

        uiManager = new UIManager(this, dragAndDropManager);
        primaryStage.setScene(uiManager.createScene());

        executionService = new CodeExecutionService(
                uiManager.getOutputArea()::appendText,
                uiManager.getOutputArea()::clear,
                uiManager.getOutputArea()::setText,
                uiManager.getStatusLabel()::setText,
                diagnosticsManager
        );

        debuggingManager = new DebuggingManager(
                executionService,
                uiManager.getStatusLabel()::setText,
                uiManager.getOutputArea()::appendText,
                uiManager.getOutputArea()::clear,
                uiManager::onDebuggerStarted,
                uiManager::onDebuggerPaused,
                uiManager::onDebuggerResumed,
                uiManager::onDebuggerFinished,
                this::highlightBlock,
                factory
        );

        Path docPath = Paths.get("./projects/src/main/java/com/demo/Demo.java").toAbsolutePath();
        docUri = docPath.toUri().toString();
        currentCode = Files.readString(docPath);

        jdtServer.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
                new TextDocumentItem(docUri, "java", (int) docVersion, currentCode)
        ));

        refreshUI(currentCode);

        primaryStage.setTitle("BotMaker Blocks");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            launcher.stop();
            Platform.exit();
        });
    }

    public void compileCode() {
        executionService.compileCode(currentCode);
    }

    public void runCode() {
        executionService.runCode(currentCode);
    }

    public void startDebugging() {
        debuggingManager.setNodeToBlockMap(nodeToBlockMap);
        debuggingManager.startDebugging(currentCode);
    }

    public void resumeDebugging() {
        debuggingManager.resume();
    }

    public void handleCodeUpdate(String newCode) {
        try {
            Files.writeString(Paths.get(new java.net.URI(docUri)), newCode);
        } catch (java.io.IOException | java.net.URISyntaxException e) {
            e.printStackTrace();
            Platform.runLater(() -> uiManager.getStatusLabel().setText("Error saving file: " + e.getMessage()));
        }

        this.docVersion++;
        jdtServer.getTextDocumentService().didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(docUri, (int) docVersion),
                List.of(new TextDocumentContentChangeEvent(newCode))
        ));
        refreshUI(newCode);
    }

    private void refreshUI(String javaCode) {
        this.currentCode = javaCode;
        this.nodeToBlockMap = new HashMap<>();
        if (diagnosticsManager != null) {
            diagnosticsManager.updateSource(nodeToBlockMap, currentCode);
        }
        uiManager.getBlocksContainer().getChildren().clear();

        CompletionContext context = new CompletionContext(
                this,
                codeEditor,
                jdtServer,
                docUri,
                currentCode,
                docVersion,
                dragAndDropManager
        );

        MainBlock rootBlock = factory.convert(javaCode, nodeToBlockMap, dragAndDropManager);

        if (rootBlock != null) {
            uiManager.getBlocksContainer().getChildren().add(rootBlock.getUINode(context));
        }
        uiManager.getStatusLabel().setText("UI Refreshed.");
    }

    private void highlightBlock(CodeBlock block) {
        if (highlightedBlock != null) {
            highlightedBlock.unhighlight();
        }
        highlightedBlock = block;
        if (highlightedBlock != null) {
            highlightedBlock.highlight();
        }
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public BlockFactory getBlockFactory() {
        return factory;
    }

    public LanguageServer getJdtServer() {
        return jdtServer;
    }

    public String getDocUri() {
        return docUri;
    }

    public com.botmaker.validation.DiagnosticsManager getDiagnosticsManager() {
        return diagnosticsManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
