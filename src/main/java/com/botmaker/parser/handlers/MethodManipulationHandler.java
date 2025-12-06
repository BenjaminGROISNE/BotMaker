package com.botmaker.parser.handlers;

import com.botmaker.parser.NodeCreator;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeInfo;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

public class MethodManipulationHandler {

    private final NodeCreator nodeCreator;

    public MethodManipulationHandler(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    public String addMethodToClass(CompilationUnit cu, String originalCode, TypeDeclaration typeDecl, String methodName, String returnType, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        MethodDeclaration newMethod = ast.newMethodDeclaration();
        newMethod.setName(ast.newSimpleName(methodName));
        newMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        newMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
        if ("void".equals(returnType)) newMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        else newMethod.setReturnType2(TypeManager.createTypeNode(ast, returnType));
        Block body = ast.newBlock();
        newMethod.setBody(body);
        ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertAt(newMethod, index, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String deleteMethodFromClass(CompilationUnit cu, String originalCode, MethodDeclaration method) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(method, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String renameMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(method.getName(), ast.newSimpleName(newName), null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Moves a body declaration (method/enum/field) to a new index within a type.
     */
    public String moveBodyDeclaration(CompilationUnit cu, String originalCode, BodyDeclaration declToMove, TypeDeclaration targetType, int targetIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        ListRewrite listRewrite = rewriter.getListRewrite(targetType, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

        // Remove from old location and copy to new location
        // Note: For simplicity assuming same class move for now.
        // If different class, we'd need two list rewrites.

        BodyDeclaration placeholder = (BodyDeclaration) rewriter.createMoveTarget(declToMove);

        // Remove original
        // (createMoveTarget implicitly handles the move logic in replace/insert,
        // but explicit removal and insertion gives control over index)
        // Actually, with createMoveTarget we just insert the placeholder at the new location.
        // But ASTRewrite handles moves better if we don't manually remove first if using move target.

        listRewrite.insertAt(placeholder, targetIndex, null);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String updateMethodInvocation(CompilationUnit cu, String originalCode,
                                         MethodInvocation mi, String newScope,
                                         String newMethodName, List<String> newParamTypes) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (newScope == null || newScope.isEmpty() || newScope.equals("Local")) {
            if (mi.getExpression() != null) rewriter.remove(mi.getExpression(), null);
        } else {
            SimpleName newScopeNode = ast.newSimpleName(newScope);
            if (mi.getExpression() == null) rewriter.set(mi, MethodInvocation.EXPRESSION_PROPERTY, newScopeNode, null);
            else rewriter.replace(mi.getExpression(), newScopeNode, null);
        }

        if (!mi.getName().getIdentifier().equals(newMethodName)) {
            rewriter.replace(mi.getName(), ast.newSimpleName(newMethodName), null);
        }

        ListRewrite argsRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        List<?> currentArgs = mi.arguments();
        int targetCount = newParamTypes.size();
        int currentCount = currentArgs.size();

        if (currentCount > targetCount) {
            for (int i = currentCount - 1; i >= targetCount; i--) {
                argsRewrite.remove((ASTNode) currentArgs.get(i), null);
            }
        } else if (currentCount < targetCount) {
            for (int i = currentCount; i < targetCount; i++) {
                String typeName = newParamTypes.get(i);
                Expression defaultExpr = nodeCreator.createDefaultInitializer(ast, typeName);
                argsRewrite.insertLast(defaultExpr, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newArg = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newArg != null) rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(newArg, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, Expression newArgument) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY).insertLast(newArgument, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String renameMethodParameter(CompilationUnit cu, String originalCode, MethodDeclaration method, int index, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<?> params = method.parameters();
        if (index >= 0 && index < params.size()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(index);
            SimpleName newNameNode = ast.newSimpleName(newName);
            rewriter.replace(param.getName(), newNameNode, null);
        }
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String setMethodReturnType(CompilationUnit cu, String originalCode, MethodDeclaration method, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Type newType;
        if ("void".equals(newTypeName)) newType = ast.newPrimitiveType(PrimitiveType.VOID);
        else newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(method.getReturnType2(), newType, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String addParameterToMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, String typeName, String paramName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
        SingleVariableDeclaration newParam = ast.newSingleVariableDeclaration();
        newParam.setType(TypeManager.createTypeNode(ast, typeName));
        newParam.setName(ast.newSimpleName(paramName));
        listRewrite.insertLast(newParam, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String deleteParameterFromMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
        List<?> params = method.parameters();
        if (index >= 0 && index < params.size()) listRewrite.remove((ASTNode) params.get(index), null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    public String setReturnExpression(CompilationUnit cu, String originalCode, ReturnStatement returnStmt, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newExpr == null) return originalCode;
        if (returnStmt.getExpression() == null) rewriter.set(returnStmt, ReturnStatement.EXPRESSION_PROPERTY, newExpr, null);
        else rewriter.replace(returnStmt.getExpression(), newExpr, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }
}