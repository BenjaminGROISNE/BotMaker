package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.project.ProjectFile;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.util.TypeInfo;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

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
                    // Check if it's a TypeDeclaration (Class) or EnumDeclaration
                    if (targetFile.getAst().types().get(0) instanceof TypeDeclaration) {
                        TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                        boolean isLocal = selectedFile.equals(finalCurrentFileClass);

                        for (MethodDeclaration md : type.getMethods()) {
                            int mods = md.getModifiers();
                            boolean isStatic = Modifier.isStatic(mods);
                            boolean isPublic = Modifier.isPublic(mods);

                            // For local calls, show everything. For external, show public static.
                            // Note: This logic assumes we only call static methods on other classes for now.
                            if (isLocal || (isPublic && isStatic)) {
                                String name = md.getName().getIdentifier();
                                if (!methodSelector.getItems().contains(name)) {
                                    methodSelector.getItems().add(name);
                                }
                            }
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

            // When changing method name, default to the first available signature
            List<String> paramTypes = findParameterTypes(context, newScopeDisplay, newMethodName, 0); // Default to index 0

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

        // --- NEW: Signature Selection Button ---
        MenuButton signatureBtn = new MenuButton("⚙");
        signatureBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4; -fx-background-radius: 10;");
        signatureBtn.setTooltip(new Tooltip("Select Method Signature (Overloads)"));

        signatureBtn.setOnShowing(e -> {
            signatureBtn.getItems().clear();
            String currentScope = fileSelector.getValue();
            String currentMethod = methodSelector.getValue();

            List<MethodSignature> signatures = findSignatures(context, currentScope, currentMethod);

            if (signatures.isEmpty()) {
                MenuItem empty = new MenuItem("No signatures found");
                empty.setDisable(true);
                signatureBtn.getItems().add(empty);
            } else {
                for (MethodSignature sig : signatures) {
                    MenuItem item = new MenuItem(sig.toString());
                    item.setOnAction(ev -> {
                        String scopeForAST = currentScope.equals(finalCurrentFileClass) ? "" : currentScope;
                        context.codeEditor().updateMethodInvocation(
                                (MethodInvocation) this.astNode,
                                scopeForAST,
                                currentMethod,
                                sig.paramTypes
                        );
                    });
                    signatureBtn.getItems().add(item);
                }
            }
        });

        sentenceBuilder.addNode(signatureBtn);
        // ---------------------------------------

        // Sync button (Argument Count Check)
        // We compare against the *currently matched* signature if possible, or just checking if *any* signature matches args.
        // For simplicity, we check if the current argument count matches ANY valid signature.

        boolean isValidArgCount = isValidArgumentCount(context, displayValue, methodName, arguments.size());

        Button syncBtn = new Button("⟳");
        if (!isValidArgCount) {
            syncBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 4 2 4; -fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold;");
            syncBtn.setTooltip(new Tooltip("⚠ Argument count mismatch! Click to reset to default signature"));
        } else {
            syncBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 4 2 4; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: #90EE90;");
            syncBtn.setTooltip(new Tooltip("Arguments valid ✓"));
        }

        syncBtn.setOnAction(e -> {
            String currentScope = fileSelector.getValue();
            String currentMethod = methodSelector.getValue();
            String scopeForAST = currentScope.equals(finalCurrentFileClass) ? "" : currentScope;
            // Reset to first signature found
            List<String> paramTypes = findParameterTypes(context, currentScope, currentMethod, 0);
            context.codeEditor().updateMethodInvocation(
                    (MethodInvocation) this.astNode, scopeForAST, currentMethod, paramTypes);
        });

        sentenceBuilder.addNode(syncBtn);

        // Arguments
        sentenceBuilder.addLabel("(");

        // Try to find parameter names based on current argument count
        List<String> paramNames = findParameterNamesForCount(context, displayValue, methodName, arguments.size());

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

            // Allow deleting specific arguments manually
            Button delBtn = new Button("×");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 10px; -fx-padding: 0 0 0 2; -fx-cursor: hand;");
            int index = i;
            delBtn.setOnAction(e -> context.codeEditor().deleteElementFromList(this.astNode, index));
            argBox.getChildren().add(delBtn);

            sentenceBuilder.addNode(argBox);
        }

        // Add Argument Button
        MenuButton addArgBtn = new MenuButton("+");
        addArgBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");

        // Determine expected type for next argument (if a signature matches)
        TypeInfo nextParamType = getParameterTypeInfoAt(context, displayValue, methodName, arguments.size());
        List<com.botmaker.ui.AddableExpression> availableTypes =
                com.botmaker.ui.AddableExpression.getForType(nextParamType);

        for (com.botmaker.ui.AddableExpression type : availableTypes) {
            MenuItem item = new MenuItem(type.getDisplayName());
            item.setOnAction(e -> context.codeEditor().addArgumentToMethodInvocation(
                    (MethodInvocation) this.astNode, type));
            addArgBtn.getItems().add(item);
        }
        sentenceBuilder.addNode(addArgBtn);

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

    private static class MethodSignature {
        String name;
        List<String> paramTypes;
        List<String> paramNames;

        public MethodSignature(String name, List<String> types, List<String> names) {
            this.name = name;
            this.paramTypes = types;
            this.paramNames = names;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name).append("(");
            for (int i = 0; i < paramTypes.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(paramTypes.get(i)).append(" ").append(paramNames.get(i));
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private List<MethodSignature> findSignatures(CompletionContext context, String className, String methodName) {
        List<MethodSignature> signatures = new ArrayList<>();
        ProjectFile targetFile = findProjectFile(context, className);

        if (targetFile != null) {
            ensureAstParsed(targetFile);
            if (targetFile.getAst() != null && !targetFile.getAst().types().isEmpty()) {
                if (targetFile.getAst().types().get(0) instanceof TypeDeclaration) {
                    TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                    for (MethodDeclaration md : type.getMethods()) {
                        if (md.getName().getIdentifier().equals(methodName)) {
                            List<String> types = new ArrayList<>();
                            List<String> names = new ArrayList<>();
                            for (Object p : md.parameters()) {
                                SingleVariableDeclaration param = (SingleVariableDeclaration) p;
                                types.add(param.getType().toString());
                                names.add(param.getName().getIdentifier());
                            }
                            signatures.add(new MethodSignature(methodName, types, names));
                        }
                    }
                }
            }
        }
        return signatures;
    }

    // Helper: Find types for a specific overload index, or default to first
    private List<String> findParameterTypes(CompletionContext context, String className, String methodName, int signatureIndex) {
        List<MethodSignature> sigs = findSignatures(context, className, methodName);
        if (sigs.isEmpty()) return new ArrayList<>();
        if (signatureIndex >= 0 && signatureIndex < sigs.size()) {
            return sigs.get(signatureIndex).paramTypes;
        }
        return sigs.get(0).paramTypes;
    }

    // Helper: Find names based on matching argument count
    private List<String> findParameterNamesForCount(CompletionContext context, String className, String methodName, int argCount) {
        List<MethodSignature> sigs = findSignatures(context, className, methodName);
        for (MethodSignature sig : sigs) {
            if (sig.paramTypes.size() == argCount) {
                return sig.paramNames;
            }
        }
        // Fallback: return first signature's names or empty
        return !sigs.isEmpty() ? sigs.get(0).paramNames : new ArrayList<>();
    }

    private boolean isValidArgumentCount(CompletionContext context, String className, String methodName, int currentCount) {
        List<MethodSignature> sigs = findSignatures(context, className, methodName);
        for (MethodSignature sig : sigs) {
            if (sig.paramTypes.size() == currentCount) return true;
        }
        return false;
    }

    /**
     * Tries to find the parameter type at a specific index.
     * Considers the signature that best matches current argument count, or defaults to the first one.
     */
    private TypeInfo getParameterTypeInfoAt(CompletionContext context, String className, String methodName, int index) {
        List<MethodSignature> sigs = findSignatures(context, className, methodName);

        // Try to find a signature that is longer than current index
        // Prefer one that matches current arguments size + 1 (the one we are adding)
        MethodSignature bestMatch = null;

        // 1. Exact match for current size + 1
        for (MethodSignature sig : sigs) {
            if (sig.paramTypes.size() == index + 1) {
                bestMatch = sig;
                break;
            }
        }

        // 2. Any signature long enough
        if (bestMatch == null) {
            for (MethodSignature sig : sigs) {
                if (sig.paramTypes.size() > index) {
                    bestMatch = sig;
                    break;
                }
            }
        }

        if (bestMatch != null && index < bestMatch.paramTypes.size()) {
            return TypeInfo.from(bestMatch.paramTypes.get(index));
        }

        return TypeInfo.UNKNOWN;
    }
}