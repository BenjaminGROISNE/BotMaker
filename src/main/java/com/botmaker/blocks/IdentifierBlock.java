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

    public IdentifierBlock(String id, SimpleName astNode) {
        super(id, astNode);
        this.identifier = astNode.getIdentifier();
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        Text text = new Text(this.identifier);
        HBox container = new HBox(text);
        container.getStyleClass().add("identifier-block");

        // Add visual cues for interaction
        container.setCursor(Cursor.HAND);
        Tooltip tooltip = new Tooltip("Click for suggestions");
        Tooltip.install(container, tooltip);

        // Add the click handler for suggestions
        container.setOnMouseClicked(e -> requestSuggestions(container, context));
        return container;
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
                            mi.setOnAction(event -> applySuggestion(item, context));
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
