package com.botmaker.parser.handlers;

import com.botmaker.parser.ImportManager;
import com.botmaker.parser.NodeCreator;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.parser.helpers.EnumNodeHelper;
import com.botmaker.parser.helpers.TypeConversionHelper;
import com.botmaker.util.TypeInfo;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;

public class TypeReplacementHandler {

    private final NodeCreator nodeCreator;

    public TypeReplacementHandler(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    // ... (Keep existing replaceVariableType, replaceFieldType) ...
    public String replaceVariableType(CompilationUnit cu, String originalCode, VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        addRequiredImports(cu, rewriter, newTypeName);
        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);
        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = createInitializerForNewType(ast, cu, rewriter, varDecl.getType().toString(), newTypeName, currentInitializer);
            if (newInitializer != null && currentInitializer != null) rewriter.replace(currentInitializer, newInitializer, null);
        }
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String replaceFieldType(CompilationUnit cu, String originalCode, FieldDeclaration fieldDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        addRequiredImports(cu, rewriter, newTypeName);
        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(fieldDecl.getType(), newType, null);
        if (!fieldDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = createInitializerForNewType(ast, cu, rewriter, fieldDecl.getType().toString(), newTypeName, currentInitializer);
            if (newInitializer != null && currentInitializer != null) rewriter.replace(currentInitializer, newInitializer, null);
        }
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Creates an appropriate initializer for a new type, preserving values where possible.
     * UPDATED: Uses TypeInfo extensively.
     */
    private Expression createInitializerForNewType(AST ast, CompilationUnit cu, ASTRewrite rewriter,
                                                   String oldTypeName, String newTypeName,
                                                   Expression currentInitializer) {
        List<Expression> valuesToPreserve = new ArrayList<>();

        TypeInfo oldType = TypeInfo.from(oldTypeName);
        TypeInfo newType = TypeInfo.from(newTypeName);

        String oldLeaf = oldType.getLeafType().getTypeName();
        String newLeaf = newType.getLeafType().getTypeName();

        // Preserve values if leaf types match (e.g. converting int to int[])
        if (oldLeaf.equals(newLeaf) && currentInitializer != null) {
            TypeConversionHelper.collectLeafValues(currentInitializer, valuesToPreserve);
        }

        boolean isNewTypeEnum = newType.getLeafType().isEnum() || TypeManager.isEnumType(newType.getLeafType(), cu);

        if (isNewTypeEnum) {
            return createEnumInitializer(ast, cu, rewriter, newTypeName, newLeaf);
        } else if (newType.isArray()) {
            // Using the overloaded createDefaultInitializer that accepts TypeInfo inside NodeCreator would be ideal,
            // but for now we call the specific array logic via the factory logic we know exists.
            // Note: nodeCreator delegates to InitializerFactory.
            // Here we construct it manually since we have valuesToPreserve.
            return createArrayInitializer(ast, newType, valuesToPreserve);
        } else {
            // Primitive or Object
            return !valuesToPreserve.isEmpty() ?
                    (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0)) :
                    nodeCreator.createDefaultInitializer(ast, newTypeName);
        }
    }

    private Expression createEnumInitializer(AST ast, CompilationUnit cu, ASTRewrite rewriter,
                                             String newTypeName, String enumTypeName) {
        String firstConstant = EnumNodeHelper.findFirstEnumConstant(cu, enumTypeName);
        if (firstConstant != null) {
            if (newTypeName.endsWith("[]")) {
                // Array of Enums - create empty array or array with one default
                return createArrayInitializer(ast, TypeInfo.from(newTypeName), null);
            } else {
                // Single enum value
                return ast.newQualifiedName(ast.newSimpleName(enumTypeName), ast.newSimpleName(firstConstant));
            }
        } else {
            return ast.newNullLiteral();
        }
    }

    /**
     * Creates an array initializer with preserved values.
     * UPDATED: Uses TypeInfo parameter.
     */
    private Expression createArrayInitializer(AST ast, TypeInfo typeInfo, List<Expression> valuesToPreserve) {
        ArrayCreation creation = ast.newArrayCreation();
        creation.setType((ArrayType) TypeManager.createTypeNode(ast, typeInfo));
        ArrayInitializer ai = ast.newArrayInitializer();

        // If we have preserved values, add them
        if (valuesToPreserve != null && !valuesToPreserve.isEmpty()) {
            for (Expression val : valuesToPreserve) {
                ai.expressions().add(ASTNode.copySubtree(ast, val));
            }
        }
        // If specific dimensions and no values, ensure nested structure is created (e.g. new int[2][])
        else {
            // If we rely on the InitializerFactory logic for nested arrays:
            // We can just return the creation with empty initializer, or one default element.
            // For now, empty is safer for refactoring.
        }

        creation.setInitializer(ai);
        return creation;
    }

    private void addRequiredImports(CompilationUnit cu, ASTRewrite rewriter, String typeName) {
        if (typeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }
    }
}