package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.project.ProjectFile;
import com.botmaker.ui.AddableExpression;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
            this.scopeName = ""; // Represents implicit local call
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
        HBox container = new HBox(6);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("method-call-block");

        if (!isStatementContext) {
            container.setStyle("-fx-background-color: #34495E; -fx-background-radius: 12; -fx-padding: 3 8 3 8;");
        } else {
            container.setStyle("-fx-background-color: #34495E; -fx-background-radius: 4; -fx-padding: 5 10 5 10;");
        }

        Label callLabel = new Label("Call");
        callLabel.setStyle("-fx-text-fill: #aaa; -fx-font-weight: bold; -fx-font-size: 10px;");

        // --- Prepare Data ---
        String currentFileClass = "";
        if (context.applicationState() != null && context.applicationState().getActiveFile() != null) {
            currentFileClass = context.applicationState().getActiveFile().getClassName();
        }

        // --- File/Class Selector ---
        ComboBox<String> fileSelector = new ComboBox<>();

        // 1. Populate with REAL files only
        if (context.applicationState() != null) {
            for (ProjectFile file : context.applicationState().getAllFiles()) {
                fileSelector.getItems().add(file.getClassName());
            }
        }

        // 2. Set Display Value
        String displayValue;
        if (scopeName.isEmpty()) {
            displayValue = currentFileClass;
        } else {
            displayValue = scopeName;
        }

        // 3. Handle "Phantom" Values
        if (!fileSelector.getItems().contains(displayValue)) {
            fileSelector.getItems().add(0, displayValue);
        }

        fileSelector.setValue(displayValue);
        fileSelector.setStyle("-fx-font-size: 11px; -fx-pref-width: 100px;");

        // --- Method Selector ---
        ComboBox<String> methodSelector = new ComboBox<>();
        methodSelector.setValue(methodName);
        methodSelector.setEditable(false); // CHANGED: No longer editable manually
        methodSelector.setStyle("-fx-font-size: 11px; -fx-pref-width: 120px; -fx-font-weight: bold;");

        final String finalCurrentFileClass = currentFileClass;

        // --- Logic to populate methods ---
        Runnable populateMethodList = () -> {
            String selectedFile = fileSelector.getValue();
            methodSelector.getItems().clear();

            ProjectFile targetFile = null;

            // Find the target file object
            if (context.applicationState() != null) {
                for (ProjectFile file : context.applicationState().getAllFiles()) {
                    if (file.getClassName().equals(selectedFile)) {
                        targetFile = file;
                        break;
                    }
                }
            }

            if (targetFile != null) {
                // BUG FIX: Parse AST if it doesn't exist (file hasn't been opened yet)
                if (targetFile.getAst() == null) {
                    try {
                        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
                        parser.setSource(targetFile.getContent().toCharArray());
                        parser.setKind(ASTParser.K_COMPILATION_UNIT);
                        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                        targetFile.setAst(cu);
                    } catch (Exception e) {
                        System.err.println("Error parsing file for methods: " + e.getMessage());
                    }
                }

                // Populate list from AST
                if (targetFile.getAst() != null && !targetFile.getAst().types().isEmpty()) {
                    TypeDeclaration type = (TypeDeclaration) targetFile.getAst().types().get(0);
                    boolean isLocal = selectedFile.equals(finalCurrentFileClass);

                    for (MethodDeclaration md : type.getMethods()) {
                        int mods = md.getModifiers();
                        // Show methods if they are Static+Public (External) OR if it's the local file
                        boolean isStatic = Modifier.isStatic(mods);
                        boolean isPublic = Modifier.isPublic(mods);

                        if (isLocal || (isPublic && isStatic)) {
                            methodSelector.getItems().add(md.getName().getIdentifier());
                        }
                    }
                }
            }
        };

        // Listener: When Method is selected, update code structure
        methodSelector.setOnAction(e -> {
            String newMethodName = methodSelector.getValue();
            if (newMethodName == null) return; // Ignore null selections (e.g. during clear)

            String newScopeDisplay = fileSelector.getValue();
            String newScopeAST = newScopeDisplay.equals(finalCurrentFileClass) ? "" : newScopeDisplay;

            if (newMethodName.equals(methodName) && newScopeAST.equals(scopeName)) return;

            List<String> paramTypes = findParameterTypes(context, newScopeDisplay, newMethodName);

            context.codeEditor().updateMethodInvocation(
                    (MethodInvocation) this.astNode,
                    newScopeAST,
                    newMethodName,
                    paramTypes
            );
        });

        // Listener: When File changes, repopulate methods
        fileSelector.setOnAction(e -> {
            populateMethodList.run();
            methodSelector.show();
        });

        // Initial Population
        populateMethodList.run();

        container.getChildren().addAll(callLabel, fileSelector, new Label("."), methodSelector);

        // --- Arguments UI ---
        Label argsLabel = new Label("(");
        argsLabel.setStyle("-fx-text-fill: white;");
        container.getChildren().add(argsLabel);

        for (ExpressionBlock arg : arguments) {
            container.getChildren().add(arg.getUINode(context));
        }

        MenuButton addArgBtn = new MenuButton("+");
        addArgBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
        for (AddableExpression type : AddableExpression.values()) {
            MenuItem item = new MenuItem(type.getDisplayName());
            item.setOnAction(e -> {
                context.codeEditor().addArgumentToMethodInvocation(
                        (MethodInvocation) this.astNode,
                        type
                );
            });
            addArgBtn.getItems().add(item);
        }
        container.getChildren().add(addArgBtn);

        Label closeParen = new Label(")");
        closeParen.setStyle("-fx-text-fill: white;");
        container.getChildren().add(closeParen);

        if (isStatementContext) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button deleteButton = new Button("X");
            deleteButton.setOnAction(e -> context.codeEditor().deleteStatement((Statement) this.astNode.getParent()));
            container.getChildren().addAll(spacer, deleteButton);
        }

        return container;
    }

    private List<String> findParameterTypes(CompletionContext context, String className, String methodName) {
        List<String> types = new ArrayList<>();
        if (context.applicationState() == null) return types;

        ProjectFile targetFile = null;
        for (ProjectFile file : context.applicationState().getAllFiles()) {
            if (file.getClassName().equals(className)) {
                targetFile = file;
                break;
            }
        }

        if (targetFile != null) {
            // Ensure AST exists for parameter lookup as well
            if (targetFile.getAst() == null) {
                try {
                    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
                    parser.setSource(targetFile.getContent().toCharArray());
                    parser.setKind(ASTParser.K_COMPILATION_UNIT);
                    CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                    targetFile.setAst(cu);
                } catch (Exception ignored) {}
            }

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
}