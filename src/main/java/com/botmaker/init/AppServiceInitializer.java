// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\init\AppServiceInitializer.java
package com.botmaker.init;

import com.botmaker.blocks.ClassBlock;
import com.botmaker.blocks.MethodDeclarationBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.di.DependencyContainer;
import com.botmaker.services.*;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.UIManager;
import com.botmaker.util.BlockLookupHelper;
import org.eclipse.jdt.core.dom.BodyDeclaration;
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
        manager.setCallback(dropInfo -> {
            if (dropInfo.targetClass() != null) {
                if (dropInfo.type() == AddableBlock.METHOD_DECLARATION) {
                    editorService.getCodeEditor().addMethodToClass(
                            (TypeDeclaration) dropInfo.targetClass().getAstNode(),
                            "newMethod", "void", dropInfo.insertionIndex()
                    );
                } else if (dropInfo.type() == AddableBlock.DECLARE_ENUM) {
                    editorService.getCodeEditor().addEnumToClass(
                            (TypeDeclaration) dropInfo.targetClass().getAstNode(),
                            "NewEnum", dropInfo.insertionIndex()
                    );
                }
            } else if (dropInfo.targetBody() != null) {
                editorService.getCodeEditor().addStatement(
                        dropInfo.targetBody(), dropInfo.type(), dropInfo.insertionIndex()
                );
            }
        });

        // Handle moving existing blocks
        manager.setMoveCallback(moveInfo -> {
            CodeBlock blockToMove = null;

            // Find block by ID (could be statement or method)
            // BlockLookupHelper usually searches nodeToBlockMap values.
            // We iterate manually if needed or use existing helper if it supports generalized CodeBlock.
            for (CodeBlock b : state.getNodeToBlockMap().values()) {
                if (b.getId().equals(moveInfo.blockId())) {
                    blockToMove = b;
                    break;
                }
            }

            if (blockToMove == null) return;

            // Case A: Moving a statement within a body
            if (blockToMove instanceof StatementBlock && moveInfo.targetBody() != null) {
                StatementBlock stmt = (StatementBlock) blockToMove;
                BodyBlock sourceBody = BlockLookupHelper.findParentBody(stmt, state.getNodeToBlockMap());
                if (sourceBody != null) {
                    editorService.getCodeEditor().moveStatement(
                            stmt, sourceBody, moveInfo.targetBody(), moveInfo.insertionIndex()
                    );
                }
            }
            // Case B: Moving a declaration (Method/Enum) within a Class
            else if (blockToMove instanceof MethodDeclarationBlock && moveInfo.targetClass() != null) {
                // Move Method
                BodyDeclaration decl = (BodyDeclaration) blockToMove.getAstNode();
                TypeDeclaration targetType = (TypeDeclaration) moveInfo.targetClass().getAstNode();
                editorService.getCodeEditor().moveBodyDeclaration(decl, targetType, moveInfo.insertionIndex());
            }
            // (Add Enum handling here if needed similarly)
        });
    }
}