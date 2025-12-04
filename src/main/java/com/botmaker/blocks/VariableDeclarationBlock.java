package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.AddableExpression;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.TextFieldComponents;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

import static com.botmaker.ui.components.BlockUIComponents.createTypeLabel;

public class VariableDeclarationBlock extends AbstractStatementBlock {

    private final String variableName;
    private final Type variableType;
    private ExpressionBlock initializer;

    public VariableDeclarationBlock(String id, VariableDeclarationStatement astNode) {
        super(id, astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);
        this.variableName = fragment.getName().getIdentifier();
        this.variableType = astNode.getType();
        this.initializer = null;
    }

    public void setInitializer(ExpressionBlock initializer) { this.initializer = initializer; }

    @Override
    protected Node createUINode(CompletionContext context) {
        String typeString = variableType.toString();
        String uiTargetType = TypeManager.determineUiType(typeString,
                context.applicationState().getCompilationUnit().orElse(null));

        Label typeLabel = createTypeLabel(getDisplayTypeName(variableType));
        typeLabel.setCursor(Cursor.HAND);
        Tooltip.install(typeLabel, new Tooltip("Click to change type"));
        typeLabel.setOnMouseClicked(e -> showTypeMenu(typeLabel, context));

        TextField nameField = TextFieldComponents.createVariableNameField(variableName, newName -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment)
                    ((VariableDeclarationStatement) this.astNode).fragments().get(0);
            if (!newName.equals(variableName) && !newName.isEmpty()) {
                context.codeEditor().replaceSimpleName(fragment.getName(), newName);
            }
        });

        Node initNode;
        if (initializer != null) {
            if (initializer instanceof ListBlock) {
                initNode = initializer.getUINode(context);
            } else if (initializer.getAstNode() instanceof org.eclipse.jdt.core.dom.ArrayInitializer) {
                initNode = createListDisplay(context);
            } else {
                initNode = initializer.getUINode(context);
            }
        } else {
            initNode = createExpressionDropZone(context);
        }

        Button addButton = createAddButton(e -> {
            Expression currentInitializer = initializer != null ?
                    (Expression) initializer.getAstNode() : null;

            ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(uiTargetType, type -> {
                if (currentInitializer != null) {
                    context.codeEditor().replaceExpression(currentInitializer, type);
                } else {
                    context.codeEditor().setVariableInitializer(
                            (VariableDeclarationStatement) this.astNode, type);
                }
            });
            menu.show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
        });

        var sentence = BlockLayout.sentence()
                .addNode(typeLabel)
                .addNode(nameField)
                .addKeyword("=")
                .addNode(initNode)
                .addNode(addButton)
                .build();

        return BlockLayout.header()
                .withCustomNode(sentence)
                .withDeleteButton(() -> context.codeEditor().deleteStatement(
                        (org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build();
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();
        String currentStr = variableType.toString();
        boolean isArray = variableType.isArrayType();

        // 1. Add/Remove Dimension Logic
        MenuItem addDim = new MenuItem("Add Dimension []");
        addDim.setOnAction(e -> {
            String newType = currentStr + "[]";
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
        });
        menu.getItems().add(addDim);

        if (isArray) {
            MenuItem removeDim = new MenuItem("Remove Dimension []");
            removeDim.setOnAction(e -> {
                if (currentStr.endsWith("[]")) {
                    String newType = currentStr.substring(0, currentStr.length() - 2);
                    context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
                }
            });
            menu.getItems().add(removeDim);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // 2. Change Base Type
        Menu changeBaseMenu = new Menu("Change Base Type");

        // Primitive Types
        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                // Preserve dimensions when changing base type
                String newType = preserveDimensions(currentStr, type);
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }

        // Enum Types
        List<String> availableEnums = getAvailableEnums(context);
        if (!availableEnums.isEmpty()) {
            changeBaseMenu.getItems().add(new SeparatorMenuItem());
            for (String enumName : availableEnums) {
                MenuItem item = new MenuItem(enumName);
                item.setOnAction(e -> {
                    String newType = preserveDimensions(currentStr, enumName);
                    context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
                });
                changeBaseMenu.getItems().add(item);
            }
        }
        menu.getItems().add(changeBaseMenu);

        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private String preserveDimensions(String oldType, String newBase) {
        int dims = 0;
        String temp = oldType;
        while (temp.endsWith("[]")) {
            dims++;
            temp = temp.substring(0, temp.length() - 2);
        }
        return newBase + "[]".repeat(dims);
    }

    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label open = new Label("{"); open.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label close = new Label("}"); close.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        listBox.getChildren().addAll(open, initializer.getUINode(context), close);
        return listBox;
    }

    private String getDisplayTypeName(Type type) {
        String typeName = type.toString();
        // Just show the type as-is (e.g. "int[][]")
        return typeName;
    }

    // Get all available enum types from the compilation unit
    private List<String> getAvailableEnums(CompletionContext context) {
        List<String> enumNames = new ArrayList<>();

        CompilationUnit cu = context.applicationState().getCompilationUnit().orElse(null);
        if (cu == null) return enumNames;

        if (!cu.types().isEmpty() && cu.types().get(0) instanceof TypeDeclaration) {
            TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);

            for (Object obj : typeDecl.bodyDeclarations()) {
                if (obj instanceof EnumDeclaration) {
                    EnumDeclaration enumDecl = (EnumDeclaration) obj;
                    enumNames.add(enumDecl.getName().getIdentifier());
                }
                else if (obj instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) obj;
                    if (method.getBody() != null) {
                        findLocalEnums(method.getBody(), enumNames);
                    }
                }
            }
        }
        else if (!cu.types().isEmpty() && cu.types().get(0) instanceof EnumDeclaration) {
            EnumDeclaration enumDecl = (EnumDeclaration) cu.types().get(0);
            enumNames.add(enumDecl.getName().getIdentifier());
        }

        return enumNames;
    }

    private void findLocalEnums(ASTNode node, List<String> enumNames) {
        if (node instanceof TypeDeclarationStatement) {
            TypeDeclarationStatement tds = (TypeDeclarationStatement) node;
            if (tds.getDeclaration() instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) tds.getDeclaration();
                enumNames.add(enumDecl.getName().getIdentifier());
            }
        }
        if (node instanceof Block) {
            for (Object stmt : ((Block) node).statements()) {
                findLocalEnums((ASTNode) stmt, enumNames);
            }
        }
    }
}