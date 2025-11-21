package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.validation.TypeValidator;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.lsp4j.*;

import java.util.List;
import java.util.stream.Collectors;

public class IdentifierBlock extends AbstractExpressionBlock {
    private final String identifier;
    private boolean isUnedited = false;
    private static final String UNEDITED_STYLE_CLASS = "unedited-identifier";

    public IdentifierBlock(String id, SimpleName astNode) {
        this(id, astNode, false);
    }

    public IdentifierBlock(String id, SimpleName astNode, boolean markAsUnedited) {
        super(id, astNode);
        this.identifier = astNode.getIdentifier();
        this.isUnedited = markAsUnedited;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isUnedited() {
        return isUnedited;
    }

    public void markAsEdited() {
        this.isUnedited = false;
        if (uiNode != null) {
            uiNode.getStyleClass().remove(UNEDITED_STYLE_CLASS);
        }
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        Text text = new Text(this.identifier);
        HBox container = new HBox(text);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.getStyleClass().add("identifier-block");

        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

        container.setCursor(Cursor.HAND);

        String tooltipText = isUnedited
                ? "⚠️ Default variable name - Click to choose a variable"
                : "Click to see available variables";

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(container, tooltip);

        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                requestSuggestions(container, context);
            }
        });

        if (isUnedited) {
            Platform.runLater(() -> autoPopulateWithSuggestion(container, context));
        }

        return container;
    }

    private void autoPopulateWithSuggestion(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                // Filter for user-friendly variables only
                CompletionItem firstVariable = items.stream()
                        .filter(item -> item.getKind() == CompletionItemKind.Variable)
                        .filter(item -> TypeValidator.isUserVariable(item.getLabel()))
                        .findFirst()
                        .orElse(null);

                if (firstVariable != null) {
                    Platform.runLater(() -> {
                        applySuggestion(firstVariable, context);
                        Tooltip newTooltip = new Tooltip("✓ Auto-selected variable - Click to change");
                        Tooltip.install(uiNode, newTooltip);
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestSuggestions(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) ||
                        (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return;
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();

                    // Filter for user-friendly variables only
                    List<CompletionItem> userVariables = items.stream()
                            .filter(item -> item.getKind() == CompletionItemKind.Variable)
                            .filter(item -> TypeValidator.isUserVariable(item.getLabel()))
                            .collect(Collectors.toList());

                    if (userVariables.isEmpty()) {
                        MenuItem noVars = new MenuItem("(No variables available yet)");
                        noVars.setDisable(true);
                        menu.getItems().add(noVars);
                    } else {
                        for (CompletionItem item : userVariables) {
                            MenuItem mi = new MenuItem(item.getLabel());

                            // Add type information if available
                            if (item.getDetail() != null && !item.getDetail().isEmpty()) {
                                mi.setText(item.getLabel() + " (" + getSimpleTypeName(item.getDetail()) + ")");
                            }

                            mi.setOnAction(event -> {
                                applySuggestion(item, context);
                                markAsEdited();
                            });
                            menu.getItems().add(mi);
                        }
                    }

                    if (!menu.getItems().isEmpty()) {
                        menu.show(uiNode, javafx.geometry.Side.BOTTOM, 0, 0);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simplify type names for user-friendliness
     */
    private String getSimpleTypeName(String detail) {
        if (detail.contains("int")) return "number";
        if (detail.contains("double") || detail.contains("float")) return "decimal";
        if (detail.contains("boolean")) return "true/false";
        if (detail.contains("String")) return "text";
        if (detail.contains("[]")) return "list";
        return detail.replaceAll(".*\\.", ""); // Remove package names
    }

    private void applySuggestion(CompletionItem item, CompletionContext context) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            context.codeEditor().replaceSimpleName((SimpleName) this.astNode, insertText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Position getPositionFromOffset(String code, int offset) {
        int line = 0;
        int lastNewline = -1;
        for (int i = 0; i < offset; i++) {
            if (code.charAt(i) == '\n') {
                line++;
                lastNewline = i;
            }
        }
        int character = offset - lastNewline - 1;
        return new Position(line, character);
    }
}