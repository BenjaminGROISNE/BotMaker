package com.botmaker.parser.handlers;

import com.botmaker.parser.ImportManager;
import com.botmaker.parser.NodeCreator;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.parser.helpers.EnumNodeHelper;
import com.botmaker.parser.helpers.TypeConversionHelper;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles type replacement operations with value preservation.
 * Consolidates logic for variable and field type changes.
 */
public class TypeReplacementHandler {

    private final NodeCreator nodeCreator;

    public TypeReplacementHandler(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    /**
     * Replaces a variable's type, preserving values where possible.
     */
    public String replaceVariableType(CompilationUnit cu, String originalCode,
                                      VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        addRequiredImports(cu, rewriter, newTypeName);

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();

            Expression newInitializer = createInitializerForNewType(
                    ast, cu, rewriter, varDecl.getType().toString(), newTypeName, currentInitializer
            );

            if (newInitializer != null && currentInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Replaces a field's type, preserving values where possible.
     */
    public String replaceFieldType(CompilationUnit cu, String originalCode,
                                   FieldDeclaration fieldDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        addRequiredImports(cu, rewriter, newTypeName);

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(fieldDecl.getType(), newType, null);

        if (!fieldDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();

            Expression newInitializer = createInitializerForNewType(
                    ast, cu, rewriter, fieldDecl.getType().toString(), newTypeName, currentInitializer
            );

            if (newInitializer != null && currentInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Creates an appropriate initializer for a new type, preserving values where possible.
     */
    private Expression createInitializerForNewType(AST ast, CompilationUnit cu, ASTRewrite rewriter,
                                                   String oldTypeName, String newTypeName,
                                                   Expression currentInitializer) {
        List<Expression> valuesToPreserve = new ArrayList<>();
        String oldLeaf = TypeConversionHelper.getLeafType(oldTypeName);
        String newLeaf = TypeConversionHelper.getLeafType(newTypeName);

        // Preserve values if leaf types match
        if (oldLeaf.equals(newLeaf) && currentInitializer != null) {
            TypeConversionHelper.collectLeafValues(currentInitializer, valuesToPreserve);
        }

        // Check if new type is enum
        boolean isNewTypeEnum = TypeManager.isEnumType(newLeaf, cu);

        if (isNewTypeEnum) {
            return createEnumInitializer(ast, cu, rewriter, newTypeName, newLeaf);
        } else if (newTypeName.startsWith("ArrayList<")) {
            return nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, valuesToPreserve);
        } else if (newTypeName.endsWith("[]")) {
            return createArrayInitializer(ast, newTypeName, valuesToPreserve);
        } else {
            return !valuesToPreserve.isEmpty() ?
                    (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0)) :
                    nodeCreator.createDefaultInitializer(ast, newTypeName);
        }
    }

    /**
     * Creates an enum initializer (either single value or ArrayList of enum).
     */
    private Expression createEnumInitializer(AST ast, CompilationUnit cu, ASTRewrite rewriter,
                                             String newTypeName, String enumTypeName) {
        String firstConstant = EnumNodeHelper.findFirstEnumConstant(cu, enumTypeName);

        if (firstConstant != null) {
            if (newTypeName.startsWith("ArrayList<")) {
                // ArrayList<EnumType> - create empty list initially
                return nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, null);
            } else {
                // Single enum value - use first constant
                QualifiedName qn = ast.newQualifiedName(
                        ast.newSimpleName(enumTypeName),
                        ast.newSimpleName(firstConstant)
                );
                return qn;
            }
        } else {
            // Fallback if no constants found
            return ast.newNullLiteral();
        }
    }

    /**
     * Creates an array initializer with preserved values.
     */
    private Expression createArrayInitializer(AST ast, String typeName, List<Expression> valuesToPreserve) {
        ArrayCreation creation = ast.newArrayCreation();
        creation.setType((ArrayType) TypeManager.createTypeNode(ast, typeName));
        ArrayInitializer ai = ast.newArrayInitializer();

        if (!valuesToPreserve.isEmpty()) {
            for (Expression val : valuesToPreserve) {
                ai.expressions().add(ASTNode.copySubtree(ast, val));
            }
        }

        creation.setInitializer(ai);
        return creation;
    }

    /**
     * Adds required imports for ArrayList types.
     */
    private void addRequiredImports(CompilationUnit cu, ASTRewrite rewriter, String typeName) {
        if (typeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }
    }
}