package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.*;

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

        Tooltip tooltip = new Tooltip("Click to change type (ArrayList/Base)");
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
            // FIX: Prioritize rendering the initializer if it exists, especially if it's a ListBlock
            if (initializer instanceof ListBlock) {
                // Render the interactive list UI directly
                container.getChildren().add(initializer.getUINode(context));
            }
            else if (initializer.getAstNode() instanceof ArrayInitializer) {
                // Fallback for primitive arrays {1,2,3} to use bracket styling if not caught by ListBlock
                container.getChildren().add(createListDisplay(context));
            }
            else {
                // Standard expression
                container.getChildren().add(initializer.getUINode(context));
            }
        } else {
            // Only show placeholder if truly null (e.g. int x;)
            container.getChildren().add(createExpressionDropZone(context));
        }

        // --- ADD BUTTON ---
        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");

        // DYNAMIC TYPE FILTERING
        String uiTargetType = TypeManager.determineUiType(variableType.toString());
        addButton.setOnAction(e -> showExpressionMenu(addButton, context, uiTargetType));

        container.getChildren().add(addButton);

        // --- SPACER & DELETE ---
        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        container.getChildren().addAll(spacer, deleteButton);

        return container;
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();

        String currentStr = variableType.toString();
        boolean isArrayListType = isArrayList(variableType);
        boolean isArray = variableType.isArrayType();

        final String baseType = extractBaseType(currentStr, isArrayListType, isArray);

        // 1. Toggle between ArrayList and Single Value
        MenuItem toggleList = new MenuItem(isArrayListType ? "Convert to Single Value" : "Convert to ArrayList");
        toggleList.setStyle("-fx-font-weight: bold;");
        toggleList.setOnAction(e -> {
            String newType;
            if (isArrayListType) {
                newType = baseType;
            } else {
                // FIX: Ensure wrapper type (e.g., Integer, Boolean) is used
                newType = "ArrayList<" + TypeManager.toWrapperType(baseType) + ">";
            }
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
        });
        menu.getItems().add(toggleList);

        // Option for nested list
        if (isArrayListType) {
            MenuItem makeNested = new MenuItem("Make ArrayList of ArrayLists");
            makeNested.setOnAction(e -> {
                // FIX: Ensure inner type is also wrapped properly
                // Result: ArrayList<ArrayList<Boolean>>
                String newType = "ArrayList<ArrayList<" + TypeManager.toWrapperType(baseType) + ">>";
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            menu.getItems().add(makeNested);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // 2. Change Base Type
        Menu changeBaseMenu = new Menu("Change Base Type");
        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            final String finalType = type;
            item.setOnAction(e -> {
                String newType;
                if (isArrayListType) {
                    // FIX: Ensure wrapper type is used when switching types in a list
                    newType = "ArrayList<" + TypeManager.toWrapperType(finalType) + ">";
                } else {
                    newType = finalType;
                }
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }
        menu.getItems().add(changeBaseMenu);

        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private String extractBaseType(String currentStr, boolean isArrayListType, boolean isArray) {
        if (isArrayListType) {
            if (currentStr.contains("<") && currentStr.contains(">")) {
                int start = currentStr.indexOf("<") + 1;
                int end = currentStr.lastIndexOf(">"); // Fix: use lastIndexOf for nested
                return currentStr.substring(start, end);
            }
        } else if (isArray) {
            return currentStr.replace("[]", "");
        }
        return currentStr;
    }

    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label openBracket = new Label("[");
        openBracket.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        listBox.getChildren().add(openBracket);

        if (initializer instanceof ListBlock) {
            // If it's a ListBlock but forced here (e.g. array), render it
            listBox.getChildren().add(initializer.getUINode(context));
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
        if (isArrayList(type)) {
            return typeName;
        }
        if (typeName.endsWith("[]")) {
            return typeName.replace("[]", " list");
        }
        return typeName;
    }

    private boolean isArrayList(Type type) {
        String typeName = type.toString();
        return typeName.startsWith("ArrayList<") || typeName.equals("ArrayList");
    }

    private void showExpressionMenu(Button button, CompletionContext context, String targetType) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-control-inner-background: white;");

        for (AddableExpression type : AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setStyle("-fx-text-fill: black;");

            menuItem.setOnAction(e -> {
                if (initializer != null) {
                    context.codeEditor().replaceExpression(
                            (Expression) initializer.getAstNode(),
                            type
                    );
                }
            });
            menu.getItems().add(menuItem);
        }
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}