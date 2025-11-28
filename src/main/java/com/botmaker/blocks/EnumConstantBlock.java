package com.botmaker.blocks;

import com.botmaker.core.AbstractExpressionBlock;
import com.botmaker.lsp.CompletionContext;
import com.botmaker.util.BlockIdPrefix;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an enum constant reference expression (e.g., MyEnum.OPTION_A)
 */
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
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("enum-constant-block");
        container.setStyle(
                "-fx-background-color: #d35400;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 4 8 4 8;"
        );

        // Enum type label
        Label typeLabel = new Label(enumTypeName);
        typeLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;"
        );

        // Dot separator
        Label dot = new Label(".");
        dot.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");

        // Constant selector
        ComboBox<String> constantSelector = new ComboBox<>();
        constantSelector.getStyleClass().add("enum-constant-selector");
        constantSelector.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 2 6 2 6;"
        );

        // Populate with available constants
        List<String> constants = getEnumConstants(enumTypeName, context);
        constantSelector.getItems().addAll(constants);
        constantSelector.setValue(constantName);

        // Handle selection change
        constantSelector.setOnAction(e -> {
            String newConstant = constantSelector.getValue();
            if (newConstant != null && !newConstant.equals(constantName)) {
                updateConstant(newConstant, context);
            }
        });

        container.getChildren().addAll(typeLabel, dot, constantSelector);
        return container;
    }

    private List<String> getEnumConstants(String enumName, CompletionContext context) {
        List<String> constants = new ArrayList<>();

        CompilationUnit cu = context.applicationState().getCompilationUnit().orElse(null);
        if (cu == null) return constants;

        // Search for the enum declaration
        EnumDeclaration enumDecl = findEnumDeclaration(cu, enumName);
        if (enumDecl != null) {
            for (Object obj : enumDecl.enumConstants()) {
                if (obj instanceof EnumConstantDeclaration) {
                    EnumConstantDeclaration ecd = (EnumConstantDeclaration) obj;
                    constants.add(ecd.getName().getIdentifier());
                }
            }
        }

        return constants;
    }

    private EnumDeclaration findEnumDeclaration(CompilationUnit cu, String enumName) {
        // Check top-level types
        for (Object obj : cu.types()) {
            if (obj instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) obj;
                if (enumDecl.getName().getIdentifier().equals(enumName)) {
                    return enumDecl;
                }
            }
            // Check class body declarations
            else if (obj instanceof TypeDeclaration) {
                TypeDeclaration typeDecl = (TypeDeclaration) obj;
                for (Object bodyObj : typeDecl.bodyDeclarations()) {
                    if (bodyObj instanceof EnumDeclaration) {
                        EnumDeclaration enumDecl = (EnumDeclaration) bodyObj;
                        if (enumDecl.getName().getIdentifier().equals(enumName)) {
                            return enumDecl;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void updateConstant(String newConstant, CompletionContext context) {
        // Replace with new qualified name: EnumType.NEW_CONSTANT
        Expression oldExpr = (Expression) this.astNode;

        // Use the replaceSimpleName if it's a SimpleName, otherwise need special handling
        if (oldExpr instanceof QualifiedName) {
            QualifiedName qn = (QualifiedName) oldExpr;
            context.codeEditor().replaceSimpleName(qn.getName(), newConstant);
        } else if (oldExpr instanceof SimpleName) {
            context.codeEditor().replaceSimpleName((SimpleName) oldExpr, newConstant);
        }
    }
}