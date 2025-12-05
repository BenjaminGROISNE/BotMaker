package com.botmaker.ui.components;

import com.botmaker.ui.AddableExpression;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

public class BlockUIComponents {

    public static Button createDeleteButton(Runnable onDelete) {
        Button btn = new Button("X");
        btn.setOnAction(e -> onDelete.run());
        // Optional: Add a specific style class if you want to target it specifically later
        // btn.getStyleClass().add("delete-button");
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

    /**
     * Creates a standard header row: [Content Nodes] + [Spacer] + [Delete Button]
     */
    public static HBox createHeaderRow(Runnable onDelete, Node... content) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);

        if (content != null) {
            container.getChildren().addAll(content);
        }

        container.getChildren().addAll(createSpacer(), createDeleteButton(onDelete));
        return container;
    }

    /**
     * TypeInfo overload for createExpressionTypeMenu
     */
    public static javafx.scene.control.ContextMenu createExpressionTypeMenu(
            com.botmaker.util.TypeInfo expectedType,
            java.util.function.Consumer<com.botmaker.ui.AddableExpression> onSelect) {

        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();

        // Get filtered expression types based on TypeInfo
        java.util.List<com.botmaker.ui.AddableExpression> availableTypes =
                com.botmaker.ui.AddableExpression.getForType(expectedType);

        for (com.botmaker.ui.AddableExpression exprType : availableTypes) {
            javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(exprType.getDisplayName());
            item.setOnAction(e -> onSelect.accept(exprType));
            menu.getItems().add(item);
        }

        return menu;
    }

}