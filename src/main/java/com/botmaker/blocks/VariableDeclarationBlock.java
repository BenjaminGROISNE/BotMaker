package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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

    public void setInitializer(ExpressionBlock initializer) {
        this.initializer = initializer;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("variable-declaration-block");

        // --- TYPE SELECTOR ---
        Label typeLabel = new Label(getDisplayTypeName(variableType));
        typeLabel.getStyleClass().add("type-label");
        typeLabel.setCursor(Cursor.HAND);

        Tooltip tooltip = new Tooltip("Click to change type (List/Base)");
        Tooltip.install(typeLabel, tooltip);

        typeLabel.setOnMouseClicked(e -> showTypeMenu(typeLabel, context));
        container.getChildren().add(typeLabel);

        // --- NAME FIELD ---
        TextField nameField = new TextField(variableName);
        nameField.getStyleClass().add("variable-name-field");
        nameField.setPrefWidth(100);
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String newName = nameField.getText();
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) this.astNode).fragments().get(0);
                String currentName = fragment.getName().getIdentifier();

                if (!newName.equals(currentName) && !newName.isEmpty()) {
                    context.codeEditor().replaceSimpleName(fragment.getName(), newName);
                }
            }
        });
        container.getChildren().add(nameField);

        // --- EQUALS ---
        Label equalsLabel = new Label("=");
        equalsLabel.getStyleClass().add("keyword-label");
        container.getChildren().add(equalsLabel);

        // --- INITIALIZER ---
        if (initializer != null) {
            if (initializer.getAstNode() instanceof org.eclipse.jdt.core.dom.ArrayInitializer) {
                container.getChildren().add(createListDisplay(context));
            } else {
                container.getChildren().add(initializer.getUINode(context));
            }
        } else {
            container.getChildren().add(createExpressionDropZone(context));
        }

        // --- ADD BUTTON ---
        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");

        // DYNAMIC TYPE FILTERING
        // Calculate the UI type string (number, boolean, list, String) from the AST Type
        String uiTargetType = TypeManager.determineUiType(variableType.toString());
        addButton.setOnAction(e -> showExpressionMenu(addButton, context, uiTargetType));

        container.getChildren().add(addButton);

        // --- SPACER & DELETE ---
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();

        String currentStr = variableType.toString();
        boolean isArray = variableType.isArrayType();
        String baseType = isArray ? currentStr.replace("[]", "") : currentStr;

        // 1. Option to toggle List status
        MenuItem toggleList = new MenuItem(isArray ? "Convert to Single Value" : "Convert to List");
        toggleList.setStyle("-fx-font-weight: bold;");
        toggleList.setOnAction(e -> {
            String newType;
            if (isArray) {
                // Remove one level of array (int[][] -> int[])
                int lastIndex = currentStr.lastIndexOf("[]");
                newType = currentStr.substring(0, lastIndex);
            } else {
                // Add array level
                newType = currentStr + "[]";
            }
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
        });
        menu.getItems().add(toggleList);

        // Option for nested list if already a list
        if (isArray) {
            MenuItem makeNested = new MenuItem("Make List of Lists");
            makeNested.setOnAction(e -> {
                String newType = currentStr + "[]";
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            menu.getItems().add(makeNested);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // 2. Change Base Type
        Menu changeBaseMenu = new Menu("Change Base Type");
        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                // Preserve array dimensions, just change base
                String dims = currentStr.substring(currentStr.indexOf(baseType) + baseType.length());
                String newType = type + dims;
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }
        menu.getItems().add(changeBaseMenu);

        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label openBracket = new Label("[");
        openBracket.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        listBox.getChildren().add(openBracket);

        if (initializer instanceof ListBlock) {
            ListBlock listBlock = (ListBlock) initializer;
            if (listBlock.getElements().isEmpty()) {
                Label emptyLabel = new Label("empty");
                emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #999; -fx-font-size: 10px;");
                listBox.getChildren().add(emptyLabel);
            } else {
                Label countLabel = new Label(listBlock.getElements().size() + " items");
                countLabel.setStyle("-fx-font-style: italic;");
                listBox.getChildren().add(countLabel);
            }
        } else {
            listBox.getChildren().add(initializer.getUINode(context));
        }

        Label closeBracket = new Label("]");
        closeBracket.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        listBox.getChildren().add(closeBracket);

        return listBox;
    }

    private String getDisplayTypeName(Type type) {
        String typeName = type.toString();
        if (typeName.endsWith("[]")) {
            // Convert "int[][]" to "int list list" for display, or keep symbols
            return typeName.replace("[]", " list");
        }
        return typeName;
    }

    // UPDATED: Accepts targetType and filters
    private void showExpressionMenu(Button button, CompletionContext context, String targetType) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-control-inner-background: white;");

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setStyle("-fx-text-fill: black;");

            menuItem.setOnAction(e -> {
                if (initializer != null) {
                    context.codeEditor().replaceExpression(
                            (org.eclipse.jdt.core.dom.Expression) initializer.getAstNode(),
                            type
                    );
                }
            });
            menu.getItems().add(menuItem);
        }
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}