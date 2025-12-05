package com.botmaker.ui.components;

import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeInfo;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockUIComponents {

    public static Button createDeleteButton(Runnable onDelete) {
        Button btn = new Button("X");
        btn.setOnAction(e -> onDelete.run());
        return btn;
    }

    public static Button createAddButton(EventHandler<ActionEvent> handler) {
        Button btn = new Button("+");
        btn.getStyleClass().add("expression-add-button");
        btn.setOnAction(handler);
        return btn;
    }

    public static Button createChangeButton(EventHandler<ActionEvent> handler) {
        Button btn = new Button("↻");
        btn.getStyleClass().add("icon-button");
        btn.setOnAction(handler);
        return btn;
    }

    public static Label createKeywordLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("keyword-label");
        return label;
    }

    public static Label createTypeLabel(String type) {
        Label label = new Label(type);
        label.getStyleClass().add("type-label");
        return label;
    }

    public static Label createOperatorLabel(String operator) {
        Label label = new Label(operator);
        label.getStyleClass().add("operator-label");
        return label;
    }

    public static Pane createSpacer() {
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static HBox createHeaderRow(Runnable onDelete, Node... content) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        if (content != null) container.getChildren().addAll(content);
        container.getChildren().addAll(createSpacer(), createDeleteButton(onDelete));
        return container;
    }

    /**
     * Standard menu creation
     */
    public static ContextMenu createExpressionTypeMenu(
            TypeInfo expectedType,
            Consumer<AddableExpression> onSelect) {
        return createExpressionTypeMenu(expectedType, false, onSelect);
    }

    /**
     * Categorized menu creation with constant filtering
     */
    public static ContextMenu createExpressionTypeMenu(
            TypeInfo expectedType,
            boolean constantOnly,
            Consumer<AddableExpression> onSelect) {

        ContextMenu menu = new ContextMenu();

        // 1. Get filtered list
        List<AddableExpression> available = AddableExpression.getForType(expectedType, constantOnly);

        // 2. Group by Category
        Map<AddableExpression.Category, List<AddableExpression>> grouped = available.stream()
                .collect(Collectors.groupingBy(AddableExpression::getCategory));

        // 3. Define Category Order
        AddableExpression.Category[] order = {
                AddableExpression.Category.LITERAL,
                AddableExpression.Category.REFERENCE,
                AddableExpression.Category.MATH,
                AddableExpression.Category.COMPARISON,
                AddableExpression.Category.LOGIC,
                AddableExpression.Category.STRUCTURE
        };

        boolean hasItems = false;

        for (AddableExpression.Category cat : order) {
            List<AddableExpression> items = grouped.get(cat);
            if (items == null || items.isEmpty()) continue;

            // Literals and References go to root (separated), others to sub-menus
            if (cat == AddableExpression.Category.LITERAL || cat == AddableExpression.Category.REFERENCE) {
                if (!menu.getItems().isEmpty()) menu.getItems().add(new SeparatorMenuItem());
                for (AddableExpression expr : items) {
                    menu.getItems().add(createItem(expr, onSelect));
                }
            } else {
                Menu subMenu = new Menu(cat.getLabel());
                for (AddableExpression expr : items) {
                    subMenu.getItems().add(createItem(expr, onSelect));
                }
                menu.getItems().add(subMenu);
            }
            hasItems = true;
        }

        if (!hasItems) {
            MenuItem empty = new MenuItem("(No options available)");
            empty.setDisable(true);
            menu.getItems().add(empty);
        }

        return menu;
    }

    private static MenuItem createItem(AddableExpression expr, Consumer<AddableExpression> onSelect) {
        MenuItem item = new MenuItem(expr.getDisplayName());
        item.setOnAction(e -> onSelect.accept(expr));
        return item;
    }
}