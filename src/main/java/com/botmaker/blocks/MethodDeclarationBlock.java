package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.Collections;
import java.util.List;

public class MethodDeclarationBlock extends AbstractStatementBlock implements BlockWithChildren {

    private final String methodName;
    private final String returnType;
    private BodyBlock body;

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
        VBox container = new VBox(0); // 0 spacing, handled by padding
        container.getStyleClass().add("method-block");
        // CSS: .method-block { -fx-background-color: transparent; }

        // --- HEADER SECTION ---
        VBox headerBox = new VBox(5);
        headerBox.getStyleClass().add("method-header");
        // We will do inline styles here for immediate results, but moving to CSS is better
        // A nice purple/indigo for functions
        headerBox.setStyle(
                "-fx-background-color: #8E44AD; " +
                        "-fx-background-radius: 8 8 0 0; " +
                        "-fx-padding: 8 10 8 10;"
        );

        // Row 1: "Function [Name] returns [Type]"
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label funcLabel = new Label("Function");
        funcLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-weight: bold; -fx-font-size: 10px; -fx-text-transform: uppercase;");

        Label nameLabel = new Label(methodName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label returnsLabel = new Label("returns");
        returnsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-style: italic; -fx-font-size: 11px;");



        ComboBox<String> typeSelector = new ComboBox<>();
        typeSelector.getItems().add("void");
        typeSelector.getItems().addAll(TypeManager.getFundamentalTypeNames());
        typeSelector.setValue(returnType);
        typeSelector.setStyle("-fx-font-size: 11px; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 4;");
        // Force the text inside combo to be visible (JavaFX Combobox styling can be tricky)
        typeSelector.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null) { setText(null); }
                else {
                    setText(item);
                    setStyle("-fx-text-fill: white;"); // Make selected text white
                }
            }
        });

        typeSelector.setOnAction(e -> {
            String selected = typeSelector.getValue();
            if (!selected.equals(returnType)) {
                context.codeEditor().setMethodReturnType((MethodDeclaration) this.astNode, selected);
            }
        });

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            context.codeEditor().deleteMethod((MethodDeclaration) this.astNode);
        });
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 2 6 2 6; -fx-cursor: hand; -fx-background-radius: 4;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;"));


        topRow.getChildren().addAll(funcLabel, nameLabel, spacer, returnsLabel, typeSelector, deleteBtn);

        // Row 2: Parameters
        HBox paramRow = new HBox(6);
        paramRow.setAlignment(Pos.CENTER_LEFT);

        Label paramsLabel = new Label("Inputs:");
        paramsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        paramRow.getChildren().add(paramsLabel);

        MethodDeclaration md = (MethodDeclaration) this.astNode;
        List<?> params = md.parameters();

        for (int i = 0; i < params.size(); i++) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(i);
            Node paramNode = createParamNode(param, i, context);
            paramRow.getChildren().add(paramNode);
        }

        MenuButton addParamBtn = new MenuButton("+");
        addParamBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 10;");

        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                String defaultName = DefaultNames.forType(type);
                context.codeEditor().addParameterToMethod(
                        (MethodDeclaration) this.astNode, type, defaultName
                );
            });
            addParamBtn.getItems().add(item);
        }
        paramRow.getChildren().add(addParamBtn);

        headerBox.getChildren().addAll(topRow, paramRow);
        container.getChildren().add(headerBox);

        // --- BODY ---
        VBox bodyWrapper = new VBox();
        // Add a left border to visually connect header to body
        bodyWrapper.setStyle("-fx-border-color: #8E44AD; -fx-border-width: 0 0 0 4; -fx-background-color: rgba(142, 68, 173, 0.05);");

        if (body != null) {
            Node bodyNode = body.getUINode(context);
            VBox.setVgrow(bodyNode, Priority.ALWAYS);
            bodyWrapper.getChildren().add(bodyNode);
        }
        container.getChildren().add(bodyWrapper);

        return container;
    }

    private Node createParamNode(SingleVariableDeclaration param, int index, CompletionContext context) {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        // Pill style: Semi-transparent white background
        box.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 12; -fx-padding: 3 8 3 8;");

        Label typeLabel = new Label(param.getType().toString());
        typeLabel.setStyle("-fx-text-fill: #8E44AD; -fx-font-weight: bold; -fx-font-size: 11px;");

        String currentName = param.getName().getIdentifier();
        TextField nameField = new TextField(currentName);

        // Make the TextField look like a label unless clicked
        nameField.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-padding: 0; " +
                        "-fx-font-size: 11px; " +
                        "-fx-text-fill: #333;"
        );
        nameField.setPrefWidth(Math.max(30, currentName.length() * 7));

        // When focused, add a subtle underline or border
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                nameField.setStyle("-fx-background-color: white; -fx-padding: 0; -fx-font-size: 11px; -fx-text-fill: black; -fx-border-color: #8E44AD; -fx-border-width: 0 0 1 0;");
            } else {
                // Lost focus - save and revert style
                nameField.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-font-size: 11px; -fx-text-fill: #333;");
                String val = nameField.getText().trim();
                if (!val.isEmpty() && !val.equals(currentName)) {
                    context.codeEditor().renameMethodParameter((MethodDeclaration) this.astNode, index, val);
                } else {
                    nameField.setText(currentName);
                }
            }
        });

        nameField.setOnAction(e -> box.requestFocus()); // Commit on Enter

        MethodDeclaration md = (MethodDeclaration) this.astNode;
        boolean isMainMethod = "main".equals(md.getName().getIdentifier()) &&
                Modifier.isStatic(md.getModifiers()) &&
                Modifier.isPublic(md.getModifiers());

        if (!isMainMethod) {
            Button deleteBtn = new Button("×");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                context.codeEditor().deleteMethod((MethodDeclaration) this.astNode);
            });
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 2 6 2 6; -fx-cursor: hand; -fx-background-radius: 4;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 16px; -fx-padding: 0; -fx-cursor: hand;"));

            box.getChildren().add(deleteBtn);
        }
        box.getChildren().addAll(typeLabel, nameField);
        return box;
    }
}