// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\blocks\SwitchBlock.java
package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.util.TypeInfo;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
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
        VBox mainContainer = new VBox(5);

        TypeInfo switchType = TypeInfo.UNKNOWN;
        if (expression != null && expression.getAstNode() != null) {
            Expression expr = (Expression) expression.getAstNode();
            ITypeBinding binding = expr.resolveTypeBinding();
            if (binding != null) {
                switchType = TypeInfo.from(binding);
            }
        }

        TypeInfo finalSwitchType = switchType;
        Button changeSwitchExprBtn = BlockUIComponents.createChangeButton(e ->
                showExpressionMenuAndReplace((Button)e.getSource(), context, finalSwitchType,
                        expression != null ? (Expression) expression.getAstNode() : null)
        );

        var headerSentence = BlockLayout.sentence()
                .addKeyword("switch")
                .addExpressionSlot(expression, context, switchType)
                .addNode(changeSwitchExprBtn)
                .build();

        mainContainer.getChildren().add(BlockLayout.header()
                .withCustomNode(headerSentence)
                .withDeleteButton(() -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode))
                .build());

        VBox casesContainer = new VBox(5);
        casesContainer.setPadding(new javafx.geometry.Insets(5, 0, 0, 20));

        for (int i = 0; i < cases.size(); i++) {
            SwitchCaseBlock caseBlock = cases.get(i);
            casesContainer.getChildren().add(caseBlock.createUINode(context, i, cases.size()));
        }

        mainContainer.getChildren().add(casesContainer);

        Button addCaseButton = new Button("+ Add Case");
        addCaseButton.setOnAction(e -> context.codeEditor().addCaseToSwitch((SwitchStatement) this.astNode));
        mainContainer.getChildren().add(addCaseButton);

        return mainContainer;
    }

    public static class SwitchCaseBlock extends AbstractStatementBlock implements BlockWithChildren {
        private ExpressionBlock caseExpression;
        private BodyBlock body;

        public SwitchCaseBlock(String id, SwitchCase astNode) {
            super(id, astNode);
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
            return createUINode(context, -1, -1);
        }
        private TypeInfo getSwitchExpressionType() {
            // Get the parent SwitchStatement
            ASTNode parent = this.astNode.getParent();
            if (parent instanceof SwitchStatement) {
                SwitchStatement switchStmt = (SwitchStatement) parent;
                Expression switchExpr = switchStmt.getExpression();
                if (switchExpr != null) {
                    ITypeBinding binding = switchExpr.resolveTypeBinding();
                    if (binding != null) {
                        return TypeInfo.from(binding);
                    }
                }
            }
            return TypeInfo.UNKNOWN;
        }
        public Node createUINode(CompletionContext context, int index, int totalCases) {
            VBox container = new VBox(5);
            var caseHeaderBuilder = BlockLayout.sentence();

            if (isDefault()) {
                caseHeaderBuilder.addKeyword("default:");
            } else {
                TypeInfo switchType = getSwitchExpressionType();

                // FIX: Filter menu for constants only
                Button changeBtn = BlockUIComponents.createChangeButton(e -> {
                    ContextMenu menu = BlockUIComponents.createExpressionTypeMenu(
                            switchType,
                            true, // constantOnly = true
                            type -> context.codeEditor().replaceExpression(
                                    (Expression) caseExpression.getAstNode(), type)
                    );
                    menu.show((Button)e.getSource(), javafx.geometry.Side.BOTTOM, 0, 0);
                });

                caseHeaderBuilder
                        .addKeyword("case")
                        .addExpressionSlot(caseExpression, context, switchType)
                        .addNode(changeBtn)
                        .addKeyword(":");
            }

            if (index >= 0) {
                Button upBtn = new Button("▲");
                upBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
                upBtn.setDisable(index == 0);
                upBtn.setOnAction(e -> context.codeEditor().moveSwitchCase((SwitchCase) this.astNode, true));

                Button downBtn = new Button("▼");
                downBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
                downBtn.setDisable(index == totalCases - 1);
                downBtn.setOnAction(e -> context.codeEditor().moveSwitchCase((SwitchCase) this.astNode, false));

                caseHeaderBuilder
                        .addNode(BlockUIComponents.createSpacer())
                        .addNode(upBtn)
                        .addNode(downBtn);
            }

            HBox caseHeader = caseHeaderBuilder.build();
            caseHeader.getChildren().add(createDeleteButton(context));

            container.getChildren().add(caseHeader);

            VBox bodyNode = createIndentedBody(body, context, "switch-case-body");
            if (bodyNode != null) container.getChildren().add(bodyNode);

            return container;
        }
    }
}