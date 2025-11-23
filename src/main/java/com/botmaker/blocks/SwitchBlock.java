package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.SwitchStatement;

import java.util.ArrayList;
import java.util.List;

public class SwitchBlock extends AbstractStatementBlock implements BlockWithChildren {

    private ExpressionBlock expression;
    private final List<SwitchCaseBlock> cases = new ArrayList<>();
    private final BlockDragAndDropManager dragAndDropManager;

    public SwitchBlock(String id, SwitchStatement astNode, BlockDragAndDropManager dragAndDropManager) {
        super(id, astNode);
        this.dragAndDropManager = dragAndDropManager;
    }

    public void setExpression(ExpressionBlock expression) { this.expression = expression; }
    public void addCase(SwitchCaseBlock caseBlock) { this.cases.add(caseBlock); }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (expression != null) children.add(expression);
        children.addAll(cases);
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        Node headerContent = createSentence(
                createKeywordLabel("switch"),
                getOrDropZone(expression, context)
        );
        headerContent.getStyleClass().add("switch-header");

        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("switch-block");
        mainContainer.getChildren().add(createStandardHeader(context, headerContent));

        VBox casesContainer = new VBox(5);
        casesContainer.getStyleClass().add("switch-cases");
        casesContainer.setPadding(new Insets(5, 0, 0, 20));

        for (SwitchCaseBlock caseBlock : cases) {
            casesContainer.getChildren().add(caseBlock.getUINode(context));
        }

        mainContainer.getChildren().add(casesContainer);
        return mainContainer;
    }

    // Inner Static Class for Cases
    public static class SwitchCaseBlock extends AbstractStatementBlock implements BlockWithChildren {
        private ExpressionBlock caseExpression;
        private BodyBlock body;
        private final BlockDragAndDropManager dragAndDropManager;

        public SwitchCaseBlock(String id, org.eclipse.jdt.core.dom.SwitchCase astNode, BlockDragAndDropManager dragAndDropManager) {
            super(id, astNode);
            this.dragAndDropManager = dragAndDropManager;
        }

        public void setCaseExpression(ExpressionBlock caseExpression) { this.caseExpression = caseExpression; }
        public void setBody(BodyBlock body) { this.body = body; }
        public boolean isDefault() { return caseExpression == null; }

        @Override
        public List<CodeBlock> getChildren() {
            List<CodeBlock> children = new ArrayList<>();
            if (caseExpression != null) children.add(caseExpression);
            if (body != null) children.add(body);
            return children;
        }

        @Override
        protected Node createUINode(CompletionContext context) {
            Node caseHeader;
            if (isDefault()) {
                caseHeader = createSentence(createKeywordLabel("default:"));
            } else {
                caseHeader = createSentence(
                        createKeywordLabel("case"),
                        getOrDropZone(caseExpression, context),
                        createKeywordLabel(":")
                );
            }

            VBox container = new VBox(5);
            container.getStyleClass().add("switch-case-block");
            container.getChildren().add(caseHeader);

            VBox bodyNode = createIndentedBody(body, context, "switch-case-body");
            if (bodyNode != null) container.getChildren().add(bodyNode);

            return container;
        }
    }
}