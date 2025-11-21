package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.eclipse.jdt.core.dom.LineComment;

/**
 * A comment block that exists only in the visual block structure.
 * When generating code, it's converted to an actual // comment.
 */
public class CommentBlock extends AbstractStatementBlock {

    private String commentText;

    public CommentBlock(String id, LineComment astNode, String commentText) {
        super(id, astNode);
        this.commentText = commentText;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("comment-block");

        // Comment indicator
        javafx.scene.control.Label commentLabel = new javafx.scene.control.Label("//");
        commentLabel.getStyleClass().add("comment-indicator");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

        // Editable text field for the comment
        TextField commentField = new TextField(commentText != null ? commentText : "");
        commentField.setPromptText("Enter comment...");
        commentField.getStyleClass().add("comment-text-field");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        // Update comment text on change
        commentField.textProperty().addListener((obs, oldVal, newVal) -> {
            this.commentText = newVal;
            // Trigger code regeneration
            rebuildCode(context);
        });

        // Spacer and delete button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode);
        });

        container.getChildren().addAll(commentLabel, commentField, spacer, deleteButton);
        return container;
    }

    /**
     * Trigger code regeneration when comment text changes
     */
    private void rebuildCode(CompletionContext context) {
        // Get the current code and trigger a refresh
        // This will cause the AST to be regenerated with the new comment
        javafx.application.Platform.runLater(() -> {
            // We need to trigger a code update through the proper channels
            // For now, we'll just mark that the comment changed
            // The actual regeneration will happen when needed
        });
    }

    @Override
    public String getDetails() {
        return "Comment: " + (commentText != null ? commentText : "");
    }
}