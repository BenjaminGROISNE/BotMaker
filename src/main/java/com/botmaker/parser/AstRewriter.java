package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class AstRewriter {

    /**
     * Moves a statement from one position to another, potentially across different bodies.
     * @param cu The compilation unit
     * @param originalCode The current source code
     * @param blockToMove The StatementBlock to move
     * @param sourceBody The BodyBlock containing the statement (can be same as targetBody)
     * @param targetBody The BodyBlock where the statement should be moved to
     * @param targetIndex The index in the target body where the statement should be inserted
     * @return The updated source code
     */
    public String moveStatement(CompilationUnit cu, String originalCode,
                                StatementBlock blockToMove, BodyBlock sourceBody,
                                BodyBlock targetBody, int targetIndex) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement statement = (Statement) blockToMove.getAstNode();
        Block sourceBlock = (Block) sourceBody.getAstNode();
        Block targetBlock = (Block) targetBody.getAstNode();

        // Get the list rewriters for both source and target
        ListRewrite sourceListRewrite = rewriter.getListRewrite(sourceBlock, Block.STATEMENTS_PROPERTY);
        ListRewrite targetListRewrite = rewriter.getListRewrite(targetBlock, Block.STATEMENTS_PROPERTY);

        // --- FIX START ---
        // JDT ASTRewrite handles indices based on the ORIGINAL AST.
        // We do NOT need to manually adjust the index for the removal shift
        // because the removal hasn't actually happened to the underlying list yet.

        // If moving to the exact same position (same body, same index), do nothing
        if (sourceBody == targetBody) {
            int currentIndex = sourceBlock.statements().indexOf(statement);
            if (currentIndex == targetIndex) {
                return originalCode;
            }
            // The logic "if (targetIndex > currentIndex) targetIndex--;" was removed here.
        }
        // --- FIX END ---

        // Create a copy of the statement for the new location
        Statement copiedStatement = (Statement) ASTNode.copySubtree(ast, statement);

        // Remove from source and insert at target
        sourceListRewrite.remove(statement, null);
        targetListRewrite.insertAt(copiedStatement, targetIndex, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String addStatement(CompilationUnit cu, String originalCode, BodyBlock targetBody, AddableBlock type, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement newStatement = createDefaultStatement(ast, type);
        if (newStatement == null) {
            return originalCode;
        }

        Block targetAstBlock = (Block) targetBody.getAstNode();
        ListRewrite listRewrite = rewriter.getListRewrite(targetAstBlock, Block.STATEMENTS_PROPERTY);
        listRewrite.insertAt(newStatement, index, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String replaceExpression(CompilationUnit cu, String originalCode, Expression toReplace, com.botmaker.ui.AddableExpression type) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Expression newExpression = createDefaultExpression(ast, type);
        if (newExpression == null) {
            return originalCode;
        }

        rewriter.replace(toReplace, newExpression, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
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

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String addArgumentToMethodInvocation(CompilationUnit cu, String originalCode, MethodInvocation mi, Expression newArgument) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        ListRewrite listRewrite = rewriter.getListRewrite(mi, MethodInvocation.ARGUMENTS_PROPERTY);
        listRewrite.insertLast(newArgument, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String replaceSimpleName(CompilationUnit cu, String originalCode, SimpleName toReplace, String newName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        SimpleName newSimpleName = ast.newSimpleName(newName);
        rewriter.replace(toReplace, newSimpleName, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String deleteNode(CompilationUnit cu, String originalCode, ASTNode toDelete) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        rewriter.remove(toDelete, null);

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String deleteElseFromIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
        if (ifStatement.getElseStatement() != null) {
            rewriter.remove(ifStatement.getElseStatement(), null);
        } else {
            return originalCode;
        }
        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String convertElseToElseIf(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        Statement elseStatement = ifStatement.getElseStatement();
        if (elseStatement == null || elseStatement.getNodeType() != ASTNode.BLOCK) {
            return originalCode;
        }
        IfStatement newElseIf = ast.newIfStatement();
        newElseIf.setExpression(ast.newBooleanLiteral(true));
        newElseIf.setThenStatement((Block) ASTNode.copySubtree(ast, elseStatement));
        rewriter.replace(elseStatement, newElseIf, null);
        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    public String addElseToIfStatement(CompilationUnit cu, String originalCode, IfStatement ifStatement) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (ifStatement.getElseStatement() == null) {
            Block elseBlock = ast.newBlock();
            rewriter.set(ifStatement, IfStatement.ELSE_STATEMENT_PROPERTY, elseBlock, null);
        } else {
            return originalCode;
        }

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    private Expression createDefaultExpression(AST ast, com.botmaker.ui.AddableExpression type) {
        switch (type) {
            case TEXT:
                StringLiteral newString = ast.newStringLiteral();
                newString.setLiteralValue("text");
                return newString;

            case NUMBER:
                return ast.newNumberLiteral("0");

            case VARIABLE:
                return ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE);

            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                // Create a binary expression: variable <op> 0
                InfixExpression infixExpr = ast.newInfixExpression();
                infixExpr.setLeftOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                infixExpr.setRightOperand(ast.newNumberLiteral("0"));

                // Set the operator based on type
                switch (type) {
                    case ADD:
                        infixExpr.setOperator(InfixExpression.Operator.PLUS);
                        break;
                    case SUBTRACT:
                        infixExpr.setOperator(InfixExpression.Operator.MINUS);
                        break;
                    case MULTIPLY:
                        infixExpr.setOperator(InfixExpression.Operator.TIMES);
                        break;
                    case DIVIDE:
                        infixExpr.setOperator(InfixExpression.Operator.DIVIDE);
                        break;
                    case MODULO:
                        infixExpr.setOperator(InfixExpression.Operator.REMAINDER);
                        break;
                }
                return infixExpr;

            default:
                return null;
        }
    }

    private Statement createDefaultStatement(AST ast, AddableBlock type) {
        switch (type) {
            case PRINT:
                // System.out.println("");
                MethodInvocation println = ast.newMethodInvocation();
                println.setExpression(ast.newQualifiedName(
                        ast.newSimpleName("System"),
                        ast.newSimpleName("out"))
                );
                println.setName(ast.newSimpleName("println"));
                StringLiteral emptyString = ast.newStringLiteral();
                emptyString.setLiteralValue("");
                println.arguments().add(emptyString);
                return ast.newExpressionStatement(println);

            case DECLARE_INT: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName(DefaultNames.DEFAULT_INT));
                fragment.setInitializer(ast.newNumberLiteral("0"));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.INT));
                return varDecl;
            }

            case DECLARE_DOUBLE: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName(DefaultNames.DEFAULT_DOUBLE));
                fragment.setInitializer(ast.newNumberLiteral("0.0"));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
                return varDecl;
            }

            case DECLARE_BOOLEAN: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName(DefaultNames.DEFAULT_BOOLEAN));
                fragment.setInitializer(ast.newBooleanLiteral(false));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
                return varDecl;
            }

            case DECLARE_STRING: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName(DefaultNames.DEFAULT_STRING));
                fragment.setInitializer(ast.newStringLiteral());
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(TypeManager.createTypeNode(ast, "String"));
                return varDecl;
            }

            case IF:
                // if (true) {}
                IfStatement ifStatement = ast.newIfStatement();
                ifStatement.setExpression(ast.newBooleanLiteral(true));
                ifStatement.setThenStatement(ast.newBlock());
                return ifStatement;

            case WHILE:
                // while (true) {}
                WhileStatement whileStatement = ast.newWhileStatement();
                whileStatement.setExpression(ast.newBooleanLiteral(true));
                whileStatement.setBody(ast.newBlock());
                return whileStatement;

            case FOR:
                // for (int i = 0; i < 10; i++) {}
                ForStatement forStatement = ast.newForStatement();

                // Initialization: int i = 0
                VariableDeclarationFragment initFragment = ast.newVariableDeclarationFragment();
                initFragment.setName(ast.newSimpleName("i"));
                initFragment.setInitializer(ast.newNumberLiteral("0"));
                VariableDeclarationExpression initExpr = ast.newVariableDeclarationExpression(initFragment);
                initExpr.setType(ast.newPrimitiveType(PrimitiveType.INT));
                forStatement.initializers().add(initExpr);

                // Condition: i < 10
                InfixExpression condition = ast.newInfixExpression();
                condition.setLeftOperand(ast.newSimpleName("i"));
                condition.setOperator(InfixExpression.Operator.LESS);
                condition.setRightOperand(ast.newNumberLiteral("10"));
                forStatement.setExpression(condition);

                // Update: i++
                PostfixExpression update = ast.newPostfixExpression();
                update.setOperand(ast.newSimpleName("i"));
                update.setOperator(PostfixExpression.Operator.INCREMENT);
                forStatement.updaters().add(update);

                // Body
                forStatement.setBody(ast.newBlock());
                return forStatement;

            case BREAK:
                return ast.newBreakStatement();

            case CONTINUE:
                return ast.newContinueStatement();

            case ASSIGNMENT: {
                // variable = 0
                Assignment assignment = ast.newAssignment();
                assignment.setLeftHandSide(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                assignment.setOperator(Assignment.Operator.ASSIGN);
                assignment.setRightHandSide(ast.newNumberLiteral("0"));
                return ast.newExpressionStatement(assignment);
            }

            case INCREMENT: {
                // variable++
                PostfixExpression postfix = ast.newPostfixExpression();
                postfix.setOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                postfix.setOperator(PostfixExpression.Operator.INCREMENT);
                return ast.newExpressionStatement(postfix);
            }

            case DECREMENT: {
                // variable--
                PostfixExpression postfix = ast.newPostfixExpression();
                postfix.setOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                postfix.setOperator(PostfixExpression.Operator.DECREMENT);
                return ast.newExpressionStatement(postfix);
            }

            case READ_LINE: {
                // String input = scanner.nextLine()
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("input"));

                MethodInvocation scannerCall = ast.newMethodInvocation();
                scannerCall.setExpression(ast.newSimpleName("scanner"));
                scannerCall.setName(ast.newSimpleName("nextLine"));
                fragment.setInitializer(scannerCall);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(TypeManager.createTypeNode(ast, "String"));
                return varDecl;
            }

            case READ_INT: {
                // int num = scanner.nextInt()
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("num"));

                MethodInvocation scannerCall = ast.newMethodInvocation();
                scannerCall.setExpression(ast.newSimpleName("scanner"));
                scannerCall.setName(ast.newSimpleName("nextInt"));
                fragment.setInitializer(scannerCall);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.INT));
                return varDecl;
            }

            case READ_DOUBLE: {
                // double num = scanner.nextDouble()
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("num"));

                MethodInvocation scannerCall = ast.newMethodInvocation();
                scannerCall.setExpression(ast.newSimpleName("scanner"));
                scannerCall.setName(ast.newSimpleName("nextDouble"));
                fragment.setInitializer(scannerCall);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
                return varDecl;
            }

            default:
                return null;
        }
    }

    private Expression createDefaultInitializer(AST ast, String typeName) {
        switch (typeName) {
            case "int":
            case "long":
            case "short":
            case "byte":
                return ast.newNumberLiteral("0");
            case "double":
            case "float":
                return ast.newNumberLiteral("0.0");
            case "boolean":
                return ast.newBooleanLiteral(false);
            case "char":
                CharacterLiteral literal = ast.newCharacterLiteral();
                literal.setCharValue('a');
                return literal;
            case "String":
                StringLiteral stringLiteral = ast.newStringLiteral();
                stringLiteral.setLiteralValue("");
                return stringLiteral;
            default:
                return ast.newNullLiteral();
        }
    }

    public String replaceVariableType(CompilationUnit cu, String originalCode, VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = createDefaultInitializer(ast, newTypeName);

            if (currentInitializer != null && newInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }

        IDocument document = new Document(originalCode);
        try {
            TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }
}