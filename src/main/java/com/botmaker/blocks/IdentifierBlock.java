package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
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

public class IdentifierBlock extends AbstractExpressionBlock {
    private final String identifier;
    private boolean isUnedited = false; // Track if this is a default/auto-populated identifier
    private static final String UNEDITED_STYLE_CLASS = "unedited-identifier";

    public IdentifierBlock(String id, SimpleName astNode) {
        this(id, astNode, false);
    }

    /**
     * Constructor for creating new IdentifierBlocks
     * @param markAsUnedited true if this identifier was auto-generated and should prompt user to edit
     */
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

        // Add unedited styling if applicable
        if (isUnedited) {
            container.getStyleClass().add(UNEDITED_STYLE_CLASS);
        }

        // Add visual cues for interaction
        container.setCursor(Cursor.HAND);

        String tooltipText = isUnedited
                ? "⚠️ Default variable name - Click to choose a variable or edit it"
                : "Click for variable suggestions";

        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(container, tooltip);

        // Add the click handler for suggestions
        container.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                requestSuggestions(container, context);
            }
        });

        // If this is unedited, automatically request suggestions once to auto-populate
        if (isUnedited) {
            Platform.runLater(() -> autoPopulateWithSuggestion(container, context));
        }

        return container;
    }

    /**
     * Automatically populate with the first available variable suggestion
     */
    private void autoPopulateWithSuggestion(Node uiNode, CompletionContext context) {
        try {
            Position pos = getPositionFromOffset(context.sourceCode(), this.astNode.getStartPosition());
            CompletionParams params = new CompletionParams(new TextDocumentIdentifier(context.docUri()), pos);

            context.server().getTextDocumentService().completion(params).thenAccept(result -> {
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) || (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return; // No suggestions found, keep default value
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                // Find the first variable suggestion
                CompletionItem firstVariable = items.stream()
                        .filter(item -> item.getKind() == CompletionItemKind.Variable)
                        .findFirst()
                        .orElse(null);

                if (firstVariable != null) {
                    Platform.runLater(() -> {
                        // Auto-apply the first suggestion
                        applySuggestion(firstVariable, context);

                        // Update tooltip to reflect the auto-population
                        Tooltip newTooltip = new Tooltip("✓ Auto-selected variable - Click to change or confirm");
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
                if (result == null || (result.isLeft() && result.getLeft().isEmpty()) || (result.isRight() && result.getRight().getItems().isEmpty())) {
                    return; // No suggestions found
                }

                List<CompletionItem> items = result.isLeft() ? result.getLeft() : result.getRight().getItems();

                Platform.runLater(() -> {
                    ContextMenu menu = new ContextMenu();
                    for (CompletionItem item : items) {
                        // Filter for variables, as requested by the user
                        if (item.getKind() == CompletionItemKind.Variable) {
                            MenuItem mi = new MenuItem(item.getLabel());
                            mi.setOnAction(event -> {
                                applySuggestion(item, context);
                                markAsEdited(); // Mark as edited when user explicitly selects
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
            e.printStackTrace(); // Log error
        }
    }

    private void applySuggestion(CompletionItem item, CompletionContext context) {
        try {
            String insertText = item.getInsertText() != null ? item.getInsertText() : item.getLabel();
            // The astNode for an IdentifierBlock is a SimpleName.
            context.codeEditor().replaceSimpleName((SimpleName) this.astNode, insertText);
        } catch (Exception e) {
            e.printStackTrace(); // Log error
        }
    }

    // Helper to convert a string offset to a line/character position
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