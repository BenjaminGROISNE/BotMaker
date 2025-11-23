package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.WhileStatement;
import java.util.ArrayList;
import java.util.List;

public class WhileBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock condition;
    private BodyBlock body;

    public WhileBlock(String id, WhileStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
    }

    public void setCondition(ExpressionBlock condition) { this.condition = condition; }
    public void setBody(BodyBlock body) { this.body = body; }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (condition != null) children.add(condition);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        // 1. Create the Header Row: "while [condition] [+]"
        Button addButton = createAddButton(e ->
                showExpressionMenuAndReplace((Button)e.getSource(), context, "boolean",
                        condition != null ? (org.eclipse.jdt.core.dom.Expression) condition.getAstNode() : null)
        );

        Node headerContent = createSentence(
                createKeywordLabel("while"),
                getOrDropZone(condition, context),
                addButton
        );
        headerContent.getStyleClass().add("while-header");

        // 2. Create the Indented Body
        VBox bodyNode = createIndentedBody(body, context, "while-body");

        // 3. Assemble: Header + Body
        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("while-block");

        // Use standard header wrapper for the top row (handles spacer & delete button)
        mainContainer.getChildren().add(createStandardHeader(context, headerContent));

        if (bodyNode != null) {
            mainContainer.getChildren().add(bodyNode);
        }

        return mainContainer;
    }
}