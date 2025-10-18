package com.botmaker.blocks;

import com.botmaker.core.AbstractCodeBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class MainBlock extends AbstractCodeBlock implements BlockWithChildren {

    private BodyBlock mainBody;

    public MainBlock(String id, ASTNode astNode) {
        super(id, astNode);
    }

    public void setMainBody(BodyBlock mainBody) {
        this.mainBody = mainBody;
    }

    @Override
    public List<CodeBlock> getChildren() {
        List<CodeBlock> children = new ArrayList<>();
        if (mainBody != null) {
            children.add(mainBody);
        }
        return children;
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(5);
        container.getStyleClass().add("main-block");
        container.setPadding(new Insets(10));

        Label header = new Label("public static void main(String[] args)");
        header.getStyleClass().add("main-block-header");
        container.getChildren().add(header);

        if (mainBody != null) {
            container.getChildren().add(mainBody.getUINode(context));
        }

        return container;
    }
}
