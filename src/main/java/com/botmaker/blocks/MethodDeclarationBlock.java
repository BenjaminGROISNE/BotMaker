// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\blocks\MethodDeclarationBlock.java
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
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.Collections;
import java.util.List;

public class MethodDeclarationBlock extends AbstractStatementBlock implements BlockWithChildren {

    private final String methodName;
    private final String returnType;
    private BodyBlock body;

    protected boolean isDeletable = true; // False for Main method
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

        // --- STATE SYNC ---
        String parentName = "";
        if (this.astNode.getParent() instanceof AbstractTypeDeclaration) {
            parentName = ((AbstractTypeDeclaration) this.astNode.getParent()).getName().getIdentifier();
        }
        String methodKey = parentName + "." + methodName;

        // Restore state from ApplicationState
        this.isCollapsed = context.applicationState().isMethodCollapsed(methodKey);

        // --- HEADER SECTION ---
        VBox headerBox = new VBox(5);
        if (isCollapsed) {
            headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8; -fx-padding: 8 10 8 10;");
        } else {
            headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8 8 0 0; -fx-padding: 8 10 8 10;");
        }

        // 1. Create the Body Wrapper
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
            context.applicationState().setMethodCollapsed(methodKey, this.isCollapsed);

            if (isCollapsed) {
                container.getChildren().remove(bodyWrapper);
                headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8; -fx-padding: 8 10 8 10;");
            } else {
                container.getChildren().add(bodyWrapper);
                headerBox.setStyle("-fx-background-color: #8E44AD; -fx-background-radius: 8 8 0 0; -fx-padding: 8 10 8 10;");
            }
        });

        // 3. Top Row (Name & Return Type)
        Label funcLabel = new Label("Function");
        funcLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-weight: bold; -fx-font-size: 10px;");

        Node nameNode;
        if (isDeletable) {
            TextField nameField = new TextField(methodName);
            nameField.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 2 5 2 5;");
            nameField.setPrefWidth(Math.max(80, methodName.length() * 8 + 20));

            nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
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

        // 4. Parameters Row
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

        // Only add "Add Parameter" button if this is NOT the main method (isDeletable is false for Main)
        if (isDeletable) {
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
        }

        HBox paramRow = paramRowBuilder.build();

        headerBox.getChildren().addAll(topRow, paramRow);
        container.getChildren().add(headerBox);

        // --- BODY ---
        if (!isCollapsed) {
            container.getChildren().add(bodyWrapper);
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

        if (isDeletable) {
            Button delBtn = new Button("×");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-font-size: 10px; -fx-padding: 0 0 0 2; -fx-cursor: hand;");
            delBtn.setOnAction(e -> context.codeEditor().deleteParameterFromMethod((MethodDeclaration) this.astNode, index));
            box.getChildren().add(delBtn);
        }

        return box;
    }
}