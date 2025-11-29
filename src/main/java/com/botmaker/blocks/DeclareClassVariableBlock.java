package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
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
 * Example: private int score = 0;
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
        container.getStyleClass().add("field-declaration-block");
        container.setStyle(
                "-fx-background-color: linear-gradient(to right, #F39C12 0%, #E67E22 100%);" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: rgba(0,0,0,0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;"
        );

        // Row 1: Modifiers Label
        Label modifiersLabel = new Label((isPrivate ? "Private" : "Public") + (isStatic ? " Static" : "") + " Field");
        modifiersLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-text-transform: uppercase;");

        // Row 2: Type, Name, and Initializer
        HBox mainRow = new HBox(8);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Type Label with Menu
        Label typeLabel = createTypeLabel(getDisplayTypeName(variableType));
        typeLabel.setCursor(Cursor.HAND);
        Tooltip.install(typeLabel, new Tooltip("Click to change type"));
        typeLabel.setOnMouseClicked(e -> showTypeMenu(typeLabel, context));

        // Name Field
        TextField nameField = TextFieldComponents.createVariableNameField(variableName, newName -> {
            FieldDeclaration fieldDecl = (FieldDeclaration) this.astNode;
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
            if (!newName.equals(variableName) && !newName.isEmpty()) {
                context.codeEditor().replaceSimpleName(fragment.getName(), newName);
            }
        });

        // Equals Label
        Label equalsLabel = createKeywordLabel("=");

        // Initializer Display
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

        // Add Button
        String typeString = variableType.toString();
        String uiTargetType = TypeManager.determineUiType(typeString,
                context.applicationState().getCompilationUnit().orElse(null));

        Button addButton = createAddButton(e -> {
            Expression currentInitializer = null;
            if (initializer != null) {
                currentInitializer = (Expression) initializer.getAstNode();
            } else {
                FieldDeclaration fieldDecl = (FieldDeclaration) this.astNode;
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
                currentInitializer = fragment.getInitializer();
            }

            Expression finalCurrentInitializer = currentInitializer;
            ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(uiTargetType, type -> {
                if (finalCurrentInitializer != null) {
                    context.codeEditor().replaceExpression(finalCurrentInitializer, type);
                } else {
                    context.codeEditor().setFieldInitializer(
                            (FieldDeclaration) this.astNode,
                            type
                    );
                }
            });
            menu.show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
        });

        // Delete Button
        Button deleteBtn = createDeleteButton(context);
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 0; -fx-cursor: hand;");

        mainRow.getChildren().addAll(typeLabel, nameField, equalsLabel, initNode, addButton);

        // Assemble
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().addAll(modifiersLabel, BlockUIComponents.createSpacer(), deleteBtn);

        container.getChildren().addAll(headerRow, mainRow);

        return container;
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();
        String currentStr = variableType.toString();
        boolean isArrayListType = isArrayList(variableType);
        boolean isArray = variableType.isArrayType();

        final String baseType = extractBaseType(currentStr, isArrayListType, isArray);

        // Toggle ArrayList
        MenuItem toggleList = new MenuItem(isArrayListType ? "Convert to Single Value" : "Convert to ArrayList");
        toggleList.setStyle("-fx-font-weight: bold;");
        toggleList.setOnAction(e -> {
            String newType = isArrayListType ? baseType : "ArrayList<" + TypeManager.toWrapperType(baseType) + ">";
            context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
        });
        menu.getItems().add(toggleList);

        // Nested List Option
        if (isArrayListType) {
            MenuItem makeNested = new MenuItem("Make ArrayList of ArrayLists");
            makeNested.setOnAction(e -> {
                String newType = "ArrayList<ArrayList<" + TypeManager.toWrapperType(baseType) + ">>";
                context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
            });
            menu.getItems().add(makeNested);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // Change Base Type - Fundamental Types
        Menu changeBaseMenu = new Menu("Change to Primitive Type");
        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                String newType = isArrayListType ? "ArrayList<" + TypeManager.toWrapperType(type) + ">" : type;
                context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }
        menu.getItems().add(changeBaseMenu);

        // Change to Enum Type
        List<String> availableEnums = getAvailableEnums(context);
        if (!availableEnums.isEmpty()) {
            Menu changeEnumMenu = new Menu("Change to Enum Type");
            for (String enumName : availableEnums) {
                MenuItem item = new MenuItem(enumName);
                item.setOnAction(e -> {
                    String newType = isArrayListType ? "ArrayList<" + enumName + ">" : enumName;
                    context.codeEditor().replaceFieldType((FieldDeclaration) this.astNode, newType);
                });
                changeEnumMenu.getItems().add(item);
            }
            menu.getItems().add(changeEnumMenu);
        }

        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private String extractBaseType(String currentStr, boolean isArrayListType, boolean isArray) {
        if (isArrayListType && currentStr.contains("<")) {
            return currentStr.substring(currentStr.indexOf("<") + 1, currentStr.lastIndexOf(">"));
        } else if (isArray) {
            return currentStr.replace("[]", "");
        }
        return currentStr;
    }

    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label open = new Label("[");
        open.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        Label close = new Label("]");
        close.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        listBox.getChildren().addAll(open, initializer.getUINode(context), close);
        return listBox;
    }

    private String getDisplayTypeName(Type type) {
        String typeName = type.toString();
        if (isArrayList(type)) return typeName;
        if (typeName.endsWith("[]")) return typeName.replace("[]", " list");
        return typeName;
    }

    private boolean isArrayList(Type type) {
        return type.toString().startsWith("ArrayList");
    }

    private List<String> getAvailableEnums(CompletionContext context) {
        List<String> enumNames = new ArrayList<>();

        CompilationUnit cu = context.applicationState().getCompilationUnit().orElse(null);
        if (cu == null) return enumNames;

        if (!cu.types().isEmpty() && cu.types().getFirst() instanceof TypeDeclaration) {
            TypeDeclaration typeDecl = (TypeDeclaration) cu.types().getFirst();

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