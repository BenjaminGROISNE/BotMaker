package com.botmaker.parser;

import com.botmaker.Main;
import com.botmaker.core.BodyBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import javafx.application.Platform;
import org.eclipse.jdt.core.dom.*;

public class CodeEditor {

    private final Main mainApp;
    private final AstRewriter astRewriter;
    private final BlockFactory blockFactory;

    public CodeEditor(Main mainApp, AstRewriter astRewriter, BlockFactory blockFactory) {
        this.mainApp = mainApp;
        this.astRewriter = astRewriter;
        this.blockFactory = blockFactory;
    }

    private String getCurrentCode() {
        return mainApp.getCurrentCode();
    }

    private CompilationUnit getCompilationUnit() {
        return blockFactory.getCompilationUnit();
    }

    private void triggerUpdate(String newCode) {
        Platform.runLater(() -> mainApp.handleCodeUpdate(newCode));
    }

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
        AST ast = getCompilationUnit().getAST();
        StringLiteral newArg = ast.newStringLiteral();
        newArg.setLiteralValue(text);

        String newCode = astRewriter.addArgumentToMethodInvocation(
                getCompilationUnit(),
                getCurrentCode(),
                mi,
                newArg
        );
        triggerUpdate(newCode);
    }

    public void replaceExpression(Expression toReplace, AddableExpression type) {
        String newCode = astRewriter.replaceExpression(
                getCompilationUnit(),
                getCurrentCode(),
                toReplace,
                type
        );
        triggerUpdate(newCode);
    }

    public void addStatement(BodyBlock targetBody, AddableBlock type, int index) {
        String newCode = astRewriter.addStatement(
                getCompilationUnit(),
                getCurrentCode(),
                targetBody,
                type,
                index
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
}
