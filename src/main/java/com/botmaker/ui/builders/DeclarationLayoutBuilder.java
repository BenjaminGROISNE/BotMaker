package com.botmaker.ui.builders;

import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.TextFieldComponents;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

public class DeclarationLayoutBuilder {
    private String type;
    private String variableName;
    private ExpressionBlock initializer;
    private CompletionContext context;
    private Runnable onDelete;
    private Runnable onTypeChange;
    private Runnable onInitializerChange;
    private Consumer<String> onNameChange;

    public DeclarationLayoutBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public DeclarationLayoutBuilder withVariableName(
            String name,
            Consumer<String> onNameChange) {
        this.variableName = name;
        this.onNameChange = onNameChange;
        return this;
    }

    public DeclarationLayoutBuilder withInitializer(
            ExpressionBlock initializer,
            CompletionContext context) {
        this.initializer = initializer;
        this.context = context;
        return this;
    }

    public DeclarationLayoutBuilder withTypeChangeHandler(Runnable onChange) {
        this.onTypeChange = onChange;
        return this;
    }

    public DeclarationLayoutBuilder withInitializerChangeHandler(Runnable onChange) {
        this.onInitializerChange = onChange;
        return this;
    }

    public DeclarationLayoutBuilder withDeleteButton(Runnable onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public HBox build() {
        SentenceLayoutBuilder sentence = BlockLayout.sentence();

        // Type label (clickable if change handler provided)
        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("type-label");
        if (onTypeChange != null) {
            typeLabel.setCursor(Cursor.HAND);
            typeLabel.setOnMouseClicked(e -> onTypeChange.run());
        }
        sentence.addNode(typeLabel);

        // Variable name field
        TextField nameField = TextFieldComponents.createVariableNameField(
                variableName,
                onNameChange != null ? onNameChange : (s) -> {}
        );
        sentence.addNode(nameField);

        // Assignment operator
        sentence.addKeyword("=");

        // Initializer
        sentence.addExpressionSlot(initializer, context, "any");

        // Change button
        if (onInitializerChange != null) {
            sentence.addNode(createAddButton(onInitializerChange));
        }

        HBox result = sentence.build();

        // Add delete button to the end
        if (onDelete != null) {
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            result.getChildren().addAll(spacer, createDeleteButton(onDelete));
        }

        return result;
    }

    private Button createAddButton(Runnable onClick) {
        Button btn = new Button("+");
        btn.getStyleClass().add("expression-add-button");
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    private Button createDeleteButton(Runnable onDelete) {
        Button btn = new Button("X");
        btn.setOnAction(e -> onDelete.run());
        return btn;
    }
}