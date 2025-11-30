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

    // VariableDeclarationBlock.java
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
        boolean isArrayListType = isArrayList(variableType);
        boolean isArray = variableType.isArrayType();
        boolean isEnumType = isEnum(context);

        final String baseType = extractBaseType(currentStr, isArrayListType, isArray);

        // Toggle ArrayList
        MenuItem toggleList = new MenuItem(isArrayListType ? "Convert to Single Value" : "Convert to ArrayList");
        toggleList.setStyle("-fx-font-weight: bold;");
        toggleList.setOnAction(e -> {
            String newType = isArrayListType ? baseType : "ArrayList<" + TypeManager.toWrapperType(baseType) + ">";
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
        });
        menu.getItems().add(toggleList);

        // Nested List Option
        if (isArrayListType) {
            MenuItem makeNested = new MenuItem("Make ArrayList of ArrayLists");
            makeNested.setOnAction(e -> {
                String newType = "ArrayList<ArrayList<" + TypeManager.toWrapperType(baseType) + ">>";
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
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
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }
        menu.getItems().add(changeBaseMenu);

        // NEW: Change to Enum Type
        List<String> availableEnums = getAvailableEnums(context);
        if (!availableEnums.isEmpty()) {
            Menu changeEnumMenu = new Menu("Change to Enum Type");
            for (String enumName : availableEnums) {
                MenuItem item = new MenuItem(enumName);
                item.setOnAction(e -> {
                    String newType = isArrayListType ? "ArrayList<" + enumName + ">" : enumName;
                    context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
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

        Label open = new Label("["); open.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label close = new Label("]"); close.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

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

    // NEW: Check if current type is an enum
    private boolean isEnum(CompletionContext context) {
        String typeName = variableType.toString();
        // Remove ArrayList wrapper if present
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            typeName = typeName.substring(10, typeName.length() - 1);
        }
        return getAvailableEnums(context).contains(typeName);
    }

    // NEW: Get all available enum types from the compilation unit
    private List<String> getAvailableEnums(CompletionContext context) {
        List<String> enumNames = new ArrayList<>();

        CompilationUnit cu = context.applicationState().getCompilationUnit().orElse(null);
        if (cu == null) return enumNames;

        // Get the main type declaration
        if (!cu.types().isEmpty() && cu.types().getFirst() instanceof TypeDeclaration) {
            TypeDeclaration typeDecl = (TypeDeclaration) cu.types().getFirst();

            // Look through all body declarations for enums
            for (Object obj : typeDecl.bodyDeclarations()) {
                if (obj instanceof EnumDeclaration) {
                    EnumDeclaration enumDecl = (EnumDeclaration) obj;
                    enumNames.add(enumDecl.getName().getIdentifier());
                }
                // Also check for enums inside methods (local enums in the AST, even if invalid)
                else if (obj instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) obj;
                    if (method.getBody() != null) {
                        findLocalEnums(method.getBody(), enumNames);
                    }
                }
            }
        }
        // Check if the root is itself an enum file
        else if (!cu.types().isEmpty() && cu.types().get(0) instanceof EnumDeclaration) {
            EnumDeclaration enumDecl = (EnumDeclaration) cu.types().get(0);
            enumNames.add(enumDecl.getName().getIdentifier());
        }

        return enumNames;
    }

    // NEW: Recursively find local enum declarations in method bodies
    private void findLocalEnums(ASTNode node, List<String> enumNames) {
        if (node instanceof TypeDeclarationStatement) {
            TypeDeclarationStatement tds = (TypeDeclarationStatement) node;
            if (tds.getDeclaration() instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) tds.getDeclaration();
                enumNames.add(enumDecl.getName().getIdentifier());
            }
        }
        // Recursively search children
        if (node instanceof Block) {
            for (Object stmt : ((Block) node).statements()) {
                findLocalEnums((ASTNode) stmt, enumNames);
            }
        }
    }
}