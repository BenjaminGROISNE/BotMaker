package com.botmaker.parser.handlers;

import com.botmaker.parser.helpers.AstRewriteHelper;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

/**
 * Handles enum declaration and constant operations.
 */
public class EnumManipulationHandler {

    /**
     * Adds a new enum to a class.
     */
    public String addEnumToClass(CompilationUnit cu, String originalCode,
                                 TypeDeclaration typeDecl, String enumName, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        EnumDeclaration newEnum = ast.newEnumDeclaration();
        newEnum.setName(ast.newSimpleName(enumName));
        newEnum.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

        // Add default values
        EnumConstantDeclaration const1 = ast.newEnumConstantDeclaration();
        const1.setName(ast.newSimpleName("OPTION_A"));
        newEnum.enumConstants().add(const1);

        ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertAt(newEnum, index, null);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes an enum from a class.
     */
    public String deleteEnumFromClass(CompilationUnit cu, String originalCode,
                                      EnumDeclaration enumDecl) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(enumDecl, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Renames an enum.
     */
    public String renameEnum(CompilationUnit cu, String originalCode,
                             EnumDeclaration enumNode, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(enumNode.getName(), ast.newSimpleName(newName), null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Adds a constant to an enum.
     */
    public String addEnumConstant(CompilationUnit cu, String originalCode,
                                  EnumDeclaration enumNode, String constantName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        ListRewrite listRewrite = rewriter.getListRewrite(enumNode, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);

        EnumConstantDeclaration newConst = ast.newEnumConstantDeclaration();
        newConst.setName(ast.newSimpleName(constantName));

        listRewrite.insertLast(newConst, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes a constant from an enum.
     */
    public String deleteEnumConstant(CompilationUnit cu, String originalCode,
                                     EnumDeclaration enumNode, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(enumNode, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);

        List<?> constants = enumNode.enumConstants();
        if (index >= 0 && index < constants.size()) {
            listRewrite.remove((ASTNode) constants.get(index), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Renames a constant in an enum.
     */
    public String renameEnumConstant(CompilationUnit cu, String originalCode,
                                     EnumDeclaration enumNode, int index, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        List<?> constants = enumNode.enumConstants();
        if (index >= 0 && index < constants.size()) {
            EnumConstantDeclaration constDecl = (EnumConstantDeclaration) constants.get(index);
            rewriter.replace(constDecl.getName(), ast.newSimpleName(newName), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }
}