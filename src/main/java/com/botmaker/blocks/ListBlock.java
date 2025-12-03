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
    /**
     * Logic to determine what kind of items this specific ListBlock should contain.
     */
    private String determineItemType() {
        // The key insight: We need to know what type of ELEMENTS we contain
        // Not what type WE are

        ASTNode rootDefinition = null;

        // Start from our parent to count how deep we are
        ASTNode current = this.astNode.getParent();
        int currentDepth = 1; // We ARE at depth 1 (we are one array initializer)

        System.out.println("[Debug ListBlock.determineItemType] We are: " + this.astNode.getClass().getSimpleName());
        System.out.println("[Debug] Starting depth at 1 (we count as one level)");

        // Walk up counting additional nesting
        while (current != null) {
            System.out.println("[Debug] Checking parent: " + current.getClass().getSimpleName());

            // Count additional array nesting above us
            if (current instanceof ArrayInitializer) {
                currentDepth++;
                System.out.println("[Debug] Parent is ArrayInitializer, depth now: " + currentDepth);
            }
            else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                String methodName = mi.getName().getIdentifier();
                if ("asList".equals(methodName) || "of".equals(methodName)) {
                    currentDepth++;
                    System.out.println("[Debug] Parent is " + methodName + ", depth now: " + currentDepth);
                }
            }

            // Found declaration - stop here
            if (current instanceof VariableDeclarationFragment) {
                rootDefinition = current;
                System.out.println("[Debug] Found VariableDeclarationFragment");
                break;
            }
            if (current instanceof VariableDeclarationStatement) {
                rootDefinition = current;
                System.out.println("[Debug] Found VariableDeclarationStatement");
                break;
            }
            if (current instanceof FieldDeclaration) {
                rootDefinition = current;
                System.out.println("[Debug] Found FieldDeclaration");
                break;
            }
            if (current instanceof ArrayCreation) {
                rootDefinition = current;
                System.out.println("[Debug] Found ArrayCreation");
                break;
            }

            current = current.getParent();
        }

        if (rootDefinition == null) {
            System.out.println("[Debug] No root definition found, returning 'any'");
            return "any";
        }

        // Get the declared type
        ITypeBinding rootTypeBinding = null;
        String rootTypeStr = null;

        if (rootDefinition instanceof VariableDeclarationStatement) {
            Type type = ((VariableDeclarationStatement) rootDefinition).getType();
            rootTypeBinding = type.resolveBinding();
            rootTypeStr = type.toString();
        } else if (rootDefinition instanceof VariableDeclarationFragment) {
            ASTNode parent = rootDefinition.getParent();
            if (parent instanceof VariableDeclarationStatement) {
                Type type = ((VariableDeclarationStatement) parent).getType();
                rootTypeBinding = type.resolveBinding();
                rootTypeStr = type.toString();
            } else if (parent instanceof FieldDeclaration) {
                Type type = ((FieldDeclaration) parent).getType();
                rootTypeBinding = type.resolveBinding();
                rootTypeStr = type.toString();
            }
        } else if (rootDefinition instanceof FieldDeclaration) {
            Type type = ((FieldDeclaration) rootDefinition).getType();
            rootTypeBinding = type.resolveBinding();
            rootTypeStr = type.toString();
        } else if (rootDefinition instanceof ArrayCreation) {
            ArrayType type = ((ArrayCreation) rootDefinition).getType();
            rootTypeBinding = type.resolveBinding();
            rootTypeStr = type.toString();
        }

        if (rootTypeBinding == null) {
            System.out.println("[Debug] Could not resolve type binding, fallback to string: " + rootTypeStr);
            if (rootTypeStr != null) {
                return determineItemTypeFromString(rootTypeStr, currentDepth);
            }
            return "any";
        }

        int totalDimensions = TypeManager.getArrayDimensions(rootTypeBinding);
        ITypeBinding leafTypeBinding = TypeManager.getLeafTypeBinding(rootTypeBinding);
        String leafTypeName = leafTypeBinding != null ? leafTypeBinding.getName() : "Object";

        System.out.println("[Debug ListBlock SUMMARY]");
        System.out.println("  Declaration Type: " + rootTypeStr);
        System.out.println("  Total Dimensions: " + totalDimensions);
        System.out.println("  Current Depth:    " + currentDepth);
        System.out.println("  Leaf Type:        " + leafTypeName);

        // Key calculation:
        // If we have int[][][] and we're at depth 1, we contain int[][] elements
        // If we're at depth 2, we contain int[] elements
        // If we're at depth 3, we contain int elements

        int elementDimensions = totalDimensions - currentDepth;

        System.out.println("  Element Dims:     " + elementDimensions + " (total - depth)");

        if (elementDimensions > 0) {
            // Our children are arrays
            System.out.println("  -> Returning 'list' (children are arrays with " + elementDimensions + " dimensions)");
            return "list";
        } else if (elementDimensions == 0) {
            // Our children are leaf values
            String uiType = TypeManager.determineUiType(leafTypeBinding);
            System.out.println("  -> Returning '" + uiType + "' (children are leaf values)");
            return uiType;
        } else {
            // We're too deep!
            System.out.println("  -> ERROR: Negative element dimensions!");
            return "any";
        }
    }

    /**
     * Fallback method using string parsing when binding is unavailable
     */
    private String determineItemTypeFromString(String rootTypeStr, int currentDepth) {
        int totalDimensions = TypeManager.getListNestingLevel(rootTypeStr);
        String leafType = TypeManager.getLeafType(rootTypeStr);

        int remainingLevels = totalDimensions - currentDepth;

        System.out.println("[Debug ListBlock String Fallback] Type: " + rootTypeStr +
                " | TotalDims: " + totalDimensions +
                " | CurrentDepth: " + currentDepth +
                " | Remaining: " + remainingLevels);

        if (remainingLevels > 1) {
            return "list";
        } else if (remainingLevels == 1) {
            return TypeManager.determineUiType(leafType);
        } else {
            return "any";
        }
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