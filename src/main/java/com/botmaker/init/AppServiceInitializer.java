package com.botmaker.init;

import com.botmaker.blocks.ClassBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.di.DependencyContainer;
import com.botmaker.services.*;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.UIManager;
import com.botmaker.util.BlockLookupHelper;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class AppServiceInitializer {

    public static void initialize(DependencyContainer container) throws Exception {
        // 1. Initialize Language Server
        LanguageServerService lss = container.resolve(LanguageServerService.class);
        lss.initialize();

        // 2. Resolve Services needed for wiring
        CodeEditorService codeEditorService = container.resolve(CodeEditorService.class);
        ApplicationState state = container.resolve(ApplicationState.class);
        BlockDragAndDropManager dragAndDropManager = container.resolve(BlockDragAndDropManager.class);

        // 3. Wire up Drag and Drop Logic
        setupDragAndDropCallbacks(dragAndDropManager, codeEditorService, state);

        // 4. Ensure other services are instantiated
        container.resolve(ExecutionService.class);
        container.resolve(DebuggingService.class);
        container.resolve(UIManager.class);
    }

    private static void setupDragAndDropCallbacks(BlockDragAndDropManager manager,
                                                  CodeEditorService editorService,
                                                  ApplicationState state) {
        // Handle adding new blocks
// ... inside setupDragAndDropCallbacks ...
        manager.setCallback(dropInfo -> {
            // Check if dropping into a CLASS
            if (dropInfo.targetClass() != null) {
                if (dropInfo.type() == AddableBlock.METHOD_DECLARATION) {
                    editorService.getCodeEditor().addMethodToClass(
                            (TypeDeclaration) dropInfo.targetClass().getAstNode(),
                            "newMethod", "void", dropInfo.insertionIndex()
                    );
                }
                else if (dropInfo.type() == AddableBlock.DECLARE_ENUM) {
                    editorService.getCodeEditor().addEnumToClass(
                            (TypeDeclaration) dropInfo.targetClass().getAstNode(),
                            "NewEnum", dropInfo.insertionIndex()
                    );
                }
            }
            // Check if dropping into a BODY (Method)
            else if (dropInfo.targetBody() != null) {
                editorService.getCodeEditor().addStatement(
                        dropInfo.targetBody(), dropInfo.type(), dropInfo.insertionIndex()
                );
            }
        });

        // Handle moving existing blocks
        manager.setMoveCallback(moveInfo -> {
            StatementBlock blockToMove = BlockLookupHelper.findBlockById(
                    moveInfo.blockId(),
                    state.getNodeToBlockMap()
            );

            if (blockToMove != null) {
                BodyBlock sourceBody = BlockLookupHelper.findParentBody(
                        blockToMove,
                        state.getNodeToBlockMap()
                );

                if (sourceBody != null) {
                    editorService.getCodeEditor().moveStatement(
                            blockToMove,
                            sourceBody,
                            moveInfo.targetBody(),
                            moveInfo.insertionIndex()
                    );
                }
            }
        });
    }
}