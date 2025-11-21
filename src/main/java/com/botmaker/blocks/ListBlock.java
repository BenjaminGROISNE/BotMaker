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
        for (Object expr : astNode.expressions()) {
            // The BlockFactory handles the recursive creation of children,
            // we just hold the references.
        }
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

        // Add padding and a slight background diff for nested lists
        // (You should add .nested-list to your CSS, or we use inline style for now)
        if (this.astNode.getParent() instanceof ArrayInitializer) {
            container.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1;");
            container.setPadding(new Insets(4, 8, 4, 8));
        } else {
            container.setPadding(new Insets(8, 12, 8, 12));
        }

        // Header row: "List [...]" label + Add button
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Determine depth/context for label
        String labelText = (this.astNode.getParent() instanceof ArrayInitializer) ? "Sub-List" : "List";
        Label listLabel = new Label(labelText + " [" + elements.size() + "]");
        listLabel.getStyleClass().add("list-label");

        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");
        // Compact the button slightly for nested views
        addButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        addButton.setOnAction(e -> showAddElementMenu(addButton, context, elements.size()));

        headerRow.getChildren().addAll(listLabel, addButton);
        container.getChildren().add(headerRow);

        // Display each element with controls
        if (elements.isEmpty()) {
            Label emptyLabel = new Label("(empty)");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 10px;");
            container.getChildren().add(emptyLabel);
        } else {
            VBox elementsContainer = new VBox(2); // Tighter spacing
            elementsContainer.setPadding(new Insets(2, 0, 0, 10)); // Indent content

            for (int i = 0; i < elements.size(); i++) {
                HBox elementRow = createElementRow(i, elements.get(i), context);
                elementsContainer.getChildren().add(elementRow);
            }

            container.getChildren().add(elementsContainer);
        }

        return container;
    }

    private HBox createElementRow(int index, ExpressionBlock element, CompletionContext context) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);

        // Index label
        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle("-fx-font-family: monospace; -fx-text-fill: #888; -fx-min-width: 15px; -fx-font-size: 10px;");

        // Element display
        Node elementNode = element.getUINode(context);

        // Logic to handle nested UI resizing
        if (element instanceof ListBlock) {
            // Allow nested list to expand
            HBox.setHgrow(elementNode, javafx.scene.layout.Priority.ALWAYS);
        }

        // Change button
        Button changeButton = new Button("↻");
        changeButton.getStyleClass().add("icon-button");
        changeButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-opacity: 0.6;");
        changeButton.setOnAction(e -> showChangeElementMenu(changeButton, context, index));

        // Delete button
        Button deleteButton = new Button("✕");
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-text-fill: #E74C3C; -fx-opacity: 0.6;");
        deleteButton.setOnAction(e -> deleteElement(index, context));

        // Hover effects for buttons
        row.setOnMouseEntered(e -> {
            changeButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-opacity: 1.0;");
            deleteButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-text-fill: #E74C3C; -fx-opacity: 1.0;");
        });
        row.setOnMouseExited(e -> {
            changeButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-opacity: 0.6;");
            deleteButton.setStyle("-fx-font-size: 9px; -fx-padding: 1px 4px; -fx-text-fill: #E74C3C; -fx-opacity: 0.6;");
        });

        row.getChildren().addAll(indexLabel, elementNode, changeButton, deleteButton);
        return row;
    }

    // ... (Rest of methods: showAddElementMenu, showChangeElementMenu, deleteElement, getDetails remain the same)
    // Ensure AddableExpression.values() now includes LIST, so the menu automatically picks it up.

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
        return "List (" + elements.size() + ")";
    }
}