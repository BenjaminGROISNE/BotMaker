package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Visual block for lists.
 * Supports:
 * 1. Array Initializers: {1, 2, 3}
 * 2. List Factories: Arrays.asList(1, 2, 3) or List.of(1, 2, 3)
 */
public class ListBlock extends AbstractExpressionBlock {

    private final List<ExpressionBlock> elements = new ArrayList<>();
    private final boolean isFixedArray; // True if it's a {}, False if it's Arrays.asList

    public ListBlock(String id, ASTNode astNode) {
        super(id, astNode);
        this.isFixedArray = (astNode instanceof ArrayInitializer);
    }

    public void addElement(ExpressionBlock element) {
        this.elements.add(element);
    }

    public List<ExpressionBlock> getElements() {
        return elements;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("list-block");

        boolean isNested = (this.astNode.getParent() instanceof ArrayInitializer) ||
                (this.astNode.getParent() instanceof MethodInvocation);

        if (isNested) {
            container.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.15); -fx-border-width: 1;");
            container.setPadding(new Insets(4, 6, 4, 6));
        } else {
            container.setPadding(new Insets(6, 10, 6, 10));
        }

        // --- Header Row ---
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Calculate logic
        String itemType = determineItemType();
        // If itemType is "list", we are holding sub-lists.
        // If itemType is "boolean"/"number", we are holding values.

        String typeLabel = isFixedArray ? "Array" : "List";

        // Debug label helpful for verification:
        // Label listLabel = new Label(typeLabel + "<" + itemType + "> (" + elements.size() + ")");
        Label listLabel = new Label(typeLabel + " (" + elements.size() + ")");
        listLabel.getStyleClass().add("list-label");

        if (!isFixedArray) {
            listLabel.setStyle("-fx-text-fill: #aaddff;");
        }

        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");
        addButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");

        addButton.setOnAction(e -> showAddElementMenu(addButton, context, elements.size(), itemType));

        headerRow.getChildren().addAll(listLabel, addButton);
        container.getChildren().add(headerRow);

        // --- Elements ---
        if (elements.isEmpty()) {
            Label emptyLabel = new Label(" (empty) ");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px;");
            container.getChildren().add(emptyLabel);
        } else {
            VBox elementsContainer = new VBox(3);
            elementsContainer.setPadding(new Insets(2, 0, 0, 12));

            for (int i = 0; i < elements.size(); i++) {
                // Pass itemType down so row knows what it contains
                HBox elementRow = createElementRow(i, elements.get(i), context, itemType);
                elementsContainer.getChildren().add(elementRow);
            }
            container.getChildren().add(elementsContainer);
        }

        return container;
    }

    /**
     * Logic to determine what kind of items this specific ListBlock should contain.
     * It walks up the AST to find the Variable Declaration, calculates total nesting depth,
     * calculates current depth, and decides if we need "list" or a leaf type.
     */
    private String determineItemType() {
        ASTNode current = this.astNode;
        ASTNode rootDefinition = null;
        int currentDepth = 0;

        // 1. Walk up to find the root definition and count current depth
        while (current != null) {
            // If we hit another ArrayInitializer, we are one level deeper
            if (current instanceof ArrayInitializer) {
                if (current != this.astNode) {
                    currentDepth++;
                }
            }
            // If we hit another Arrays.asList, we are one level deeper
            else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                if ("asList".equals(mi.getName().getIdentifier()) || "of".equals(mi.getName().getIdentifier())) {
                    // If we aren't the starting node, increment depth
                    if (current != this.astNode) {
                        currentDepth++;
                    }
                }
            }

            // Found declaration: int[][] x = ...
            if (current instanceof VariableDeclarationFragment) {
                rootDefinition = current;
                break;
            }
            // Found declaration in standard usage
            if (current instanceof VariableDeclarationStatement) {
                rootDefinition = current;
                break;
            }
            // Found field declaration
            if (current instanceof FieldDeclaration) {
                rootDefinition = current;
                break;
            }
            // Found usage inside new ArrayList<>(...)
            if (current instanceof ClassInstanceCreation) {
                rootDefinition = current;
                break;
            }

            current = current.getParent();
        }

        // 2. Analyze the root type
        String rootTypeStr = "any";

        if (rootDefinition instanceof VariableDeclarationStatement) {
            rootTypeStr = ((VariableDeclarationStatement) rootDefinition).getType().toString();
        } else if (rootDefinition instanceof VariableDeclarationFragment) {
            ASTNode parent = rootDefinition.getParent();
            if (parent instanceof VariableDeclarationStatement) {
                rootTypeStr = ((VariableDeclarationStatement) parent).getType().toString();
            } else if (parent instanceof FieldDeclaration) {
                rootTypeStr = ((FieldDeclaration) parent).getType().toString();
            }
        } else if (rootDefinition instanceof ClassInstanceCreation) {
            rootTypeStr = ((ClassInstanceCreation) rootDefinition).getType().toString();
        } else if (rootDefinition instanceof FieldDeclaration) {
            rootTypeStr = ((FieldDeclaration) rootDefinition).getType().toString();
        }

        // 3. Calculate Dimensions
        // e.g. int[][] -> genericDepth = 2, leafType = "int"
        int genericDepth = TypeManager.getListNestingLevel(rootTypeStr);
        String leafType = TypeManager.getLeafType(rootTypeStr); // e.g. "int"

        // 4. Determine UI Type based on depth comparison
        // If genericDepth is 2 (int[][])
        // currentDepth 0 (Root) -> needs "list" (to make depth 1)
        // currentDepth 1 (Middle) -> needs leafType ("int")

        int remainingLevels = genericDepth - 1 - currentDepth;

        System.out.println("[Debug ListBlock] RootType: " + rootTypeStr +
                " | TotalDepth: " + genericDepth +
                " | CurrentDepth: " + currentDepth +
                " | RemainingLevels: " + remainingLevels +
                " | LeafType: " + leafType);

        if (remainingLevels > 0) {
            return "list";
        } else {
            return TypeManager.determineUiType(leafType);
        }
    }

    private HBox createElementRow(int index, ExpressionBlock element, CompletionContext context, String itemType) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle("-fx-font-family: monospace; -fx-text-fill: #666; -fx-font-size: 9px; -fx-min-width: 10px;");

        Node elementNode = element.getUINode(context);
        if (element instanceof ListBlock) {
            HBox.setHgrow(elementNode, javafx.scene.layout.Priority.ALWAYS);
        }

        Button changeButton = new Button("↻");
        changeButton.getStyleClass().add("icon-button");
        changeButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-opacity: 0.3;");
        changeButton.setOnAction(e -> showChangeElementMenu(changeButton, context, index, itemType));

        Button deleteButton = new Button("✕");
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-text-fill: #ff5555; -fx-opacity: 0.3;");
        deleteButton.setOnAction(e -> deleteElement(index, context));

        row.setOnMouseEntered(e -> {
            changeButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-opacity: 1.0;");
            deleteButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-text-fill: #ff5555; -fx-opacity: 1.0;");
        });
        row.setOnMouseExited(e -> {
            changeButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-opacity: 0.3;");
            deleteButton.setStyle("-fx-font-size: 8px; -fx-padding: 1px 4px; -fx-text-fill: #ff5555; -fx-opacity: 0.3;");
        });

        row.getChildren().addAll(indexLabel, elementNode, changeButton, deleteButton);
        return row;
    }

    private void showAddElementMenu(Button button, CompletionContext context, int insertIndex, String targetType) {
        ContextMenu menu = new ContextMenu();

        // Use getForType to filter the options
        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                context.codeEditor().addElementToList(this.astNode, type, insertIndex);
            });
            menu.getItems().add(menuItem);
        }

        // If no valid options, show a disabled message
        if (menu.getItems().isEmpty()) {
            MenuItem noOptions = new MenuItem("(Maximum nesting reached)");
            noOptions.setDisable(true);
            menu.getItems().add(noOptions);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showChangeElementMenu(Button button, CompletionContext context, int elementIndex, String targetType) {
        ContextMenu menu = new ContextMenu();

        // Use getForType to filter the options
        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                if (elementIndex < elements.size()) {
                    ExpressionBlock oldElement = elements.get(elementIndex);
                    context.codeEditor().replaceExpression(
                            (Expression) oldElement.getAstNode(),
                            type
                    );
                }
            });
            menu.getItems().add(menuItem);
        }
        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void deleteElement(int index, CompletionContext context) {
        if (index >= 0 && index < elements.size()) {
            context.codeEditor().deleteElementFromList(this.astNode, index);
        }
    }

    @Override
    public String getDetails() {
        return (isFixedArray ? "Array" : "List") + " (" + elements.size() + " items)";
    }
}