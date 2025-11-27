package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.util.TypeManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.*;

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
        // Header
        // FIX: Use UI_TYPE_SWITCH_COMPATIBLE to filter allowed types for the Switch expression
        Button changeSwitchExprBtn = BlockUIComponents.createChangeButton(e ->
                showExpressionMenuAndReplace((Button)e.getSource(), context, TypeManager.UI_TYPE_SWITCH_COMPATIBLE,
                        expression != null ? (Expression) expression.getAstNode() : null)
        );

        Node headerContent = createSentence(
                createKeywordLabel("switch"),
                getOrDropZone(expression, context),
                changeSwitchExprBtn
        );
        headerContent.getStyleClass().add("switch-header");

        // Main container
        VBox mainContainer = new VBox(5);
        mainContainer.getStyleClass().add("switch-block");
        mainContainer.getChildren().add(createStandardHeader(context, headerContent));

        // Cases Container
        VBox casesContainer = new VBox(5);
        casesContainer.getStyleClass().add("switch-cases");
        casesContainer.setPadding(new Insets(5, 0, 0, 20));

        for (int i = 0; i < cases.size(); i++) {
            SwitchCaseBlock caseBlock = cases.get(i);
            // Pass index context to control button visibility
            casesContainer.getChildren().add(caseBlock.createUINode(context, i, cases.size()));
        }

        mainContainer.getChildren().add(casesContainer);

        // Add Case Button at the bottom
        Button addCaseButton = new Button("+ Add Case");
        addCaseButton.getStyleClass().add("add-case-button");
        addCaseButton.setOnAction(e -> context.codeEditor().addCaseToSwitch((SwitchStatement) this.astNode));

        mainContainer.getChildren().add(addCaseButton);

        return mainContainer;
    }

    // Inner Static Class for Cases
    public static class SwitchCaseBlock extends AbstractStatementBlock implements BlockWithChildren {
        private ExpressionBlock caseExpression;
        private BodyBlock body;
        private final BlockDragAndDropManager dragAndDropManager;

        public SwitchCaseBlock(String id, SwitchCase astNode, BlockDragAndDropManager dragAndDropManager) {
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
            // Use the overloaded version below
            return createUINode(context, -1, -1);
        }

        // Special render method that knows position
        public Node createUINode(CompletionContext context, int index, int totalCases) {
            HBox caseHeader = new HBox(5);

            // Case Label & Expression
            if (isDefault()) {
                caseHeader.getChildren().add(createKeywordLabel("default:"));
            } else {
                // 1. Determine Expected Type from Parent Switch
                String targetType = "any";
                if (this.astNode.getParent() instanceof SwitchStatement) {
                    SwitchStatement parent = (SwitchStatement) this.astNode.getParent();
                    Expression switchExpr = parent.getExpression();
                    if (switchExpr != null) {
                        ITypeBinding binding = switchExpr.resolveTypeBinding();
                        if (binding != null) {
                            targetType = TypeManager.determineUiType(binding.getName());
                        }
                    }
                }

                // 2. Create Change Button
                String finalTargetType = targetType;
                Button changeBtn = BlockUIComponents.createChangeButton(e ->
                        showExpressionMenuAndReplace((Button)e.getSource(), context, finalTargetType,
                                caseExpression != null ? (Expression) caseExpression.getAstNode() : null)
                );

                caseHeader.getChildren().addAll(
                        createKeywordLabel("case"),
                        getOrDropZone(caseExpression, context),
                        changeBtn,
                        createKeywordLabel(":")
                );
            }

            // Move Buttons
            if (index >= 0) {
                Button upBtn = new Button("▲");
                upBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
                upBtn.setDisable(index == 0);
                upBtn.setOnAction(e -> context.codeEditor().moveSwitchCase((SwitchCase) this.astNode, true));

                Button downBtn = new Button("▼");
                downBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
                downBtn.setDisable(index == totalCases - 1);
                downBtn.setOnAction(e -> context.codeEditor().moveSwitchCase((SwitchCase) this.astNode, false));

                caseHeader.getChildren().addAll(BlockUIComponents.createSpacer(), upBtn, downBtn);
            }

            // Delete Button
            caseHeader.getChildren().add(createDeleteButton(context));

            VBox container = new VBox(5);
            container.getStyleClass().add("switch-case-block");
            container.getChildren().add(caseHeader);

            VBox bodyNode = createIndentedBody(body, context, "switch-case-body");
            if (bodyNode != null) container.getChildren().add(bodyNode);

            return container;
        }
    }
}