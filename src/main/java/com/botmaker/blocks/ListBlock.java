package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.ArrayInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual block for array/list literals like {1, 2, 3}
 * Allows users to add, remove, and edit elements inline
 */
public class ListBlock extends AbstractExpressionBlock {

    private final List<ExpressionBlock> elements = new ArrayList<>();

    public ListBlock(String id, ArrayInitializer astNode) {
        super(id, astNode);
    }

    public List<ExpressionBlock> getElements() {
        return elements;
    }

    public void addElement(ExpressionBlock element) {
        this.elements.add(element);
    }

    public void clearElements() {
        this.elements.clear();
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("list-block");
        container.setPadding(new Insets(8, 12, 8, 12));

        // Header row: "List [...]" label + Add button
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label listLabel = new Label("List [" + elements.size() + " items]");
        listLabel.getStyleClass().add("list-label");

        Button addButton = new Button("+ Add Item");
        addButton.getStyleClass().add("expression-add-button");
        addButton.setOnAction(e -> showAddElementMenu(addButton, context, elements.size()));

        headerRow.getChildren().addAll(listLabel, addButton);
        container.getChildren().add(headerRow);

        // Display each element with controls
        if (elements.isEmpty()) {
            Label emptyLabel = new Label("(empty list)");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: rgba(255,255,255,0.6);");
            container.getChildren().add(emptyLabel);
        } else {
            VBox elementsContainer = new VBox(5);
            elementsContainer.setPadding(new Insets(5, 0, 0, 10));

            for (int i = 0; i < elements.size(); i++) {
                HBox elementRow = createElementRow(i, elements.get(i), context);
                elementsContainer.getChildren().add(elementRow);
            }

            container.getChildren().add(elementsContainer);
        }

        return container;
    }

    /**
     * Creates a row for a single list element with controls
     */
    private HBox createElementRow(int index, ExpressionBlock element, CompletionContext context) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        // Index label
        Label indexLabel = new Label("[" + index + "]");
        indexLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 30px;");

        // Element display
        Node elementNode = element.getUINode(context);

        // Change button (replace with different expression)
        Button changeButton = new Button("↻");
        changeButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        changeButton.setOnAction(e -> showChangeElementMenu(changeButton, context, index));

        // Delete button
        Button deleteButton = new Button("✕");
        deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px; -fx-text-fill: #E74C3C;");
        deleteButton.setOnAction(e -> deleteElement(index, context));

        // Insert before button (optional, for reordering)
        Button insertButton = new Button("+ Insert");
        insertButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        insertButton.setOnAction(e -> showAddElementMenu(insertButton, context, index));

        row.getChildren().addAll(indexLabel, elementNode, changeButton, insertButton, deleteButton);
        return row;
    }

    /**
     * Shows menu to add a new element at the specified index
     */
    private void showAddElementMenu(Button button, CompletionContext context, int insertIndex) {
        ContextMenu menu = new ContextMenu();

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.values()) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                context.codeEditor().addElementToArrayInitializer(
                        (ArrayInitializer) this.astNode,
                        type,
                        insertIndex
                );
            });
            menu.getItems().add(menuItem);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    /**
     * Shows menu to change an existing element
     */
    private void showChangeElementMenu(Button button, CompletionContext context, int elementIndex) {
        ContextMenu menu = new ContextMenu();

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.values()) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setOnAction(e -> {
                if (elementIndex < elements.size()) {
                    ExpressionBlock oldElement = elements.get(elementIndex);
                    context.codeEditor().replaceExpression(
                            (org.eclipse.jdt.core.dom.Expression) oldElement.getAstNode(),
                            type
                    );
                }
            });
            menu.getItems().add(menuItem);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    /**
     * Deletes an element from the list
     */
    private void deleteElement(int index, CompletionContext context) {
        if (index >= 0 && index < elements.size()) {
            context.codeEditor().deleteElementFromArrayInitializer(
                    (ArrayInitializer) this.astNode,
                    index
            );
        }
    }

    @Override
    public String getDetails() {
        return "List with " + elements.size() + " elements";
    }
}