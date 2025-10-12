package com.botmaker.blocks;

import com.botmaker.core.StatementBlock;
import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.lsp.CompletionContext;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.eclipse.jdt.core.dom.Block;

import java.util.ArrayList;
import java.util.List;

public class BodyBlock extends AbstractStatementBlock {
    private final List<StatementBlock> statements = new ArrayList<>();

    public BodyBlock(String id, Block astNode) {
        super(id, astNode);
    }

    public List<StatementBlock> getStatements() {
        return statements;
    }

    public void addStatement(StatementBlock statement) {
        this.statements.add(statement);
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        ListView<StatementBlock> listView = new ListView<>();
        listView.setItems(FXCollections.observableList(statements));

        // Tell the ListView how to render each statement
        listView.setCellFactory(param -> new ListCell<StatementBlock>() {
            @Override
            protected void updateItem(StatementBlock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // For each visible item, get its UI node, passing the context down.
                    setGraphic(item.getUINode(context));
                }
            }
        });

        // Basic styling to show the body container
        listView.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-style: dashed; -fx-background-insets: 0; -fx-padding: 1;");
        return listView;
    }
}
