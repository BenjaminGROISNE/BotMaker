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

    public static ContextMenu createExpressionTypeMenu(String targetType, Consumer<AddableExpression> onSelect) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-control-inner-background: white;");

        for (AddableExpression type : AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setStyle("-fx-text-fill: black;");
            menuItem.setOnAction(e -> onSelect.accept(type));
            menu.getItems().add(menuItem);
        }
        return menu;
    }
}