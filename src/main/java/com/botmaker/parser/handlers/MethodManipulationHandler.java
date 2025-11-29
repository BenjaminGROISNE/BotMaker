package com.botmaker.parser.handlers;

import com.botmaker.parser.NodeCreator;
import com.botmaker.parser.helpers.AstRewriteHelper;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.List;

/**
 * Handles method declaration and method invocation operations.
 */
public class MethodManipulationHandler {

    private final NodeCreator nodeCreator;

    public MethodManipulationHandler(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    /**
     * Adds a new method to a class.
     */
    public String addMethodToClass(CompilationUnit cu, String originalCode,
                                   TypeDeclaration typeDecl, String methodName,
                                   String returnType, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        MethodDeclaration newMethod = ast.newMethodDeclaration();
        newMethod.setName(ast.newSimpleName(methodName));

        // Set modifiers: public static
        newMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        newMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

        // Set return type
        if ("void".equals(returnType)) {
            newMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
        } else {
            newMethod.setReturnType2(TypeManager.createTypeNode(ast, returnType));
        }

        // Create empty body
        Block body = ast.newBlock();
        newMethod.setBody(body);

        // Insert into class
        ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
        listRewrite.insertAt(newMethod, index, null);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes a method from a class.
     */
    public String deleteMethodFromClass(CompilationUnit cu, String originalCode,
                                        MethodDeclaration method) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(method, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Updates a method invocation (scope, name, and parameters).
     */
    public String updateMethodInvocation(CompilationUnit cu, String originalCode,
                                         MethodInvocation mi, String newScope,
                                         String newMethodName, List<String> newParamTypes) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Update scope
        if (newScope == null || newScope.isEmpty() || newScope.equals("Local")) {
            if (mi.getExpression() != null) {
                rewriter.remove(mi.getExpression(), null);
            }
        } else {
            SimpleName newScopeNode = ast.newSimpleName(newScope);
            if (mi.getExpression() == null) {
                rewriter.set(mi, MethodInvocation.EXPRESSION_PROPERTY, newScopeNode, null);
            } else {
                rewriter.replace(mi.getExpression(), newScopeNode, null);
            }
        }

        // Update method name
        if (!mi.getName().getIdentifier().equals(newMethodName)) {
            rewriter.replace(mi.getName(), ast.newSimpleName(newMethodName), null);
        }

        // Update arguments
        ListRewrite argsRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        List<?> currentArgs = mi.arguments();
        int targetCount = newParamTypes.size();
        int currentCount = currentArgs.size();

        // Remove excess arguments
        if (currentCount > targetCount) {
            for (int i = currentCount - 1; i >= targetCount; i--) {
                argsRewrite.remove((ASTNode) currentArgs.get(i), null);
            }
        }
        // Add missing arguments
        else if (currentCount < targetCount) {
            for (int i = currentCount; i < targetCount; i++) {
                String typeName = newParamTypes.get(i);
                Expression defaultExpr = nodeCreator.createDefaultInitializer(ast, typeName);
                argsRewrite.insertLast(defaultExpr, null);
            }
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Adds an argument to a method invocation.
     */
    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode,
                                                MethodInvocation mi, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newArg = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newArg != null) {
            rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY)
                    .insertLast(newArg, null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Adds a specific expression as an argument to a method invocation.
     */
    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode,
                                                MethodInvocation mi, Expression newArgument) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY)
                .insertLast(newArgument, null);

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Renames a method parameter.
     */
    public String renameMethodParameter(CompilationUnit cu, String originalCode,
                                        MethodDeclaration method, int index, String newName) {
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

    /**
     * Sets the return type of a method.
     */
    public String setMethodReturnType(CompilationUnit cu, String originalCode,
                                      MethodDeclaration method, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Type newType;
        if ("void".equals(newTypeName)) {
            newType = ast.newPrimitiveType(PrimitiveType.VOID);
        } else {
            newType = TypeManager.createTypeNode(ast, newTypeName);
        }

        rewriter.replace(method.getReturnType2(), newType, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Adds a parameter to a method.
     */
    public String addParameterToMethod(CompilationUnit cu, String originalCode,
                                       MethodDeclaration method, String typeName, String paramName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);

        SingleVariableDeclaration newParam = ast.newSingleVariableDeclaration();
        newParam.setType(TypeManager.createTypeNode(ast, typeName));
        newParam.setName(ast.newSimpleName(paramName));

        listRewrite.insertLast(newParam, null);
        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes a parameter from a method.
     */
    public String deleteParameterFromMethod(CompilationUnit cu, String originalCode,
                                            MethodDeclaration method, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);

        List<?> params = method.parameters();
        if (index >= 0 && index < params.size()) {
            listRewrite.remove((ASTNode) params.get(index), null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }

    /**
     * Sets the return expression of a return statement.
     */
    public String setReturnExpression(CompilationUnit cu, String originalCode,
                                      ReturnStatement returnStmt, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newExpr == null) return originalCode;

        if (returnStmt.getExpression() == null) {
            rewriter.set(returnStmt, ReturnStatement.EXPRESSION_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(returnStmt.getExpression(), newExpr, null);
        }

        return AstRewriteHelper.applyRewrite(rewriter, originalCode);
    }
}