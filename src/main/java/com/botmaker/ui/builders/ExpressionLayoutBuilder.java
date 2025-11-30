package com.botmaker.ui.builders;

import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.builders.DropZoneFactory;
import com.botmaker.ui.theme.BlockTheme;
import com.botmaker.ui.theme.StyleBuilder;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating expression block UIs.
 * Handles literals, identifiers, operators, method calls, and complex expressions.
 */
public class ExpressionLayoutBuilder {

    // Common properties
    private final List<Node> components = new ArrayList<>();
    private double spacing = 5.0;
    private Pos alignment = Pos.CENTER_LEFT;
    private CompletionContext context;
    private final List<String> styleClasses = new ArrayList<>();

    // Interaction
    private Runnable onClickHandler;

    // Expression-specific properties
    private ExpressionType type;

    public enum ExpressionType {
        LITERAL,
        IDENTIFIER,
        OPERATOR,
        METHOD_CALL,
        FIELD_ACCESS,
        ARRAY_ACCESS,
        LIST,
        ENUM_CONSTANT,
        PARENTHESIZED
    }

    public ExpressionLayoutBuilder() {
        styleClasses.add("expression-block");
    }

    // ===== TYPE-SPECIFIC FACTORY METHODS =====

    public static ExpressionLayoutBuilder literal() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.LITERAL;
        builder.styleClasses.add("literal-block");
        return builder;
    }

    public static ExpressionLayoutBuilder identifier() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.IDENTIFIER;
        builder.styleClasses.add("identifier-block");
        return builder;
    }

    public static ExpressionLayoutBuilder operator() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.OPERATOR;
        builder.styleClasses.add("operator-block");
        return builder;
    }

    public static ExpressionLayoutBuilder methodCall() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.METHOD_CALL;
        builder.styleClasses.add("method-call-block");
        return builder;
    }

    public static ExpressionLayoutBuilder fieldAccess() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.FIELD_ACCESS;
        builder.styleClasses.add("field-access-block");
        return builder;
    }

    public static ExpressionLayoutBuilder list() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.LIST;
        builder.styleClasses.add("list-block");
        return builder;
    }

    public static ExpressionLayoutBuilder enumConstant() {
        ExpressionLayoutBuilder builder = new ExpressionLayoutBuilder();
        builder.type = ExpressionType.ENUM_CONSTANT;
        builder.styleClasses.add("enum-constant-block");
        return builder;
    }

    // ===== COMPONENT ADDITION METHODS =====

    public ExpressionLayoutBuilder withContext(CompletionContext context) {
        this.context = context;
        return this;
    }

    public ExpressionLayoutBuilder addLabel(String text) {
        components.add(new Label(text));
        return this;
    }

    public ExpressionLayoutBuilder addStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        components.add(label);
        return this;
    }

    public ExpressionLayoutBuilder addKeyword(String text) {
        Label label = new Label(text);
        StyleBuilder.create().asKeyword().applyTo(label);
        components.add(label);
        return this;
    }

    public ExpressionLayoutBuilder addNode(Node node) {
        components.add(node);
        return this;
    }

    public ExpressionLayoutBuilder addExpression(ExpressionBlock expr) {
        if (expr != null && context != null) {
            components.add(expr.getUINode(context));
        } else if (context != null) {
            components.add(createDropZone(context));
        }
        return this;
    }

    public ExpressionLayoutBuilder addSeparator(String text) {
        Label separator = new Label(text);
        StyleBuilder.create()
                .textColor(BlockTheme.current().colors().operator())
                .fontWeight(BlockTheme.current().typography().boldWeight())
                .applyTo(separator);
        components.add(separator);
        return this;
    }

    // ===== LITERAL-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withTextField(javafx.scene.control.TextField field) {
        components.add(field);
        return this;
    }

    public ExpressionLayoutBuilder withBooleanToggle(javafx.scene.control.ComboBox<String> toggle) {
        components.add(toggle);
        return this;
    }

    // ===== IDENTIFIER-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withIdentifierText(String identifier, boolean isUnedited) {
        javafx.scene.text.Text text = new javafx.scene.text.Text(identifier);
        HBox container = new HBox(text);
        container.setAlignment(Pos.CENTER_LEFT);

        if (isUnedited) {
            container.getStyleClass().add("unedited-identifier");
        }

        components.add(container);
        return this;
    }

    public ExpressionLayoutBuilder withClickHandler(Runnable onClick) {
        this.onClickHandler = onClick;
        return this;
    }

    // ===== FIELD ACCESS-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withQualifier(String qualifier) {
        javafx.scene.text.Text qualifierText = new javafx.scene.text.Text(qualifier + ".");
        StyleBuilder.create()
                .textColor("#8E44AD")
                .fontWeight(BlockTheme.current().typography().boldWeight())
                .applyTo(qualifierText);
        components.add(qualifierText);
        return this;
    }

    public ExpressionLayoutBuilder withFieldName(String fieldName) {
        javafx.scene.text.Text fieldText = new javafx.scene.text.Text(fieldName);
        StyleBuilder.create()
                .textColor("#2C3E50")
                .applyTo(fieldText);
        components.add(fieldText);
        return this;
    }

    // ===== METHOD CALL-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withMethodName(String methodName) {
        Label nameLabel = new Label(methodName);
        StyleBuilder.create()
                .fontWeight(BlockTheme.current().typography().boldWeight())
                .fontSize(BlockTheme.current().typography().normal())
                .applyTo(nameLabel);
        components.add(nameLabel);
        return this;
    }

    public ExpressionLayoutBuilder withArgumentList(List<Node> arguments) {
        components.add(new Label("("));

        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                components.add(new Label(", "));
            }
            components.add(arguments.get(i));
        }

        components.add(new Label(")"));
        return this;
    }

    public ExpressionLayoutBuilder withArguments(ArgumentsBuilder argsBuilder) {
        components.add(argsBuilder.build());
        return this;
    }

    // ===== OPERATOR-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withLeftOperand(ExpressionBlock operand) {
        if (operand != null && context != null) {
            components.add(operand.getUINode(context));
        }
        return this;
    }

    public ExpressionLayoutBuilder withOperatorSelector(
            javafx.scene.control.ComboBox<String> selector) {
        components.add(selector);
        return this;
    }

    public ExpressionLayoutBuilder withOperatorLabel(String operator) {
        Label opLabel = new Label(operator);
        StyleBuilder.create().asOperator().applyTo(opLabel);
        components.add(opLabel);
        return this;
    }

    public ExpressionLayoutBuilder withRightOperand(ExpressionBlock operand) {
        if (operand != null && context != null) {
            components.add(operand.getUINode(context));
        }
        return this;
    }

    public ExpressionLayoutBuilder withTypeIndicator(String typeName) {
        Label typeLabel = new Label("→ " + typeName);
        StyleBuilder.create()
                .textColor("#999")
                .fontSize(BlockTheme.current().typography().small())
                .build();
        components.add(typeLabel);
        return this;
    }

    // ===== LIST-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withListHeader(String listType, int elementCount) {
        Label header = new Label(listType + " (" + elementCount + ")");
        header.getStyleClass().add("list-label");
        components.add(header);
        return this;
    }

    public ExpressionLayoutBuilder withListElements(javafx.scene.layout.VBox elementsContainer) {
        components.add(elementsContainer);
        return this;
    }

    // ===== ENUM CONSTANT-SPECIFIC METHODS =====

    public ExpressionLayoutBuilder withEnumType(String enumTypeName) {
        Label typeLabel = new Label(enumTypeName);
        StyleBuilder.create()
                .textColor("white")
                .fontWeight(BlockTheme.current().typography().boldWeight())
                .fontSize(BlockTheme.current().typography().normal())
                .applyTo(typeLabel);
        components.add(typeLabel);
        return this;
    }

    public ExpressionLayoutBuilder withEnumDot() {
        Label dot = new Label(".");
        StyleBuilder.create()
                .textColor("rgba(255,255,255,0.7)")
                .fontWeight(BlockTheme.current().typography().boldWeight())
                .applyTo(dot);
        components.add(dot);
        return this;
    }

    public ExpressionLayoutBuilder withEnumConstantSelector(
            javafx.scene.control.ComboBox<String> selector) {
        components.add(selector);
        return this;
    }

    // ===== STYLING METHODS =====

    public ExpressionLayoutBuilder spacing(double spacing) {
        this.spacing = spacing;
        return this;
    }

    public ExpressionLayoutBuilder alignment(Pos alignment) {
        this.alignment = alignment;
        return this;
    }

    public ExpressionLayoutBuilder withStyleClass(String... classes) {
        styleClasses.addAll(List.of(classes));
        return this;
    }

    public ExpressionLayoutBuilder withInlineStyle(String style) {
        // Store for application during build
        return this;
    }

    public ExpressionLayoutBuilder asCompact() {
        this.spacing = 3.0;
        return this;
    }

    public ExpressionLayoutBuilder asSpaced() {
        this.spacing = 8.0;
        return this;
    }

    // ===== BUILD METHOD =====

    public HBox build() {
        HBox container = new HBox(spacing);
        container.setAlignment(alignment);
        styleClasses.forEach(c -> container.getStyleClass().add(c));
        container.getChildren().addAll(components);

        // FIX: Applied click handler
        if (onClickHandler != null) {
            container.setCursor(Cursor.HAND);
            container.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    onClickHandler.run();
                    e.consume();
                }
            });
        }

        return container;
    }

    // ===== HELPER METHODS =====

    private Node createDropZone(CompletionContext context) {
        return DropZoneFactory.createExpressionDropZone(context);
    }

    // ===== NESTED BUILDER FOR ARGUMENTS =====

    public static class ArgumentsBuilder {
        private final List<ArgumentNode> arguments = new ArrayList<>();

        public static class ArgumentNode {
            String parameterName;
            Node valueNode;

            public ArgumentNode(String parameterName, Node valueNode) {
                this.parameterName = parameterName;
                this.valueNode = valueNode;
            }
        }

        public ArgumentsBuilder addArgument(String paramName, Node value) {
            arguments.add(new ArgumentNode(paramName, value));
            return this;
        }

        public ArgumentsBuilder addArgument(Node value) {
            arguments.add(new ArgumentNode(null, value));
            return this;
        }

        public HBox build() {
            HBox container = new HBox(5);
            container.setAlignment(Pos.CENTER_LEFT);

            container.getChildren().add(new Label("("));

            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    Label comma = new Label(", ");
                    container.getChildren().add(comma);
                }

                ArgumentNode arg = arguments.get(i);

                // Create argument container
                HBox argBox = new HBox(2);
                argBox.setAlignment(Pos.CENTER_LEFT);
                StyleBuilder.create()
                        .backgroundColor("rgba(255,255,255,0.1)")
                        .backgroundRadius(4)
                        .padding(2)
                        .applyTo(argBox);

                // Add parameter name if provided
                if (arg.parameterName != null) {
                    Label paramLabel = new Label(arg.parameterName + ":");
                    StyleBuilder.create()
                            .textColor("#aaa")
                            .fontSize(BlockTheme.current().typography().tiny())
                            .padding(0, 4, 0, 2)
                            .applyTo(paramLabel);
                    argBox.getChildren().add(paramLabel);
                }

                argBox.getChildren().add(arg.valueNode);
                container.getChildren().add(argBox);
            }

            container.getChildren().add(new Label(")"));

            return container;
        }
    }
}