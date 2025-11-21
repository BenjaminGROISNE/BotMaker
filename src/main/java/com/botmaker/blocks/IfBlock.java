package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

public class IfBlock extends AbstractStatementBlock {

    private ExpressionBlock condition;
    private BodyBlock thenBody;
    private com.botmaker.core.StatementBlock elseStatement;

    public IfBlock(String id, IfStatement astNode) {
        super(id, astNode);
    }

    public ExpressionBlock getCondition() { return condition; }
    public void setCondition(ExpressionBlock condition) { this.condition = condition; }
    public BodyBlock getThenBody() { return thenBody; }
    public void setThenBody(BodyBlock thenBody) { this.thenBody = thenBody; }
    public com.botmaker.core.StatementBlock getElseStatement() { return elseStatement; }
    public void setElseStatement(com.botmaker.core.StatementBlock elseStatement) { this.elseStatement = elseStatement; }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.getStyleClass().add("if-block");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);

        Label ifLabel = new Label("If");
        ifLabel.getStyleClass().add("keyword-label");
        header.getChildren().add(ifLabel);

        if (condition != null) {
            header.getChildren().add(condition.getUINode(context));
        } else {
            header.getChildren().add(createExpressionDropZone(context));
        }

        Button addButton = new Button("+");
        addButton.getStyleClass().add("expression-add-button");

        // UPDATED: Pass "boolean" as target type
        addButton.setOnAction(e -> showExpressionMenu(addButton, context, "boolean"));

        header.getChildren().add(addButton);

        javafx.scene.layout.Pane spacer = new javafx.scene.layout.Pane();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("X");
        deleteButton.setOnAction(e -> context.codeEditor().deleteStatement((org.eclipse.jdt.core.dom.Statement) this.astNode));

        header.getChildren().addAll(spacer, deleteButton);
        container.getChildren().add(header);

        if (thenBody != null) {
            Node thenBodyNode = thenBody.getUINode(context);
            thenBodyNode.getStyleClass().add("if-body");
            HBox.setHgrow(thenBodyNode, javafx.scene.layout.Priority.ALWAYS);
            container.getChildren().add(thenBodyNode);
        }

        if (elseStatement != null) {
            if (elseStatement instanceof com.botmaker.core.BodyBlock) {
                VBox elseContainer = new VBox(5);
                HBox elseHeader = new HBox(5);
                elseHeader.setAlignment(Pos.CENTER_LEFT);
                Label elseLabel = new Label("Else");
                elseLabel.getStyleClass().add("keyword-label");
                javafx.scene.control.Button addElseIfButton = new javafx.scene.control.Button("+ if");
                addElseIfButton.setOnAction(e -> context.codeEditor().convertElseToElseIf((IfStatement) this.astNode));
                javafx.scene.layout.Pane elseSpacer = new javafx.scene.layout.Pane();
                HBox.setHgrow(elseSpacer, javafx.scene.layout.Priority.ALWAYS);
                javafx.scene.control.Button deleteElseButton = new javafx.scene.control.Button("X");
                deleteElseButton.setOnAction(e -> context.codeEditor().deleteElseFromIfStatement((IfStatement) this.astNode));
                elseHeader.getChildren().addAll(elseLabel, addElseIfButton, elseSpacer, deleteElseButton);

                Node elseBodyNode = elseStatement.getUINode(context);
                elseBodyNode.getStyleClass().add("if-body");
                HBox.setHgrow(elseBodyNode, javafx.scene.layout.Priority.ALWAYS);
                elseContainer.getChildren().addAll(elseHeader, elseBodyNode);
                container.getChildren().add(elseContainer);
            } else {
                HBox elseIfContainer = new HBox(5);
                elseIfContainer.setAlignment(Pos.CENTER_LEFT);
                Label elseLabel = new Label("Else");
                elseLabel.getStyleClass().add("keyword-label");
                elseIfContainer.getChildren().add(elseLabel);
                Node elseNode = elseStatement.getUINode(context);
                HBox.setHgrow(elseNode, javafx.scene.layout.Priority.ALWAYS);
                elseIfContainer.getChildren().add(elseNode);
                container.getChildren().add(elseIfContainer);
            }
        } else {
            javafx.scene.control.Button addElseButton = new javafx.scene.control.Button("+");
            addElseButton.setOnAction(e -> context.codeEditor().addElseToIfStatement((IfStatement) this.astNode));
            container.getChildren().add(addElseButton);
        }

        return container;
    }

    // UPDATED: Accepts targetType
    private void showExpressionMenu(Button button, CompletionContext context, String targetType) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-control-inner-background: white;");

        for (com.botmaker.ui.AddableExpression type : com.botmaker.ui.AddableExpression.getForType(targetType)) {
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            menuItem.setStyle("-fx-text-fill: black;");

            menuItem.setOnAction(e -> {
                if (condition != null) {
                    org.eclipse.jdt.core.dom.Expression toReplace = (org.eclipse.jdt.core.dom.Expression) condition.getAstNode();
                    context.codeEditor().replaceExpression(toReplace, type);
                }
            });
            menu.getItems().add(menuItem);
        }

        menu.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @Override
    public void highlight() {
        if (condition != null) {
            condition.highlight();
        } else {
            super.highlight();
        }
    }

    @Override
    public void unhighlight() {
        if (condition != null) {
            condition.unhighlight();
        } else {
            super.unhighlight();
        }
    }

    @Override
    public int getBreakpointLine(CompilationUnit cu) {
        if (condition != null) {
            return condition.getBreakpointLine(cu);
        }
        return super.getBreakpointLine(cu);
    }

    @Override
    public CodeBlock getHighlightTarget() {
        return condition != null ? condition : this;
    }

    @Override
    public String getDetails() {
        String conditionDetails = (condition != null) ? condition.getDetails() : "no condition";
        return "If Statement (condition: " + conditionDetails + ")";
    }
}