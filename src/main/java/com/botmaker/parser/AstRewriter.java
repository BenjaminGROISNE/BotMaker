package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.List;

public class AstRewriter {

    private final NodeCreator nodeCreator;

    public AstRewriter(NodeCreator nodeCreator) {
        this.nodeCreator = nodeCreator;
    }

    // ... [Existing methods: moveStatement, addStatement, replaceExpression, replaceLiteral, etc.] ...
    public String moveStatement(CompilationUnit cu, String originalCode, StatementBlock blockToMove, BodyBlock sourceBody, BodyBlock targetBody, int targetIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement statement = (Statement) blockToMove.getAstNode();
        ListRewrite sourceListRewrite = getListRewriteForBody(rewriter, sourceBody);
        ListRewrite targetListRewrite = getListRewriteForBody(rewriter, targetBody);
        if (sourceListRewrite == targetListRewrite || sourceBody == targetBody) {}
        Statement copiedStatement = (Statement) ASTNode.copySubtree(ast, statement);
        sourceListRewrite.remove(statement, null);
        insertIntoList(targetListRewrite, targetBody, copiedStatement, targetIndex);
        return applyRewrite(rewriter, originalCode);
    }



    public String addStatement(CompilationUnit cu, String originalCode, BodyBlock targetBody, AddableBlock type, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement newStatement;
        if (type == AddableBlock.COMMENT) {
            newStatement = (Statement) rewriter.createStringPlaceholder("// Comment", ASTNode.EMPTY_STATEMENT);
        } else {
            newStatement = nodeCreator.createDefaultStatement(ast, type, cu, rewriter);
        }
        if (newStatement == null) return originalCode;
        ListRewrite listRewrite = getListRewriteForBody(rewriter, targetBody);
        insertIntoList(listRewrite, targetBody, newStatement, index);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceExpression(CompilationUnit cu, String originalCode, Expression toReplace, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // NEW: Try to infer context type from parent
        String contextType = inferContextType(toReplace);

        Expression newExpression = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, contextType);
        if (newExpression == null) return originalCode;
        rewriter.replace(toReplace, newExpression, null);
        return applyRewrite(rewriter, originalCode);
    }

    private String inferContextType(Expression expr) {
        ASTNode parent = expr.getParent();

        // Variable declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getParent() instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement varDecl = (VariableDeclarationStatement) frag.getParent();
                String typeName = varDecl.getType().toString();
                if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
                    return typeName.substring(10, typeName.length() - 1);
                }
                return typeName;
            }
        }

        // Assignment
        if (parent instanceof Assignment) {
            Assignment assign = (Assignment) parent;
            Expression lhs = assign.getLeftHandSide();
            if (lhs instanceof SimpleName) {
                // Try to resolve type from bindings
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    return binding.getName();
                }
            }
        }

        return null;
    }

    public String replaceLiteral(CompilationUnit cu, String originalCode, Expression toReplace, String newLiteralValue) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newExpression;
        if (toReplace instanceof StringLiteral) {
            StringLiteral newString = ast.newStringLiteral();
            newString.setLiteralValue(newLiteralValue);
            newExpression = newString;
        } else if (toReplace instanceof NumberLiteral) {
            newExpression = ast.newNumberLiteral(newLiteralValue);
        } else if (toReplace instanceof BooleanLiteral) {
            newExpression = ast.newBooleanLiteral(Boolean.parseBoolean(newLiteralValue));
        } else {
            return originalCode;
        }
        rewriter.replace(toReplace, newExpression, null);
        return applyRewrite(rewriter, originalCode);
    }

    // --- NEW: Infix Operator Replacement ---
    public String replaceInfixOperator(CompilationUnit cu, String originalCode, InfixExpression infix, InfixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.set(infix, InfixExpression.OPERATOR_PROPERTY, newOp, null);
        return applyRewrite(rewriter, originalCode);
    }


    public String setFieldInitializer(CompilationUnit cu, String originalCode,
                                      FieldDeclaration fieldDecl, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Get the fragment
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);

        // Get the field's type name for enum context
        String typeName = fieldDecl.getType().toString();
        // Strip ArrayList wrapper if present
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            typeName = typeName.substring(10, typeName.length() - 1);
        }

        // Create the new expression with type context
        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, typeName);
        if (newExpr == null) return originalCode;

        // Set or replace the initializer
        if (fragment.getInitializer() == null) {
            rewriter.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(fragment.getInitializer(), newExpr, null);
        }

        return applyRewrite(rewriter, originalCode);
    }

    public String replaceFieldType(CompilationUnit cu, String originalCode,
                                   FieldDeclaration fieldDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (newTypeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(fieldDecl.getType(), newType, null);

        if (!fieldDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = null;
            List<Expression> valuesToPreserve = new ArrayList<>();
            String oldLeaf = TypeManager.getLeafType(fieldDecl.getType().toString());
            String newLeaf = TypeManager.getLeafType(newTypeName);

            if (oldLeaf.equals(newLeaf) && currentInitializer != null) {
                collectLeafValues(currentInitializer, valuesToPreserve);
            }

            // Check if new type is enum
            boolean isNewTypeEnum = TypeManager.isEnumType(newLeaf, cu);

            if (isNewTypeEnum) {
                // Create enum constant initializer
                String firstConstant = findFirstEnumConstant(cu, newLeaf);
                if (firstConstant != null) {
                    if (newTypeName.startsWith("ArrayList<")) {
                        // ArrayList<EnumType> - create empty list initially
                        newInitializer = nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, null);
                    } else {
                        // Single enum value - use first constant
                        QualifiedName qn = ast.newQualifiedName(
                                ast.newSimpleName(newLeaf),
                                ast.newSimpleName(firstConstant)
                        );
                        newInitializer = qn;
                    }
                } else {
                    // Fallback if no constants found
                    newInitializer = ast.newNullLiteral();
                }
            } else if (newTypeName.startsWith("ArrayList<")) {
                newInitializer = nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, valuesToPreserve);
            } else if (newTypeName.endsWith("[]")) {
                ArrayCreation creation = ast.newArrayCreation();
                creation.setType((ArrayType) TypeManager.createTypeNode(ast, newTypeName));
                ArrayInitializer ai = ast.newArrayInitializer();
                if (!valuesToPreserve.isEmpty()) {
                    for(Expression val : valuesToPreserve) ai.expressions().add(ASTNode.copySubtree(ast, val));
                }
                creation.setInitializer(ai);
                newInitializer = creation;
            } else {
                newInitializer = !valuesToPreserve.isEmpty() ?
                        (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0)) :
                        nodeCreator.createDefaultInitializer(ast, newTypeName);
            }

            if (newInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String setVariableInitializer(CompilationUnit cu, String originalCode,
                                         VariableDeclarationStatement varDecl, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Get the fragment
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);

        // NEW: Get the variable's type name for enum context
        String typeName = varDecl.getType().toString();
        // Strip ArrayList wrapper if present
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            typeName = typeName.substring(10, typeName.length() - 1);
        }

        // Create the new expression with type context
        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter, typeName);
        if (newExpr == null) return originalCode;

        // Set or replace the initializer
        if (fragment.getInitializer() == null) {
            rewriter.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newExpr, null);
        } else {
            rewriter.replace(fragment.getInitializer(), newExpr, null);
        }

        return applyRewrite(rewriter, originalCode);
    }

    public String addMethodToClass(CompilationUnit cu, String originalCode, TypeDeclaration typeDecl,
                                   String methodName, String returnType, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Create new method
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

        return applyRewrite(rewriter, originalCode);
    }

    public String deleteMethodFromClass(CompilationUnit cu, String originalCode, MethodDeclaration method) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(method, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String updateComment(String originalCode, Comment commentNode, String newText) {
        try {
            IDocument document = new Document(originalCode);
            String replacement = newText.contains("\n") ? "/* " + newText + " */" : "// " + newText;
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), replacement);
            return document.get();
        } catch (Exception e) { return originalCode; }
    }

    public String deleteComment(String originalCode, Comment commentNode) {
        try {
            IDocument document = new Document(originalCode);
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), "");
            return document.get();
        } catch (Exception e) { return originalCode; }
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        Expression newArg = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newArg != null) listRewrite.insertLast(newArg, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, Expression newArgument) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        listRewrite.insertLast(newArgument, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceSimpleName(CompilationUnit cu, String originalCode, SimpleName toReplace, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(toReplace, ast.newSimpleName(newName), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteNode(CompilationUnit cu, String originalCode, ASTNode toDelete) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(toDelete, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceVariableType(CompilationUnit cu, String originalCode, VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        if (newTypeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }
        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = null;
            List<Expression> valuesToPreserve = new ArrayList<>();
            String oldLeaf = TypeManager.getLeafType(varDecl.getType().toString());
            String newLeaf = TypeManager.getLeafType(newTypeName);

            if (oldLeaf.equals(newLeaf) && currentInitializer != null) {
                collectLeafValues(currentInitializer, valuesToPreserve);
            }

            // CHANGED: Use actual enum check instead of heuristic
            boolean isNewTypeEnum = TypeManager.isEnumType(newLeaf, cu);

            if (isNewTypeEnum) {
                // Create enum constant initializer
                String firstConstant = findFirstEnumConstant(cu, newLeaf);
                if (firstConstant != null) {
                    if (newTypeName.startsWith("ArrayList<")) {
                        // ArrayList<EnumType> - create empty list initially
                        newInitializer = nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, null);
                    } else {
                        // Single enum value - use first constant
                        QualifiedName qn = ast.newQualifiedName(
                                ast.newSimpleName(newLeaf),
                                ast.newSimpleName(firstConstant)
                        );
                        newInitializer = qn;
                    }
                } else {
                    // Fallback if no constants found
                    newInitializer = ast.newNullLiteral();
                }
            } else if (newTypeName.startsWith("ArrayList<")) {
                newInitializer = nodeCreator.createRecursiveListInitializer(ast, newTypeName, cu, rewriter, valuesToPreserve);
            } else if (newTypeName.endsWith("[]")) {
                ArrayCreation creation = ast.newArrayCreation();
                creation.setType((ArrayType) TypeManager.createTypeNode(ast, newTypeName));
                ArrayInitializer ai = ast.newArrayInitializer();
                if (!valuesToPreserve.isEmpty()) {
                    for(Expression val : valuesToPreserve) ai.expressions().add(ASTNode.copySubtree(ast, val));
                }
                creation.setInitializer(ai);
                newInitializer = creation;
            } else {
                newInitializer = !valuesToPreserve.isEmpty() ?
                        (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0)) :
                        nodeCreator.createDefaultInitializer(ast, newTypeName);
            }

            if (newInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
    }

    private String findFirstEnumConstant(CompilationUnit cu, String enumName) {
        if (cu == null || enumName == null) return null;

        // Search for the enum declaration
        for (Object obj : cu.types()) {
            if (obj instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) obj;
                if (enumDecl.getName().getIdentifier().equals(enumName)) {
                    if (!enumDecl.enumConstants().isEmpty()) {
                        EnumConstantDeclaration first = (EnumConstantDeclaration) enumDecl.enumConstants().get(0);
                        return first.getName().getIdentifier();
                    }
                }
            }
            // Check class body declarations
            else if (obj instanceof TypeDeclaration) {
                TypeDeclaration typeDecl = (TypeDeclaration) obj;
                for (Object bodyObj : typeDecl.bodyDeclarations()) {
                    if (bodyObj instanceof EnumDeclaration) {
                        EnumDeclaration enumDecl = (EnumDeclaration) bodyObj;
                        if (enumDecl.getName().getIdentifier().equals(enumName)) {
                            if (!enumDecl.enumConstants().isEmpty()) {
                                EnumConstantDeclaration first = (EnumConstantDeclaration) enumDecl.enumConstants().get(0);
                                return first.getName().getIdentifier();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public String addElementToList(CompilationUnit cu, String originalCode, ASTNode listNode, AddableExpression type, int insertIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newElement = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newElement == null) return originalCode;
        if (listNode instanceof ArrayInitializer) {
            rewriter.getListRewrite(listNode, ArrayInitializer.EXPRESSIONS_PROPERTY).insertAt(newElement, insertIndex, null);
        } else if (listNode instanceof MethodInvocation) {
            rewriter.getListRewrite(listNode, MethodInvocation.ARGUMENTS_PROPERTY).insertAt(newElement, insertIndex, null);
        } else if (listNode instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) listNode;
            if (!cic.arguments().isEmpty() && cic.arguments().get(0) instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) cic.arguments().get(0);
                rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY).insertAt(newElement, insertIndex, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteElementFromList(CompilationUnit cu, String originalCode, ASTNode listNode, int elementIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<?> expressions;
        ChildListPropertyDescriptor property;
        ASTNode targetNode = listNode;
        if (listNode instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) listNode;
            if (!cic.arguments().isEmpty() && cic.arguments().get(0) instanceof MethodInvocation) {
                MethodInvocation mi = (MethodInvocation) cic.arguments().get(0);
                targetNode = mi;
                expressions = mi.arguments();
                property = MethodInvocation.ARGUMENTS_PROPERTY;
            } else return originalCode;
        } else if (listNode instanceof ArrayInitializer) {
            expressions = ((ArrayInitializer) listNode).expressions();
            property = ArrayInitializer.EXPRESSIONS_PROPERTY;
        } else if (listNode instanceof MethodInvocation) {
            expressions = ((MethodInvocation) listNode).arguments();
            property = MethodInvocation.ARGUMENTS_PROPERTY;
        } else return originalCode;
        if (elementIndex >= 0 && elementIndex < expressions.size()) {
            rewriter.getListRewrite(targetNode, property).remove((ASTNode) expressions.get(elementIndex), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String convertElseToElseIf(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement elseStatement = ifStatement.getElseStatement();
        if (elseStatement != null && elseStatement.getNodeType() == ASTNode.BLOCK) {
            IfStatement newElseIf = ast.newIfStatement();
            newElseIf.setExpression(ast.newBooleanLiteral(true));
            newElseIf.setThenStatement((Block) ASTNode.copySubtree(ast, elseStatement));
            rewriter.replace(elseStatement, newElseIf, null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String addElseToIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        if (ifStatement.getElseStatement() == null) rewriter.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, ast.newBlock(), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteElseFromIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        if (ifStatement.getElseStatement() != null) rewriter.remove(ifStatement.getElseStatement(), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replaceAssignmentOperator(CompilationUnit cu, String originalCode, Assignment assignment, Assignment.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Assignment newAssignment = ast.newAssignment();
        newAssignment.setLeftHandSide((Expression) ASTNode.copySubtree(ast, assignment.getLeftHandSide()));
        newAssignment.setRightHandSide((Expression) ASTNode.copySubtree(ast, assignment.getRightHandSide()));
        newAssignment.setOperator(newOp);
        rewriter.replace(assignment, newAssignment, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replacePrefixOperator(CompilationUnit cu, String originalCode, PrefixExpression prefix, PrefixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        PrefixExpression newPrefix = ast.newPrefixExpression();
        newPrefix.setOperand((Expression) ASTNode.copySubtree(ast, prefix.getOperand()));
        newPrefix.setOperator(newOp);
        rewriter.replace(prefix, newPrefix, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String replacePostfixOperator(CompilationUnit cu, String originalCode, PostfixExpression postfix, PostfixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        PostfixExpression newPostfix = ast.newPostfixExpression();
        newPostfix.setOperand((Expression) ASTNode.copySubtree(ast, postfix.getOperand()));
        newPostfix.setOperator(newOp);
        rewriter.replace(postfix, newPostfix, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String updateMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, String newScope, String newMethodName, List<String> newParamTypes) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        if (newScope == null || newScope.isEmpty() || newScope.equals("Local")) {
            if (mi.getExpression() != null) rewriter.remove(mi.getExpression(), null);
        } else {
            SimpleName newScopeNode = ast.newSimpleName(newScope);
            if (mi.getExpression() == null) rewriter.set(mi, MethodInvocation.EXPRESSION_PROPERTY, newScopeNode, null);
            else rewriter.replace(mi.getExpression(), newScopeNode, null);
        }
        if (!mi.getName().getIdentifier().equals(newMethodName)) rewriter.replace(mi.getName(), ast.newSimpleName(newMethodName), null);
        ListRewrite argsRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        List<?> currentArgs = mi.arguments();
        int targetCount = newParamTypes.size();
        int currentCount = currentArgs.size();
        if (currentCount > targetCount) {
            for (int i = currentCount - 1; i >= targetCount; i--) argsRewrite.remove((ASTNode) currentArgs.get(i), null);
        } else if (currentCount < targetCount) {
            for (int i = currentCount; i < targetCount; i++) {
                String typeName = newParamTypes.get(i);
                Expression defaultExpr = nodeCreator.createDefaultInitializer(ast, typeName);
                argsRewrite.insertLast(defaultExpr, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
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
        return applyRewrite(rewriter, originalCode);
    }

    public String setMethodReturnType(CompilationUnit cu, String originalCode, MethodDeclaration method, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Type newType;
        if ("void".equals(newTypeName)) newType = ast.newPrimitiveType(PrimitiveType.VOID);
        else newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(method.getReturnType2(), newType, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String addParameterToMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, String typeName, String paramName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
        SingleVariableDeclaration newParam = ast.newSingleVariableDeclaration();
        newParam.setType(TypeManager.createTypeNode(ast, typeName));
        newParam.setName(ast.newSimpleName(paramName));
        listRewrite.insertLast(newParam, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteParameterFromMethod(CompilationUnit cu, String originalCode, MethodDeclaration method, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(method, MethodDeclaration.PARAMETERS_PROPERTY);
        List<?> params = method.parameters();
        if (index >= 0 && index < params.size()) listRewrite.remove((ASTNode) params.get(index), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String setReturnExpression(CompilationUnit cu, String originalCode, ReturnStatement returnStmt, AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Expression newExpr = nodeCreator.createDefaultExpression(ast, type, cu, rewriter);
        if (newExpr == null) return originalCode;
        if (returnStmt.getExpression() == null) rewriter.set(returnStmt, ReturnStatement.EXPRESSION_PROPERTY, newExpr, null);
        else rewriter.replace(returnStmt.getExpression(), newExpr, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String addEnumToClass(CompilationUnit cu, String originalCode, TypeDeclaration typeDecl, String enumName, int index) {
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

        return applyRewrite(rewriter, originalCode);
    }

    public String deleteEnumFromClass(CompilationUnit cu, String originalCode, EnumDeclaration enumDecl) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(enumDecl, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String renameEnum(CompilationUnit cu, String originalCode, EnumDeclaration enumNode, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        rewriter.replace(enumNode.getName(), ast.newSimpleName(newName), null);

        return applyRewrite(rewriter, originalCode);
    }

    public String addEnumConstant(CompilationUnit cu, String originalCode, EnumDeclaration enumNode, String constantName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(enumNode, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);

        EnumConstantDeclaration newConst = ast.newEnumConstantDeclaration();
        newConst.setName(ast.newSimpleName(constantName));

        listRewrite.insertLast(newConst, null);
        return applyRewrite(rewriter, originalCode);
    }

    public String deleteEnumConstant(CompilationUnit cu, String originalCode, EnumDeclaration enumNode, int index) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        ListRewrite listRewrite = rewriter.getListRewrite(enumNode, EnumDeclaration.ENUM_CONSTANTS_PROPERTY);
        List<?> constants = enumNode.enumConstants();

        if (index >= 0 && index < constants.size()) {
            listRewrite.remove((ASTNode) constants.get(index), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String renameEnumConstant(CompilationUnit cu, String originalCode, EnumDeclaration enumNode, int index, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<?> constants = enumNode.enumConstants();

        if (index >= 0 && index < constants.size()) {
            EnumConstantDeclaration constDecl = (EnumConstantDeclaration) constants.get(index);
            rewriter.replace(constDecl.getName(), ast.newSimpleName(newName), null);
        }
        return applyRewrite(rewriter, originalCode);
    }

    public String addCaseToSwitch(CompilationUnit cu, String originalCode, SwitchStatement switchStmt) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(switchStmt, SwitchStatement.STATEMENTS_PROPERTY);
        SwitchCase newCase = ast.newSwitchCase();
        int count = 0;
        for(Object o : switchStmt.statements()) { if(o instanceof SwitchCase) count++; }
        try { newCase.expressions().add(ast.newNumberLiteral(String.valueOf(count))); } catch(Exception ignored) {}
        listRewrite.insertLast(newCase, null);
        listRewrite.insertLast(ast.newBreakStatement(), null);
        return applyRewrite(rewriter, originalCode);
    }

    public String moveSwitchCase(CompilationUnit cu, String originalCode, SwitchCase caseNode, boolean moveUp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        SwitchStatement parent = (SwitchStatement) caseNode.getParent();
        List<Statement> statements = parent.statements();
        List<List<Statement>> chunks = new ArrayList<>();
        List<Statement> currentChunk = null;
        for (Statement stmt : statements) {
            if (stmt instanceof SwitchCase) {
                if (currentChunk != null) chunks.add(currentChunk);
                currentChunk = new ArrayList<>();
            }
            if (currentChunk != null) currentChunk.add(stmt);
        }
        if (currentChunk != null) chunks.add(currentChunk);
        int targetIndex = -1;
        for (int i = 0; i < chunks.size(); i++) {
            if (!chunks.get(i).isEmpty() && chunks.get(i).get(0) == caseNode) {
                targetIndex = i;
                break;
            }
        }
        if (targetIndex == -1) return originalCode;
        int neighborIndex = moveUp ? targetIndex - 1 : targetIndex + 1;
        if (neighborIndex < 0 || neighborIndex >= chunks.size()) return originalCode;
        List<Statement> targetChunk = chunks.get(targetIndex);
        List<Statement> neighborChunk = chunks.get(neighborIndex);
        ListRewrite listRewrite = rewriter.getListRewrite(parent, SwitchStatement.STATEMENTS_PROPERTY);
        if (moveUp) {
            ASTNode insertPoint = neighborChunk.get(0);
            for (Statement stmt : targetChunk) {
                ASTNode moveTarget = rewriter.createMoveTarget(stmt);
                listRewrite.insertBefore(moveTarget, insertPoint, null);
            }
        } else {
            ASTNode insertPoint = targetChunk.get(0);
            for (Statement stmt : neighborChunk) {
                ASTNode moveTarget = rewriter.createMoveTarget(stmt);
                listRewrite.insertBefore(moveTarget, insertPoint, null);
            }
        }
        return applyRewrite(rewriter, originalCode);
    }

    private ListRewrite getListRewriteForBody(ASTRewrite rewriter, BodyBlock body) {
        ASTNode node = body.getAstNode();
        if (node instanceof Block) {
            return rewriter.getListRewrite(node, Block.STATEMENTS_PROPERTY);
        } else if (node instanceof SwitchCase) {
            return rewriter.getListRewrite(node.getParent(), SwitchStatement.STATEMENTS_PROPERTY);
        }
        throw new IllegalArgumentException("Unsupported body node type: " + node.getClass());
    }

    private void insertIntoList(ListRewrite listRewrite, BodyBlock body, Statement newStatement, int relativeIndex) {
        ASTNode node = body.getAstNode();
        if (node instanceof Block) {
            listRewrite.insertAt(newStatement, relativeIndex, null);
        } else if (node instanceof SwitchCase) {
            SwitchCase caseNode = (SwitchCase) node;
            SwitchStatement parent = (SwitchStatement) caseNode.getParent();
            List<?> allStatements = parent.statements();
            int caseIndex = allStatements.indexOf(caseNode);
            int absoluteIndex = caseIndex + 1 + relativeIndex;
            listRewrite.insertAt(newStatement, absoluteIndex, null);
        }
    }

    private String applyRewrite(ASTRewrite rewriter, String originalCode) {
        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) { e.printStackTrace(); return originalCode; }
    }

    private void collectLeafValues(Expression expr, List<Expression> accumulator) {
        if (expr == null) return;
        boolean isContainer = false;
        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            if (cic.getType().toString().startsWith("ArrayList") && !cic.arguments().isEmpty()) {
                isContainer = true;
                collectLeafValues((Expression) cic.arguments().get(0), accumulator);
            }
        } else if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            String name = mi.getName().getIdentifier();
            if (name.equals("asList") || name.equals("of")) {
                isContainer = true;
                for (Object arg : mi.arguments()) collectLeafValues((Expression) arg, accumulator);
            }
        } else if (expr instanceof ArrayInitializer) {
            isContainer = true;
            for (Object e : ((ArrayInitializer) expr).expressions()) collectLeafValues((Expression) e, accumulator);
        } else if (expr instanceof ArrayCreation) {
            isContainer = true;
            if (((ArrayCreation) expr).getInitializer() != null) collectLeafValues(((ArrayCreation) expr).getInitializer(), accumulator);
        }
        if (!isContainer) accumulator.add(expr);
    }
}