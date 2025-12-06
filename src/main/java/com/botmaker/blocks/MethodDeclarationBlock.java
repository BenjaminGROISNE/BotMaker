package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.builders.BlockLayout;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.Collections;
import java.util.List;

public class MethodDeclarationBlock extends AbstractStatementBlock implements BlockWithChildren {

    private final String methodName;
    private final String returnType;
    private BodyBlock body;

    // Step 3 Features (Preserved)
    protected boolean isDeletable = true;

    // Step 5 Feature: State for minimization
    private boolean isCollapsed = false;

    public MethodDeclarationBlock(String id, MethodDeclaration astNode, BlockDragAndDropManager manager) {
        super(id, astNode);
        this.methodName = astNode.getName().getIdentifier();
        if (astNode.getReturnType2() != null) {
            this.returnType = astNode.getReturnType2().toString();
        } else {
            this.returnType = "void";
        }
    }

    public void setBody(BodyBlock body) {
        this.body = body;
    }

    @Override
    public List<CodeBlock> getChildren() {
        return body != null ? Collections.singletonList(body) : Collections.emptyList();
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        VBox container = new VBox(0);

        // --- HEADER SECTION ---
        VBox headerBox = new VBox(5);
        headerBox.setStyle(
                "-fx-background-color: #8E44AD; " +
                        "-fx-background-radius: 8 8 0 0; " +
                        "-fx-padding: 8 10 8 10;"
        );

        // 1. Create the Body Wrapper (so we can add/remove it dynamically)
        VBox bodyWrapper = new VBox();
        bodyWrapper.setStyle("-fx-border-color: #8E44AD; -fx-border-width: 0 0 0 4; -fx-background-color: rgba(142, 68, 173, 0.05);");

        if (body != null) {
            Node bodyNode = body.getUINode(context);
            VBox.setVgrow(bodyNode, javafx.scene.layout.Priority.ALWAYS);
            bodyWrapper.getChildren().add(bodyNode);
        }

        // 2. Collapse Toggle Button
        Button collapseBtn = new Button(isCollapsed ? "▶" : "▼");
        collapseBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand;");
        collapseBtn.setMinWidth(25);

        collapseBtn.setOnAction(e -> {
            this.isCollapsed = !this.isCollapsed;
            collapseBtn.setText(isCollapsed ? "▶" : "▼");

            if (isCollapsed) {
                // Remove body to minimize
                container.getChildren().remove(bodyWrapper);
                // Adjust header radius to look like a closed pill
                headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8; -fx-padding: 8 10 8 10;");
            } else {
                // Add body back
                container.getChildren().add(bodyWrapper);
                // Restore header radius (flat bottom)
                headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8 8 0 0; -fx-padding: 8 10 8 10;");
            }
        });

        // 3. Method Definition UI (Step 3 Features included)
        Label funcLabel = new Label("Function");
        funcLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-weight: bold; -fx-font-size: 10px;");

        // Rename Field
        Node nameNode;
        if (isDeletable) {
            TextField nameField = new TextField(methodName);
            nameField.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 2 5 2 5;");
            nameField.setPrefWidth(Math.max(80, methodName.length() * 8 + 20));

            nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Lost focus
                    String newName = nameField.getText().trim();
                    if (!newName.isEmpty() && !newName.equals(methodName) && !"main".equals(newName)) {
                        context.codeEditor().renameMethod((MethodDeclaration) this.astNode, newName);
                    } else {
                        nameField.setText(methodName);
                    }
                }
            });
            nameNode = nameField;
        } else {
            Label nameLabel = new Label(methodName);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameNode = nameLabel;
        }

        Label returnsLabel = new Label("returns");
        returnsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-style: italic; -fx-font-size: 11px;");

        ComboBox<String> typeSelector = new ComboBox<>();
        typeSelector.getItems().add("void");
        typeSelector.getItems().addAll(TypeManager.getFundamentalTypeNames());
        typeSelector.setValue(returnType);
        typeSelector.setStyle("-fx-font-size: 11px; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 4;");
        typeSelector.setOnAction(e -> {
            String selected = typeSelector.getValue();
            if (!selected.equals(returnType)) {
                context.codeEditor().setMethodReturnType((MethodDeclaration) this.astNode, selected);
            }
        });

        // Build Top Row
        var topRowBuilder = BlockLayout.sentence()
                .addNode(collapseBtn)
                .addNode(funcLabel)
                .addNode(nameNode)
                .addNode(BlockUIComponents.createSpacer())
                .addNode(returnsLabel)
                .addNode(typeSelector);

        if (isDeletable) {
            Button deleteBtn = new Button("×");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> context.codeEditor().deleteMethod((MethodDeclaration) this.astNode));
            topRowBuilder.addNode(deleteBtn);
        }

        HBox topRow = topRowBuilder.build();

        // 4. Parameters Row (Step 3 Feature: Delete params)
        Label paramsLabel = new Label("Inputs:");
        paramsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

        var paramRowBuilder = BlockLayout.sentence()
                .addNode(paramsLabel);

        MethodDeclaration md = (MethodDeclaration) this.astNode;
        List<?> params = md.parameters();

        for (int i = 0; i < params.size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(i);
            paramRowBuilder.addNode(createParamNode(param, i, context));
        }

        MenuButton addParamBtn = new MenuButton("+");
        addParamBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 10;");

        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                String defaultName = com.botmaker.util.DefaultNames.forType(type);
                context.codeEditor().addParameterToMethod((MethodDeclaration) this.astNode, type, defaultName);
            });
            addParamBtn.getItems().add(item);
        }
        paramRowBuilder.addNode(addParamBtn);

        HBox paramRow = paramRowBuilder.build();

        // Assemble Header
        headerBox.getChildren().addAll(topRow, paramRow);
        container.getChildren().add(headerBox);

        // Assemble Initial Body State
        if (!isCollapsed) {
            container.getChildren().add(bodyWrapper);
        } else {
            // Apply collapsed style immediately
            headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8; -fx-padding: 8 10 8 10;");
        }

        return container;
    }

    private Node createParamNode(SingleVariableDeclaration param, int index, CompletionContext context) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 12; -fx-padding: 3 8 3 8;");

        Label typeLabel = new Label(param.getType().toString());
        typeLabel.setStyle("-fx-text-fill: #8E44AD; -fx-font-weight: bold; -fx-font-size: 11px;");

        String currentName = param.getName().getIdentifier();
        TextField nameField = new TextField(currentName);

        nameField.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-font-size: 11px; -fx-text-fill: #333;");
        nameField.setPrefWidth(Math.max(30, currentName.length() * 7));

        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                nameField.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-font-size: 11px; -fx-text-fill: black;");
            } else {
                nameField.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-font-size: 11px; -fx-text-fill: #333;");
                String val = nameField.getText().trim();
                if (!val.isEmpty() && !val.equals(currentName)) {
                    context.codeEditor().renameMethodParameter((MethodDeclaration) this.astNode, index, val);
                } else {
                    nameField.setText(currentName);
                }
            }
        });

        box.getChildren().addAll(typeLabel, nameField);

        // Step 3 Feature: Delete Parameter Button
        if (isDeletable) {
            Button delBtn = new Button("×");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 10px; -fx-padding: 0 0 0 2; -fx-cursor: hand;");
            delBtn.setOnAction(e -> context.codeEditor().deleteParameterFromMethod((MethodDeclaration) this.astNode, index));
            box.getChildren().add(delBtn);
        }

        return box;
    }
}