package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
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

    public ExpressionBlock getExpression() {
        return expression;
    }

    public void setExpression(ExpressionBlock expression) {
        this.expression = expression;
    }

    public List<SwitchCaseBlock> getCases() {
        return cases;
    }

    public void addCase(SwitchCaseBlock caseBlock) {
        this.cases.add(caseBlock);
    }

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
        mainContainer.getStyleClass().add("switch-block");

        // Header row with delete button
        HBox headerRow = new HBox(5);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Switch expression part
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("switch-header");

        Label switchLabel = new Label("switch");
        switchLabel.getStyleClass().add("keyword-label");

        header.getChildren().add(switchLabel);
        if (expression != null) {
            header.getChildren().add(expression.getUINode(context));
        }

        // Add spacer and delete button
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("X");
        deleteButton.setOnAction(e -> {
            context.codeEditor().deleteStatement((Statement) this.astNode);
        });

        headerRow.getChildren().addAll(header, spacer, deleteButton);
        mainContainer.getChildren().add(headerRow);

        // Cases
        VBox casesContainer = new VBox(5);
        casesContainer.getStyleClass().add("switch-cases");
        casesContainer.setPadding(new Insets(5, 0, 0, 20));

        for (SwitchCaseBlock caseBlock : cases) {
            casesContainer.getChildren().add(caseBlock.getUINode(context));
        }

        mainContainer.getChildren().add(casesContainer);

        return mainContainer;
    }

    /**
     * Inner class representing a single case in the switch
     */
    public static class SwitchCaseBlock extends AbstractStatementBlock implements BlockWithChildren {
        private ExpressionBlock caseExpression; // null for default case
        private BodyBlock body;
        private final BlockDragAndDropManager dragAndDropManager;

        public SwitchCaseBlock(String id, SwitchCase astNode, BlockDragAndDropManager dragAndDropManager) {
            super(id, astNode);
            this.dragAndDropManager = dragAndDropManager;
        }

        public void setCaseExpression(ExpressionBlock caseExpression) {
            this.caseExpression = caseExpression;
        }

        public void setBody(BodyBlock body) {
            this.body = body;
        }

        public boolean isDefault() {
            return caseExpression == null;
        }

        @Override
        public List<CodeBlock> getChildren() {
            List<CodeBlock> children = new ArrayList<>();
            if (caseExpression != null) children.add(caseExpression);
            if (body != null) children.add(body);
            return children;
        }

        @Override
        protected Node createUINode(CompletionContext context) {
            VBox container = new VBox(5);
            container.getStyleClass().add("switch-case-block");

            // Case label
            HBox caseHeader = new HBox(5);
            caseHeader.setAlignment(Pos.CENTER_LEFT);

            if (isDefault()) {
                Label defaultLabel = new Label("default:");
                defaultLabel.getStyleClass().add("keyword-label");
                caseHeader.getChildren().add(defaultLabel);
            } else {
                Label caseLabel = new Label("case");
                caseLabel.getStyleClass().add("keyword-label");
                caseHeader.getChildren().add(caseLabel);
                if (caseExpression != null) {
                    caseHeader.getChildren().add(caseExpression.getUINode(context));
                }
                Label colon = new Label(":");
                caseHeader.getChildren().add(colon);
            }

            container.getChildren().add(caseHeader);

            // Case body
            if (body != null) {
                VBox bodyContainer = new VBox();
                bodyContainer.getStyleClass().add("switch-case-body");
                bodyContainer.setPadding(new Insets(5, 0, 0, 20));
                bodyContainer.getChildren().add(body.getUINode(context));
                container.getChildren().add(bodyContainer);
            }

            return container;
        }
    }
}