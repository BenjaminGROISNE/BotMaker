package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.TextFieldComponents;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Comment;

public class CommentBlock extends AbstractStatementBlock {

    private String commentText;

    public CommentBlock(String id, Comment astNode, String commentText) {
        super(id, astNode);
        this.commentText = commentText;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("comment-block");

        // Label
        Label commentLabel = new Label("Comment:");
        commentLabel.getStyleClass().add("comment-indicator");

        // TextField using component factory
        TextField commentField = TextFieldComponents.createCommentField(
                commentText,
                "Write your note here...",
                newText -> {
                    if (!newText.equals(commentText)) {
                        this.commentText = newText;
                        javafx.application.Platform.runLater(() -> {
                            context.codeEditor().updateComment((Comment) this.astNode, this.commentText);
                        });
                        container.requestFocus();
                    }
                }
        );

        container.getChildren().addAll(
                commentLabel,
                commentField,
                BlockUIComponents.createSpacer(),
                createDeleteButton(context)
        );

        return container;
    }

    @Override
    public String getDetails() {
        return "Comment: " + (commentText != null ? commentText : "");
    }
}