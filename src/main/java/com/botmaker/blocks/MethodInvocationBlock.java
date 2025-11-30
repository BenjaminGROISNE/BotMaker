package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.project.ProjectFile;
import com.botmaker.ui.AddableExpression;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

import static com.botmaker.ui.components.BlockUIComponents.createDeleteButton;

public class MethodInvocationBlock extends AbstractExpressionBlock implements StatementBlock {

    private String scopeName;
    private String methodName;
    private final List<ExpressionBlock> arguments = new ArrayList<>();
    private boolean isStatementContext = false;

    public MethodInvocationBlock(String id, ASTNode astNode) {
        super(id, resolveExpressionNode(astNode));

        if (astNode instanceof ExpressionStatement) {
            this.isStatementContext = true;
        }

        MethodInvocation mi = (MethodInvocation) this.astNode;
        this.methodName = mi.getName().getIdentifier();
        if (mi.getExpression() != null) {
            this.scopeName = mi.getExpression().toString();
        } else {
            this.scopeName = ""; // Local
        }
    }

    private static MethodInvocation resolveExpressionNode(ASTNode node) {
        if (node instanceof ExpressionStatement) {
            return (MethodInvocation) ((ExpressionStatement) node).getExpression();
        }
        return (MethodInvocation) node;
    }

    public void addArgument(ExpressionBlock arg) {
        arguments.add(arg);
    }

    // MethodInvocationBlock.java
    @Override
    protected Node createUINode(CompletionContext context) {
        String currentFileClass = "";
        if (context.applicationState() != null && context.applicationState().getActiveFile() != null) {
            currentFileClass = context.applicationState().getActiveFile().getClassName();
        }

        // File Selector
        ComboBox<String> fileSelector = new ComboBox<>();
        if (context.applicationState() != null) {
            for (com.botmaker.project.ProjectFile file : context.applicationState().getAllFiles()) {
                fileSelector.getItems().add(file.getClassName());
            }
        }

        String displayValue = scopeName.isEmpty() ? currentFileClass : scopeName;
        if (!fileSelector.getItems().contains(displayValue)) {
            fileSelector.getItems().add(0, displayValue);
        }
        fileSelector.setValue(displayValue);
        fileSelector.setStyle("-fx-font-size: 11px; -fx-pref-width: 100px;");

        // Method Selector
        ComboBox<String> methodSelector = new ComboBox<>();
        methodSelector.setValue(methodName);
        methodSelector.setEditable(false);
        methodSelector.setStyle("-fx-font-size: 11px; -fx-pref-width: 120px; -fx-font-weight: bold;");

        final String finalCurrentFileClass = currentFileClass;

        Runnable populateMethodList = () -> {
            String selectedFile = fileSelector.getValue();
            methodSelector.getItems().clear();

            com.botmaker.project.ProjectFile targetFile = findProjectFile(context, selectedFile);

            if (targetFile != null) {
                ensureAstParsed(targetFile);

                if (targetFile.getAst() != null && !targetFile.getAst().types().isEmpty()) {
                    TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                    boolean isLocal = selectedFile.equals(finalCurrentFileClass);

                    for (MethodDeclaration md : type.getMethods()) {
                        int mods = md.getModifiers();
                        boolean isStatic = Modifier.isStatic(mods);
                        boolean isPublic = Modifier.isPublic(mods);

                        if (isLocal || (isPublic && isStatic)) {
                            methodSelector.getItems().add(md.getName().getIdentifier());
                        }
                    }
                }

                if (methodSelector.getItems().size() == 1) {
                    methodSelector.getSelectionModel().select(0);
                }
            }
        };

        methodSelector.setOnAction(e -> {
            String newMethodName = methodSelector.getValue();
            if (newMethodName == null) return;

            String newScopeDisplay = fileSelector.getValue();
            String newScopeAST = newScopeDisplay.equals(finalCurrentFileClass) ? "" : newScopeDisplay;

            List<String> paramTypes = findParameterTypes(context, newScopeDisplay, newMethodName);

            context.codeEditor().updateMethodInvocation(
                    (MethodInvocation) this.astNode,
                    newScopeAST,
                    newMethodName,
                    paramTypes
            );
        });

        fileSelector.setOnAction(e -> {
            populateMethodList.run();
            methodSelector.show();
        });

        populateMethodList.run();

        // Build main sentence
        var sentenceBuilder = BlockLayout.sentence()
                .addLabel("Call")
                .addNode(fileSelector)
                .addLabel(".")
                .addNode(methodSelector);

        // Sync button
        int expectedParamCount = findParameterTypes(context, displayValue, methodName).size();
        boolean hasMismatch = arguments.size() != expectedParamCount;

        Button syncBtn = new Button("⟳");
        if (hasMismatch) {
            syncBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 4 2 4; -fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold;");
            syncBtn.setTooltip(new Tooltip("⚠ Arguments don't match! Click to sync"));
        } else {
            syncBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 4 2 4; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: #90EE90;");
            syncBtn.setTooltip(new Tooltip("Arguments match signature ✓"));
        }
        syncBtn.setOnAction(e -> {
            String currentScope = fileSelector.getValue();
            String currentMethod = methodSelector.getValue();
            String scopeForAST = currentScope.equals(finalCurrentFileClass) ? "" : currentScope;
            List<String> paramTypes = findParameterTypes(context, currentScope, currentMethod);
            context.codeEditor().updateMethodInvocation(
                    (MethodInvocation) this.astNode, scopeForAST, currentMethod, paramTypes);
        });

        sentenceBuilder.addNode(syncBtn);

        // Arguments
        sentenceBuilder.addLabel("(");

        List<String> paramNames = findParameterNames(context, displayValue, methodName);

        for (int i = 0; i < arguments.size(); i++) {
            ExpressionBlock arg = arguments.get(i);
            HBox argBox = new HBox(2);
            argBox.setAlignment(Pos.CENTER_LEFT);
            argBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 4; -fx-padding: 2;");

            if (i < paramNames.size()) {
                Label paramNameLabel = new Label(paramNames.get(i) + ":");
                paramNameLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 9px; -fx-padding: 0 4 0 2;");
                argBox.getChildren().add(paramNameLabel);
            }

            argBox.getChildren().add(arg.getUINode(context));

            Button delBtn = new Button("×");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 10px; -fx-padding: 0 0 0 2; -fx-cursor: hand;");
            int index = i;
            delBtn.setOnAction(e -> context.codeEditor().deleteElementFromList(this.astNode, index));
            argBox.getChildren().add(delBtn);

            sentenceBuilder.addNode(argBox);
        }

        if (arguments.size() < expectedParamCount) {
            MenuButton addArgBtn = new MenuButton("+");
            addArgBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");

            String nextParamType = getParameterTypeAt(context, displayValue, methodName, arguments.size());
            List<com.botmaker.ui.AddableExpression> availableTypes = com.botmaker.ui.AddableExpression.getForType(nextParamType);

            for (com.botmaker.ui.AddableExpression type : availableTypes) {
                MenuItem item = new MenuItem(type.getDisplayName());
                item.setOnAction(e -> context.codeEditor().addArgumentToMethodInvocation(
                        (MethodInvocation) this.astNode, type));
                addArgBtn.getItems().add(item);
            }
            sentenceBuilder.addNode(addArgBtn);
        } else if (arguments.size() > expectedParamCount) {
            Label warningLabel = new Label("⚠ Too many");
            warningLabel.setStyle("-fx-text-fill: #FFA500; -fx-font-size: 9px; -fx-font-weight: bold;");
            sentenceBuilder.addNode(warningLabel);
        }

        sentenceBuilder.addLabel(")");

        HBox container = sentenceBuilder.build();
        container.setStyle("-fx-background-color: #34495E; -fx-background-radius: " +
                (isStatementContext ? "4" : "12") + "; -fx-padding: " +
                (isStatementContext ? "5 10 5 10" : "3 8 3 8") + ";");

        if (isStatementContext) {
            container.getChildren().addAll(
                    BlockUIComponents.createSpacer(),
                    BlockUIComponents.createDeleteButton(() ->
                            context.codeEditor().deleteStatement((Statement) this.astNode.getParent()))
            );
        }

        return container;
    }

    // --- Helpers ---

    private ProjectFile findProjectFile(CompletionContext context, String className) {
        if (context.applicationState() == null) return null;
        for (ProjectFile file : context.applicationState().getAllFiles()) {
            if (file.getClassName().equals(className)) {
                return file;
            }
        }
        return null;
    }

    private void ensureAstParsed(ProjectFile file) {
        if (file.getAst() == null) {
            try {
                ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
                parser.setSource(file.getContent().toCharArray());
                parser.setKind(ASTParser.K_COMPILATION_UNIT);
                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                file.setAst(cu);
            } catch (Exception ignored) {}
        }
    }

    private List<String> findParameterTypes(CompletionContext context, String className, String methodName) {
        List<String> types = new ArrayList<>();
        ProjectFile targetFile = findProjectFile(context, className);

        if (targetFile != null) {
            ensureAstParsed(targetFile);
            if (targetFile.getAst() != null && !targetFile.getAst().types().isEmpty()) {
                TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                for (MethodDeclaration md : type.getMethods()) {
                    if (md.getName().getIdentifier().equals(methodName)) {
                        for (Object p : md.parameters()) {
                            SingleVariableDeclaration param = (SingleVariableDeclaration) p;
                            types.add(param.getType().toString());
                        }
                        return types;
                    }
                }
            }
        }
        return types;
    }

    // NEW: Helper to get parameter names for UI labels
    private List<String> findParameterNames(CompletionContext context, String className, String methodName) {
        List<String> names = new ArrayList<>();
        ProjectFile targetFile = findProjectFile(context, className);

        if (targetFile != null) {
            ensureAstParsed(targetFile);
            if (targetFile.getAst() != null && !targetFile.getAst().types().isEmpty()) {
                TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                for (MethodDeclaration md : type.getMethods()) {
                    if (md.getName().getIdentifier().equals(methodName)) {
                        for (Object p : md.parameters()) {
                            SingleVariableDeclaration param = (SingleVariableDeclaration) p;
                            names.add(param.getName().getIdentifier());
                        }
                        return names;
                    }
                }
            }
        }
        return names;
    }

    // NEW: Helper to get the UI type for a specific parameter position
    private String getParameterTypeAt(CompletionContext context, String className, String methodName, int index) {
        List<String> types = findParameterTypes(context, className, methodName);
        if (index >= 0 && index < types.size()) {
            return com.botmaker.util.TypeManager.determineUiType(types.get(index));
        }
        return "any"; // Fallback
    }
}