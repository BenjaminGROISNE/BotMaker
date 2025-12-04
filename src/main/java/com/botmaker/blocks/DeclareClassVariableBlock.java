package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.TextFieldComponents;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

import static com.botmaker.ui.components.BlockUIComponents.createTypeLabel;

/**
 * Represents a class-level field declaration (instance or static variable).
 */
public class DeclareClassVariableBlock extends AbstractStatementBlock {

    private final String variableName;
    private final Type variableType;
    private final boolean isStatic;
    private final boolean isPrivate;
    private ExpressionBlock initializer;

    public DeclareClassVariableBlock(String id, FieldDeclaration astNode) {
        super(id, astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);
        this.variableName = fragment.getName().getIdentifier();
        this.variableType = astNode.getType();
        this.isStatic = Modifier.isStatic(astNode.getModifiers());
        this.isPrivate = Modifier.isPrivate(astNode.getModifiers());
        this.initializer = null;
    }

    public void setInitializer(ExpressionBlock initializer) {
        this.initializer = initializer;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.setStyle(
                "-fx-background-color: linear-gradient(to right, #F39C12 0%, #E67E22 100%);" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: rgba(0,0,0,0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;"
        );

        Label modifiersLabel = new Label((isPrivate ? "Private" : "Public") + (isStatic ? " Static" : "") + " Field");
        modifiersLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label typeLabel = createTypeLabel(variableType.toString());
        typeLabel.setCursor(Cursor.HAND);
        Tooltip.install(typeLabel, new Tooltip("Click to change type"));
        typeLabel.setOnMouseClicked(e -> showTypeMenu(typeLabel, context));

        TextField nameField = TextFieldComponents.createVariableNameField(variableName, newName -> {
            FieldDeclaration fieldDecl = (FieldDeclaration) this.astNode;
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
            if (!newName.equals(variableName) && !newName.isEmpty()) {
                context.codeEditor().replaceSimpleName(fragment.getName(), newName);
            }
        });

        String typeString = variableType.toString();
        String uiTargetType = TypeManager.determineUiType(typeString,
                context.applicationState().getCompilationUnit().orElse(null));

        var mainRowBuilder = BlockLayout.sentence()
                .addNode(typeLabel)
                .addNode(nameField);

        if (initializer == null) {
            Button setValueBtn = new Button("Set Value");
            setValueBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.3);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11px;" +
                            "-fx-padding: 4 12 4 12;" +
                            "-fx-background-radius: 4;" +
                            "-fx-cursor: hand;"
            );
            setValueBtn.setOnAction(e -> {
                context.codeEditor().setFieldInitializerToDefault(
                        (FieldDeclaration) this.astNode, uiTargetType);
            });
            mainRowBuilder.addNode(setValueBtn);
        } else {
            Node initNode = (initializer instanceof ListBlock) ?
                    initializer.getUINode(context) :
                    (initializer.getAstNode() instanceof org.eclipse.jdt.core.dom.ArrayInitializer) ?
                            createListDisplay(context) : initializer.getUINode(context);

            Button addButton = createAddButton(e -> {
                Expression currentInitializer = (Expression) initializer.getAstNode();
                ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(uiTargetType, type -> {
                    context.codeEditor().replaceExpression(currentInitializer, type);
                });
                menu.show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
            });

            mainRowBuilder
                    .addKeyword("=")
                    .addNode(initNode)
                    .addNode(addButton);
        }

        HBox mainRow = mainRowBuilder.build();

        Button deleteBtn = createDeleteButton(context);
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 0; -fx-cursor: hand;");

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().addAll(modifiersLabel, BlockUIComponents.createSpacer(), deleteBtn);

        container.getChildren().addAll(headerRow, mainRow);

        return container;
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();
        String currentStr = variableType.toString();
        boolean isArray = variableType.isArrayType();

        // 1. Add/Remove Dimension Logic
        MenuItem addDim = new MenuItem("Add Dimension []");
        addDim.setOnAction(e -> {
            String newType = currentStr + "[]";
            context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
        });
        menu.getItems().add(addDim);

        if (isArray) {
            MenuItem removeDim = new MenuItem("Remove Dimension []");
            removeDim.setOnAction(e -> {
                if (currentStr.endsWith("[]")) {
                    String newType = currentStr.substring(0, currentStr.length() - 2);
                    context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
                }
            });
            menu.getItems().add(removeDim);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // 2. Change Base Type
        Menu changeBaseMenu = new Menu("Change Base Type");

        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                String newType = preserveDimensions(currentStr, type);
                context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }

        List<String> availableEnums = getAvailableEnums(context);
        if (!availableEnums.isEmpty()) {
            changeBaseMenu.getItems().add(new SeparatorMenuItem());
            for (String enumName : availableEnums) {
                MenuItem item = new MenuItem(enumName);
                item.setOnAction(e -> {
                    String newType = preserveDimensions(currentStr, enumName);
                    context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
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

        Label open = new Label("["); open.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        Label close = new Label("]"); close.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        listBox.getChildren().addAll(open, initializer.getUINode(context), close);
        return listBox;
    }

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
            }
        }
        return enumNames;
    }
}