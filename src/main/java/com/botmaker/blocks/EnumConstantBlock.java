// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\blocks\EnumConstantBlock.java
package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.parser.helpers.EnumNodeHelper; // Use shared helper
import com.botmaker.ui.builders.BlockLayout;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class EnumConstantBlock extends AbstractExpressionBlock {

    private final String enumTypeName;
    private final String constantName;

    public EnumConstantBlock(String id, QualifiedName astNode) {
        super(id, astNode);
        this.enumTypeName = astNode.getQualifier().toString();
        this.constantName = astNode.getName().getIdentifier();
    }

    public EnumConstantBlock(String id, SimpleName astNode, String enumTypeName) {
        super(id, astNode);
        this.enumTypeName = enumTypeName;
        this.constantName = astNode.getIdentifier();
    }

    @Override
    protected Node createUINode(CompletionContext context) {
        Label typeLabel = new Label(enumTypeName);
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label dot = new Label(".");
        dot.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");

        ComboBox<String> constantSelector = new ComboBox<>();
        constantSelector.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 2 6 2 6;"
        );

        List<String> constants = getEnumConstants(enumTypeName, context);
        constantSelector.getItems().addAll(constants);
        constantSelector.setValue(constantName);

        constantSelector.setOnAction(e -> {
            String newConstant = constantSelector.getValue();
            if (newConstant != null && !newConstant.equals(constantName)) {
                updateConstant(newConstant, context);
            }
        });

        HBox container = BlockLayout.sentence()
                .addNode(typeLabel)
                .addNode(dot)
                .addNode(constantSelector)
                .build();

        container.setStyle(
                "-fx-background-color: #d35400;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 4 8 4 8;"
        );

        return container;
    }

    private List<String> getEnumConstants(String enumName, CompletionContext context) {
        CompilationUnit cu = context.applicationState().getCompilationUnit().orElse(null);
        if (cu == null) return new ArrayList<>();

        // Fix: Use EnumNodeHelper to properly scan both top-level and inner enum declarations
        EnumDeclaration enumDecl = EnumNodeHelper.findEnumDeclaration(cu, enumName);

        if (enumDecl != null) {
            return EnumNodeHelper.getAllEnumConstantNames(enumDecl);
        }

        return new ArrayList<>();
    }

    private void updateConstant(String newConstant, CompletionContext context) {
        Expression oldExpr = (Expression) this.astNode;
        if (oldExpr instanceof QualifiedName) {
            QualifiedName qn = (QualifiedName) oldExpr;
            context.codeEditor().replaceSimpleName(qn.getName(), newConstant);
        } else if (oldExpr instanceof SimpleName) {
            context.codeEditor().replaceSimpleName((SimpleName) oldExpr, newConstant);
        }
    }
}