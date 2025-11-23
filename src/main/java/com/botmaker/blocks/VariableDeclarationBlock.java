package com.botmaker.blocks;

import com.botmaker.core.AbstractStatementBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.ui.components.BlockUIComponents;
import com.botmaker.ui.components.TextFieldComponents;
import com.botmaker.util.TypeManager;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import static com.botmaker.ui.components.BlockUIComponents.createTypeLabel;

public class VariableDeclarationBlock extends AbstractStatementBlock {

    private final String variableName;
    private final Type variableType;
    private ExpressionBlock initializer;

    public VariableDeclarationBlock(String id, VariableDeclarationStatement astNode) {
        super(id, astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);
        this.variableName = fragment.getName().getIdentifier();
        this.variableType = astNode.getType();
        this.initializer = null;
    }

    public void setInitializer(ExpressionBlock initializer) { this.initializer = initializer; }

    @Override
    protected Node createUINode(CompletionContext context) {
        // 1. Type Label with Menu
        Label typeLabel = createTypeLabel(getDisplayTypeName(variableType));
        typeLabel.setCursor(Cursor.HAND);
        Tooltip.install(typeLabel, new Tooltip("Click to change type (ArrayList/Base)"));
        typeLabel.setOnMouseClicked(e -> showTypeMenu(typeLabel, context));

        // 2. Name Field
        TextField nameField = TextFieldComponents.createVariableNameField(variableName, newName -> {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) this.astNode).fragments().get(0);
            if (!newName.equals(variableName) && !newName.isEmpty()) {
                context.codeEditor().replaceSimpleName(fragment.getName(), newName);
            }
        });

        // 3. Initializer Logic
        Node initNode;
        if (initializer != null) {
            // FIX: Restored original priority logic
            if (initializer instanceof ListBlock) {
                // Render ListBlock directly (it handles its own layout)
                initNode = initializer.getUINode(context);
            } else if (initializer.getAstNode() instanceof org.eclipse.jdt.core.dom.ArrayInitializer) {
                // Fallback for other array initializers (add bracket styling)
                initNode = createListDisplay(context);
            } else {
                // Standard expression
                initNode = initializer.getUINode(context);
            }
        } else {
            initNode = createExpressionDropZone(context);
        }

        // 4. Add Button
        String uiTargetType = TypeManager.determineUiType(variableType.toString());
        Button addButton = createAddButton(e ->
                showExpressionMenuAndReplace((Button)e.getSource(), context, uiTargetType,
                        initializer != null ? (org.eclipse.jdt.core.dom.Expression)initializer.getAstNode() : null)
        );

        Node content = createSentence(
                typeLabel,
                nameField,
                createKeywordLabel("="),
                initNode,
                addButton
        );

        HBox container = createStandardHeader(context, content);
        container.getStyleClass().add("variable-declaration-block");

        return container;
    }

    private void showTypeMenu(Node anchor, CompletionContext context) {
        ContextMenu menu = new ContextMenu();
        String currentStr = variableType.toString();
        boolean isArrayListType = isArrayList(variableType);
        boolean isArray = variableType.isArrayType();
        final String baseType = extractBaseType(currentStr, isArrayListType, isArray);

        // Toggle ArrayList
        MenuItem toggleList = new MenuItem(isArrayListType ? "Convert to Single Value" : "Convert to ArrayList");
        toggleList.setStyle("-fx-font-weight: bold;");
        toggleList.setOnAction(e -> {
            String newType = isArrayListType ? baseType : "ArrayList<" + TypeManager.toWrapperType(baseType) + ">";
            context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
        });
        menu.getItems().add(toggleList);

        // Nested List Option
        if (isArrayListType) {
            MenuItem makeNested = new MenuItem("Make ArrayList of ArrayLists");
            makeNested.setOnAction(e -> {
                String newType = "ArrayList<ArrayList<" + TypeManager.toWrapperType(baseType) + ">>";
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            menu.getItems().add(makeNested);
        }

        menu.getItems().add(new SeparatorMenuItem());

        // Change Base Type
        Menu changeBaseMenu = new Menu("Change Base Type");
        for (String type : TypeManager.getFundamentalTypeNames()) {
            MenuItem item = new MenuItem(type);
            item.setOnAction(e -> {
                String newType = isArrayListType ? "ArrayList<" + TypeManager.toWrapperType(type) + ">" : type;
                context.codeEditor().replaceVariableType((VariableDeclarationStatement) this.astNode, newType);
            });
            changeBaseMenu.getItems().add(item);
        }
        menu.getItems().add(changeBaseMenu);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private String extractBaseType(String currentStr, boolean isArrayListType, boolean isArray) {
        if (isArrayListType && currentStr.contains("<")) {
            return currentStr.substring(currentStr.indexOf("<") + 1, currentStr.lastIndexOf(">"));
        } else if (isArray) {
            return currentStr.replace("[]", "");
        }
        return currentStr;
    }

    private HBox createListDisplay(CompletionContext context) {
        HBox listBox = new HBox(3);
        listBox.setAlignment(Pos.CENTER_LEFT);
        listBox.getStyleClass().add("inline-list-display");

        Label open = new Label("["); open.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label close = new Label("]"); close.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        listBox.getChildren().addAll(open, initializer.getUINode(context), close);
        return listBox;
    }

    private String getDisplayTypeName(Type type) {
        String typeName = type.toString();
        if (isArrayList(type)) return typeName;
        if (typeName.endsWith("[]")) return typeName.replace("[]", " list");
        return typeName;
    }

    private boolean isArrayList(Type type) {
        return type.toString().startsWith("ArrayList");
    }
}