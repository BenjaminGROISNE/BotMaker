package com.botmaker.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LayoutComponents {

    private static final Insets STANDARD_INDENTATION = new Insets(5, 0, 0, 20);

    /**
     * Creates a VBox representing a nested body of code (e.g., inside an If or Loop).
     * Applies standard indentation and specific style classes.
     */
    public static VBox createIndentedBody(Node content, String... styleClasses) {
        VBox container = new VBox();
        if (styleClasses != null) {
            container.getStyleClass().addAll(styleClasses);
        }
        container.setPadding(STANDARD_INDENTATION);
        if (content != null) {
            container.getChildren().add(content);
        }
        return container;
    }

    /**
     * Creates a horizontal row for building "sentences" (e.g., "for each [var] in [list]").
     */
    public static HBox createSentenceRow(Node... nodes) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        if (nodes != null) {
            row.getChildren().addAll(nodes);
        }
        return row;
    }
}