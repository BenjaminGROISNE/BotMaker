package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

public class IfBlock extends AbstractStatementBlock {

    private ExpressionBlock condition;
    private BodyBlock thenBody;
    private BodyBlock elseBody; // Can be null

    public IfBlock(String id, IfStatement astNode) {
        super(id, astNode);
    }

    public ExpressionBlock getCondition() {
        return condition;
    }

    public void setCondition(ExpressionBlock condition) {
        this.condition = condition;
    }

    public BodyBlock getThenBody() {
        return thenBody;
    }

    public void setThenBody(BodyBlock thenBody) {
        this.thenBody = thenBody;
    }

    public BodyBlock getElseBody() {
        return elseBody;
    }

    public void setElseBody(BodyBlock elseBody) {
        this.elseBody = elseBody;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: #fffbe6; -fx-border-color: #ffe58f; -fx-padding: 5; -fx-background-radius: 5; -fx-border-radius: 5;");

        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(new Label("If"));
        if (condition != null) {
            header.getChildren().add(condition.getUINode(context));
        }
        container.getChildren().add(header);

        if (thenBody != null) {
            HBox thenContainer = new HBox(5);
            thenContainer.setAlignment(Pos.TOP_LEFT);
            thenContainer.getChildren().add(new Label("Then:"));
            thenContainer.getChildren().add(thenBody.getUINode(context));
            container.getChildren().add(thenContainer);
        }

        if (elseBody != null) {
            HBox elseContainer = new HBox(5);
            elseContainer.setAlignment(Pos.TOP_LEFT);
            elseContainer.getChildren().add(new Label("Else:"));
            elseContainer.getChildren().add(elseBody.getUINode(context));
            container.getChildren().add(elseContainer);
        }

        return container;
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
        // The breakpoint for an IfBlock should be on its condition.
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
