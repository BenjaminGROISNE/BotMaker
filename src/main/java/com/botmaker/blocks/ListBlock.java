package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.TypeInfo;
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
import java.util.List;

/**
 * Visual block for lists.
 * Supports:
 * 1. Array Initializers: {1, 2, 3}
 * 2. List Factories: Arrays.asList(1, 2, 3) or List.of(1, 2, 3)
 *
 * UPDATED: Uses TypeInfo for ALL type operations - this eliminates the multi-dimensional array bugs!
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

        // UPDATED: Use TypeInfo for determining item type
        TypeInfo itemType = determineItemType();

        String typeLabel = isFixedArray ? "Array" : "List";

        // Debug label (optional - can be enabled for verification):
        // Label listLabel = new Label(typeLabel + "<" + itemType.getDisplayName() + "> (" + elements.size() + ")");
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
                HBox elementRow = createElementRow(i, elements.get(i), context, itemType);
                elementsContainer.getChildren().add(elementRow);
            }
            container.getChildren().add(elementsContainer);
        }

        return container;
    }

    private HBox createElementRow(int index, ExpressionBlock element, CompletionContext context, TypeInfo itemType) {
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

    private void showAddElementMenu(Button button, CompletionContext context, int insertIndex, TypeInfo targetType) {
        ContextMenu menu = new ContextMenu();

        // UPDATED: Pass TypeInfo to getForType
        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                context.codeEditor().addElementToList(this.astNode, type, insertIndex);
            });
            menu.getItems().add(menuItem);
        }

        if (menu.getItems().isEmpty()) {
            MenuItem noOptions = new MenuItem("(Maximum nesting reached)");
            noOptions.setDisable(true);
            menu.getItems().add(noOptions);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    /**
     * COMPLETELY REWRITTEN: Determine what kind of items this ListBlock should contain.
     * This now uses TypeInfo exclusively and should fix ALL multi-dimensional array issues!
     *
     * Logic:
     * 1. Walk up parent chain to find root declaration (VariableDeclarationStatement, FieldDeclaration, etc.)
     * 2. Count how deep WE are in the nesting (currentDepth)
     * 3. Get the declared type's total dimensions using TypeInfo
     * 4. Calculate: elementDimensions = totalDimensions - currentDepth
     * 5. If elementDimensions > 0: return array TypeInfo
     *    If elementDimensions == 0: return leaf TypeInfo
     */
    private TypeInfo determineItemType() {
        System.out.println("\n[ListBlock.determineItemType] Starting for: " + this.astNode.getClass().getSimpleName());

        // Step 1: Walk up and find root declaration
        ASTNode rootDefinition = null;
        ASTNode current = this.astNode.getParent();
        int currentDepth = 1; // We count as one level of nesting

        System.out.println("  Starting depth at 1 (we are one array initializer)");

        while (current != null) {
            System.out.println("  Checking parent: " + current.getClass().getSimpleName());

            // Count additional array nesting above us
            if (current instanceof ArrayInitializer) {
                currentDepth++;
                System.out.println("    -> Parent is ArrayInitializer, depth now: " + currentDepth);
            }
            else if (current instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) current;
                String methodName = mi.getName().getIdentifier();
                if ("asList".equals(methodName) || "of".equals(methodName)) {
                    currentDepth++;
                    System.out.println("    -> Parent is " + methodName + ", depth now: " + currentDepth);
                }
            }

            // Found declaration - stop here
            if (current instanceof VariableDeclarationFragment ||
                    current instanceof VariableDeclarationStatement ||
                    current instanceof FieldDeclaration ||
                    current instanceof ArrayCreation) {
                rootDefinition = current;
                System.out.println("  Found root declaration: " + current.getClass().getSimpleName());
                break;
            }

            current = current.getParent();
        }

        if (rootDefinition == null) {
            System.out.println("  ERROR: No root definition found!");
            return TypeInfo.UNKNOWN;
        }

        // Step 2: Get the declared type
        TypeInfo declaredType = extractDeclaredType(rootDefinition);

        if (declaredType == null) {
            System.out.println("  ERROR: Could not extract declared type!");
            return TypeInfo.UNKNOWN;
        }

        System.out.println("  Declared Type: " + declaredType.getTypeName());
        System.out.println("  Total Dimensions: " + declaredType.getArrayDimensions());
        System.out.println("  Current Depth: " + currentDepth);
        System.out.println("  Leaf Type: " + declaredType.getLeafType().getTypeName());

        // Step 3: Calculate what OUR elements should be
        int totalDimensions = declaredType.getArrayDimensions();
        int elementDimensions = totalDimensions - currentDepth;

        System.out.println("  Element Dimensions: " + elementDimensions + " (total - depth)");

        if (elementDimensions > 0) {
            // Our children are arrays
            TypeInfo elementType = declaredType.getLeafType().asArray(elementDimensions);
            System.out.println("  -> Returning ARRAY: " + elementType.getTypeName());
            return elementType;
        } else if (elementDimensions == 0) {
            // Our children are leaf values
            TypeInfo leafType = declaredType.getLeafType();
            System.out.println("  -> Returning LEAF: " + leafType.getTypeName());
            return leafType;
        } else {
            // We're too deep! This shouldn't happen if declarations are correct
            System.out.println("  -> ERROR: Negative element dimensions!");
            return TypeInfo.UNKNOWN;
        }
    }

    /**
     * Helper: Extract TypeInfo from various AST node types
     */
    private TypeInfo extractDeclaredType(ASTNode node) {
        if (node instanceof VariableDeclarationStatement) {
            Type type = ((VariableDeclarationStatement) node).getType();
            return TypeInfo.from(type);
        }

        if (node instanceof VariableDeclarationFragment) {
            ASTNode parent = node.getParent();
            if (parent instanceof VariableDeclarationStatement) {
                Type type = ((VariableDeclarationStatement) parent).getType();
                return TypeInfo.from(type);
            } else if (parent instanceof FieldDeclaration) {
                Type type = ((FieldDeclaration) parent).getType();
                return TypeInfo.from(type);
            }
        }

        if (node instanceof FieldDeclaration) {
            Type type = ((FieldDeclaration) node).getType();
            return TypeInfo.from(type);
        }

        if (node instanceof ArrayCreation) {
            ArrayType type = ((ArrayCreation) node).getType();
            return TypeInfo.from(type);
        }

        return null;
    }

    private void showChangeElementMenu(Button button, CompletionContext context, int elementIndex, TypeInfo targetType) {
        ContextMenu menu = new ContextMenu();

        // UPDATED: Pass TypeInfo to getForType
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