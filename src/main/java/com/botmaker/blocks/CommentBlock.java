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
import org.eclipse.jdt.core.dom.Comment; // Generic JDT Comment

public class CommentBlock extends AbstractStatementBlock {

    private String commentText;

    // Note: Accepts generic 'Comment' (covers LineComment and BlockComment)
    public CommentBlock(String id, Comment astNode, String commentText) {
        super(id, astNode);
        this.commentText = commentText;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("comment-block");

        // Visual indicator: // or /*
        boolean isLine = ((Comment)astNode).isLineComment();
        javafx.scene.control.Label commentLabel = new javafx.scene.control.Label(isLine ? "//" : "/*");
        commentLabel.getStyleClass().add("comment-indicator");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");

        // Text Field
        TextField commentField = new TextField(commentText != null ? commentText : "");
        commentField.setPromptText("Enter comment...");
        commentField.getStyleClass().add("comment-text-field");
        HBox.setHgrow(commentField, Priority.ALWAYS);

        // Save on Focus Lost
        commentField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                if (!commentField.getText().equals(commentText)) {
                    this.commentText = commentField.getText();
                    rebuildCode(context);
                }
            }
        });

        // Save on Enter
        commentField.setOnAction(e -> {
            this.commentText = commentField.getText();
            rebuildCode(context);
            // Remove focus from field to trigger visual update/prevent stickiness
            container.requestFocus();
        });

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Delete Button
        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteComment((Comment) this.astNode);
        });

        container.getChildren().addAll(commentLabel, commentField, spacer, deleteButton);
        return container;
    }

    private void rebuildCode(CompletionContext context) {
        javafx.application.Platform.runLater(() -> {
            context.codeEditor().updateComment((Comment) this.astNode, this.commentText);
        });
    }

    @Override
    public String getDetails() {
        return "Comment: " + (commentText != null ? commentText : "");
    }
}