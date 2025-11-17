package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import org.eclipse.jdt.core.dom.*;

/**
 * Handles code editing operations.
 * Phase 1 Refactoring: Now uses ApplicationState and EventBus instead of Main reference.
 */
public class CodeEditor {

    // ============================================
    // PHASE 1 CHANGES: Replace Main dependency
    // ============================================
    private final ApplicationState state;
    private final EventBus eventBus;

    // KEPT: Original dependencies
    private final AstRewriter astRewriter;
    private final BlockFactory blockFactory;

    // ============================================
    // PHASE 1: CHANGED - New constructor signature
    // OLD: public CodeEditor(Main mainApp, AstRewriter astRewriter, BlockFactory blockFactory)
    // NEW: Uses state and eventBus instead of mainApp
    // ============================================
    public CodeEditor(ApplicationState state, EventBus eventBus,
                      AstRewriter astRewriter, BlockFactory blockFactory) {
        this.state = state;
        this.eventBus = eventBus;
        this.astRewriter = astRewriter;
        this.blockFactory = blockFactory;
    }

    // ============================================
    // PHASE 1: CHANGED - Use state instead of mainApp
    // ============================================
    private String getCurrentCode() {
        return state.getCurrentCode();
    }

    private CompilationUnit getCompilationUnit() {
        return state.getCompilationUnit().orElse(null);
    }

    // ============================================
    // PHASE 1: CHANGED - Publish event instead of calling Main
    // OLD: Platform.runLater(() -> mainApp.handleCodeUpdate(newCode));
    // NEW: eventBus.publish(...)
    // ============================================
    private void triggerUpdate(String newCode) {
        String previousCode = getCurrentCode();
        eventBus.publish(new CoreApplicationEvents.CodeUpdatedEvent(newCode, previousCode));
    }

    // ============================================
    // KEPT: All editing methods unchanged
    // ============================================

    public void replaceLiteralValue(Expression toReplace, String newLiteralValue) {
        String newCode = astRewriter.replaceLiteral(
                getCompilationUnit(),
                getCurrentCode(),
                toReplace,
                newLiteralValue
        );
        triggerUpdate(newCode);
    }

    public void addStringArgumentToMethodInvocation(MethodInvocation mi, String text) {
        CompilationUnit cu = getCompilationUnit();
        if (cu == null) return;

        AST ast = cu.getAST();
        StringLiteral newArg = ast.newStringLiteral();
        newArg.setLiteralValue(text);

        String newCode = astRewriter.addArgumentToMethodInvocation(
                cu,
                getCurrentCode(),
                mi,
                newArg
        );
        triggerUpdate(newCode);
    }

    public void replaceExpression(Expression toReplace, AddableExpression type) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.replaceExpression(
                getCompilationUnit(),
                getCurrentCode(),
                toReplace,
                type
        );
        triggerUpdate(newCode);
    }

    public void addStatement(BodyBlock targetBody, AddableBlock type, int index) {
        blockFactory.setMarkNewIdentifiersAsUnedited(true);
        String newCode = astRewriter.addStatement(
                getCompilationUnit(),
                getCurrentCode(),
                targetBody,
                type,
                index
        );
        triggerUpdate(newCode);
    }

    public void deleteElseFromIfStatement(IfStatement ifStmt) {
        String newCode = astRewriter.deleteElseFromIfStatement(
                getCompilationUnit(),
                getCurrentCode(),
                ifStmt
        );
        triggerUpdate(newCode);
    }

    public void convertElseToElseIf(IfStatement ifStmt) {
        String newCode = astRewriter.convertElseToElseIf(
                getCompilationUnit(),
                getCurrentCode(),
                ifStmt
        );
        triggerUpdate(newCode);
    }

    public void addElseToIfStatement(IfStatement ifStmt) {
        String newCode = astRewriter.addElseToIfStatement(
                getCompilationUnit(),
                getCurrentCode(),
                ifStmt
        );
        triggerUpdate(newCode);
    }

    public void replaceSimpleName(SimpleName toReplace, String newName) {
        String newCode = astRewriter.replaceSimpleName(
                getCompilationUnit(),
                getCurrentCode(),
                toReplace,
                newName
        );
        triggerUpdate(newCode);
    }

    public void deleteStatement(Statement toDelete) {
        String newCode = astRewriter.deleteNode(
                getCompilationUnit(),
                getCurrentCode(),
                toDelete
        );
        triggerUpdate(newCode);
    }

    public void replaceVariableType(VariableDeclarationStatement toReplace, String newTypeName) {
        String newCode = astRewriter.replaceVariableType(
                getCompilationUnit(),
                getCurrentCode(),
                toReplace,
                newTypeName
        );
        triggerUpdate(newCode);
    }
}