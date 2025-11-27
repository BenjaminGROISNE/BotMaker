package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import java.util.ArrayList;
import java.util.List;

public class ForBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock variable;
    private ExpressionBlock collection;
    private BodyBlock body;

    public ForBlock(String id, EnhancedForStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
    }

    public void setVariable(ExpressionBlock variable) { this.variable = variable; }
    public void setCollection(ExpressionBlock collection) { this.collection = collection; }
    public void setBody(BodyBlock body) { this.body = body; }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (variable != null) children.add(variable);
        if (collection != null) children.add(collection);
        if (body != null) children.add(body);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        // Header: "for each [var] in [collection]"
        Node headerContent = createSentence(
                createKeywordLabel("for each"),
                variable != null ? variable.getUINode(context) : createExpressionDropZone(context),
                createKeywordLabel("in"),
                collection != null ? collection.getUINode(context) : createExpressionDropZone(context)
        );
        headerContent.getStyleClass().add("for-header");

        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("for-block");
        mainContainer.getChildren().add(createStandardHeader(context, headerContent));

        // Body
        VBox bodyNode = createIndentedBody(body, context, "for-body");
        if (bodyNode != null) mainContainer.getChildren().add(bodyNode);

        return mainContainer;
    }
}