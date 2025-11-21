package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.List;

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

        // If moving to the exact same position (same body, same index), do nothing
        if (sourceBody == targetBody) {
            int currentIndex = sourceBlock.statements().indexOf(statement);
            if (currentIndex == targetIndex) {
                return originalCode;
            }
        }

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

        if (type == AddableBlock.COMMENT) {
            // SPECIAL HANDLING FOR COMMENTS
            // We use a String Placeholder. It is not a real statement, but JDT ListRewrite accepts it.
            // ASTNode.EMPTY_STATEMENT is passed as a type hint for formatting.
            Statement commentPlaceholder = (Statement) rewriter.createStringPlaceholder("// Comment", ASTNode.EMPTY_STATEMENT);

            Block targetAstBlock = (Block) targetBody.getAstNode();
            ListRewrite listRewrite = rewriter.getListRewrite(targetAstBlock, Block.STATEMENTS_PROPERTY);
            listRewrite.insertAt(commentPlaceholder, index, null);
        } else {
            // Standard Statement Handling
            Statement newStatement = createDefaultStatement(ast, type);
            if (newStatement == null) return originalCode;

            Block targetAstBlock = (Block) targetBody.getAstNode();
            ListRewrite listRewrite = rewriter.getListRewrite(targetAstBlock, Block.STATEMENTS_PROPERTY);
            listRewrite.insertAt(newStatement, index, null);
        }

        return applyRewrite(rewriter, originalCode);
    }

    private String applyRewrite(ASTRewrite rewriter, String originalCode) {
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

    public String updateComment(String originalCode, Comment commentNode, String newText) {
        try {
            IDocument document = new Document(originalCode);

            String replacement;
            if (newText.contains("\n")) {
                // Use Block Comment for multi-line text
                replacement = "/* " + newText + " */";
            } else {
                // Use Line Comment for single-line text
                replacement = "// " + newText;
            }

            // Directly replace the text range in the document
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), replacement);

            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    /**
     * Deletes a comment node by removing its text range.
     */
    public String deleteComment(String originalCode, Comment commentNode) {
        try {
            IDocument document = new Document(originalCode);
            // Replace content with empty string
            document.replace(commentNode.getStartPosition(), commentNode.getLength(), "");
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

    // ADD THIS METHOD TO AstRewriter.java - replaces the existing createDefaultExpression

    private Expression createDefaultExpression(AST ast, AddableExpression type) {
        switch (type) {
            case TEXT:
                StringLiteral newString = ast.newStringLiteral();
                newString.setLiteralValue("text");
                return newString;

            case NUMBER:
                return ast.newNumberLiteral("0");

            case TRUE:
                return ast.newBooleanLiteral(true);

            case FALSE:
                return ast.newBooleanLiteral(false);

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
            case DECLARE_INT_ARRAY: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("numbers"));

                // Create empty array initializer: new int[]{0, 0, 0}
                ArrayInitializer initializer = ast.newArrayInitializer();
                initializer.expressions().add(ast.newNumberLiteral("0"));
                initializer.expressions().add(ast.newNumberLiteral("0"));
                initializer.expressions().add(ast.newNumberLiteral("0"));

                ArrayCreation arrayCreation = ast.newArrayCreation();
                arrayCreation.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT)));
                arrayCreation.setInitializer(initializer);

                fragment.setInitializer(arrayCreation);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.INT)));
                return varDecl;
            }

            case DECLARE_DOUBLE_ARRAY: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("decimals"));

                ArrayInitializer initializer = ast.newArrayInitializer();
                initializer.expressions().add(ast.newNumberLiteral("0.0"));
                initializer.expressions().add(ast.newNumberLiteral("0.0"));
                initializer.expressions().add(ast.newNumberLiteral("0.0"));

                ArrayCreation arrayCreation = ast.newArrayCreation();
                arrayCreation.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.DOUBLE)));
                arrayCreation.setInitializer(initializer);

                fragment.setInitializer(arrayCreation);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.DOUBLE)));
                return varDecl;
            }

            case DECLARE_STRING_ARRAY: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("words"));

                ArrayInitializer initializer = ast.newArrayInitializer();
                StringLiteral str1 = ast.newStringLiteral();
                str1.setLiteralValue("item1");
                StringLiteral str2 = ast.newStringLiteral();
                str2.setLiteralValue("item2");
                StringLiteral str3 = ast.newStringLiteral();
                str3.setLiteralValue("item3");

                initializer.expressions().add(str1);
                initializer.expressions().add(str2);
                initializer.expressions().add(str3);

                ArrayCreation arrayCreation = ast.newArrayCreation();
                arrayCreation.setType(ast.newArrayType(TypeManager.createTypeNode(ast, "String")));
                arrayCreation.setInitializer(initializer);

                fragment.setInitializer(arrayCreation);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newArrayType(TypeManager.createTypeNode(ast, "String")));
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
                // Enhanced for loop (foreach): for(String item : array) {}
                EnhancedForStatement enhancedFor = ast.newEnhancedForStatement();

                // Parameter: String item
                SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
                parameter.setType(TypeManager.createTypeNode(ast, "String"));
                parameter.setName(ast.newSimpleName("item"));
                enhancedFor.setParameter(parameter);

                // Expression: array (or any collection)
                enhancedFor.setExpression(ast.newSimpleName("array"));

                // Body
                enhancedFor.setBody(ast.newBlock());

                return enhancedFor;

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

            case DO_WHILE:
                // do { } while (true)
                DoStatement doStatement = ast.newDoStatement();
                doStatement.setExpression(ast.newBooleanLiteral(true));
                doStatement.setBody(ast.newBlock());
                return doStatement;

            case SWITCH:
                // switch (variable) { default: break; }
                SwitchStatement switchStatement = ast.newSwitchStatement();
                switchStatement.setExpression(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));

                // Add a default case
                SwitchCase defaultCase = ast.newSwitchCase();
                // In JDT AST, a SwitchCase with empty/null expression is treated as 'default'.
                // We do NOT call setDefault(true) as it doesn't exist in modern AST.

                switchStatement.statements().add(defaultCase);

                BreakStatement breakStmt = ast.newBreakStatement();
                switchStatement.statements().add(breakStmt);

                return switchStatement;

            case CASE:
                // case 0:
                SwitchCase switchCase = ast.newSwitchCase();
                // We assume JDK 8+ style (case expression:)
                // In newer JDT versions, we use expressions().add()
                try {
                    // Using reflection to handle different JDT versions if necessary,
                    // but assuming we are on a modern version based on AST.getJLSLatest()
                    switchCase.expressions().add(ast.newNumberLiteral("0"));
                } catch (Exception e) {
                    // Fallback for older JDT where setExpression is used
                    // switchCase.setExpression(ast.newNumberLiteral("0"));
                }
                return switchCase;

            case RETURN:
                // return;
                return ast.newReturnStatement();

            case WAIT:
                // try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
                TryStatement tryStmt = ast.newTryStatement();
                Block tryBody = ast.newBlock();

                // Thread.sleep(1000)
                MethodInvocation sleepCall = ast.newMethodInvocation();
                sleepCall.setExpression(ast.newSimpleName("Thread"));
                sleepCall.setName(ast.newSimpleName("sleep"));
                sleepCall.arguments().add(ast.newNumberLiteral("1000"));

                tryBody.statements().add(ast.newExpressionStatement(sleepCall));
                tryStmt.setBody(tryBody);

                // catch (InterruptedException e)
                CatchClause catchClause = ast.newCatchClause();
                SingleVariableDeclaration exceptionDecl = ast.newSingleVariableDeclaration();
                exceptionDecl.setType(ast.newSimpleType(ast.newSimpleName("InterruptedException")));
                exceptionDecl.setName(ast.newSimpleName("e"));
                catchClause.setException(exceptionDecl);

                // e.printStackTrace()
                Block catchBody = ast.newBlock();
                MethodInvocation printStackTrace = ast.newMethodInvocation();
                printStackTrace.setExpression(ast.newSimpleName("e"));
                printStackTrace.setName(ast.newSimpleName("printStackTrace"));
                catchBody.statements().add(ast.newExpressionStatement(printStackTrace));
                catchClause.setBody(catchBody);

                tryStmt.catchClauses().add(catchClause);
                return tryStmt;
            case COMMENT:
                // Comments are special - they're not real statements
                // We'll use an EmptyStatement as a placeholder
                // The actual comment text is stored in the CommentBlock
                EmptyStatement emptyStmt = ast.newEmptyStatement();
                return emptyStmt;

            default:
                return null;
        }
    }

    public String addElementToArrayInitializer(
            CompilationUnit cu,
            String originalCode,
            org.eclipse.jdt.core.dom.ArrayInitializer arrayInit,
            com.botmaker.ui.AddableExpression type,
            int insertIndex) {

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Create the new expression based on type
        Expression newElement = createDefaultExpression(ast, type);
        if (newElement == null) {
            return originalCode;
        }

        // Use ListRewrite to insert the element
        ListRewrite listRewrite = rewriter.getListRewrite(
                arrayInit,
                org.eclipse.jdt.core.dom.ArrayInitializer.EXPRESSIONS_PROPERTY
        );

        listRewrite.insertAt(newElement, insertIndex, null);

        return applyRewrite(rewriter, originalCode);
    }

    /**
     * Deletes an element from an ArrayInitializer at the specified index
     */
    public String deleteElementFromArrayInitializer(
            CompilationUnit cu,
            String originalCode,
            org.eclipse.jdt.core.dom.ArrayInitializer arrayInit,
            int elementIndex) {

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Get the list of expressions
        @SuppressWarnings("unchecked")
        List<Expression> expressions = arrayInit.expressions();

        if (elementIndex < 0 || elementIndex >= expressions.size()) {
            return originalCode; // Invalid index
        }

        Expression toRemove = expressions.get(elementIndex);

        // Use ListRewrite to remove the element
        ListRewrite listRewrite = rewriter.getListRewrite(
                arrayInit,
                org.eclipse.jdt.core.dom.ArrayInitializer.EXPRESSIONS_PROPERTY
        );

        listRewrite.remove(toRemove, null);

        return applyRewrite(rewriter, originalCode);
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