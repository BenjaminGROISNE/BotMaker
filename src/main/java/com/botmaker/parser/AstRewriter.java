package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.ui.AddableExpression;
import com.botmaker.util.DefaultNames;
import com.botmaker.util.TypeManager;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.botmaker.util.TypeManager.toWrapperType;

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
            Statement commentPlaceholder = (Statement) rewriter.createStringPlaceholder("// Comment", ASTNode.EMPTY_STATEMENT);
            Block targetAstBlock = (Block) targetBody.getAstNode();
            ListRewrite listRewrite = rewriter.getListRewrite(targetAstBlock, Block.STATEMENTS_PROPERTY);
            listRewrite.insertAt(commentPlaceholder, index, null);
        } else {
            // PASS CU AND REWRITER HERE
            Statement newStatement = createDefaultStatement(ast, type, cu, rewriter);
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

        Expression newExpression = createDefaultExpression(ast, type, cu, rewriter);
        if (newExpression == null) {
            return originalCode;
        }

        rewriter.replace(toReplace, newExpression, null);
        return applyRewrite(rewriter, originalCode);
    }


    private Expression createRecursiveListInitializer(AST ast, String typeName, CompilationUnit cu, ASTRewrite rewriter, List<Expression> leavesToPreserve) {
        ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
        ImportManager.addImport(cu, rewriter, "java.util.Arrays");

        // new ArrayList<...>()
        ClassInstanceCreation creation = ast.newClassInstanceCreation();
        String innerTypeStr = extractArrayListElementType(typeName);
        String wrapperInnerType = toWrapperType(innerTypeStr);

        ParameterizedType paramType = ast.newParameterizedType(ast.newSimpleType(ast.newName("ArrayList")));

        if (!innerTypeStr.equals("Object")) {
            paramType.typeArguments().add(TypeManager.createTypeNode(ast, wrapperInnerType));
        }
        creation.setType(paramType);

        // Arrays.asList(...)
        MethodInvocation asList = ast.newMethodInvocation();
        asList.setExpression(ast.newSimpleName("Arrays"));
        asList.setName(ast.newSimpleName("asList"));

        if (innerTypeStr.startsWith("ArrayList<")) {
            // RECURSION (Deepening):
            // If we are adding layers (e.g., List<Int> -> List<List<Int>>),
            // we put ALL existing leaves into the *first* new inner list.
            // Example: [1, 2] -> [[1, 2]]

            // We pass 'leavesToPreserve' down to the next level.
            // However, if we passed it directly, the recursive call would consume them.
            // If we wanted to split them (e.g. [[1], [2]]), that logic would be very complex.
            // Wrapping them all in one list is the safest default.

            Expression innerList = createRecursiveListInitializer(ast, innerTypeStr, cu, rewriter, leavesToPreserve);
            asList.arguments().add(innerList);

            // Crucial: Set leavesToPreserve to null/empty for subsequent calls if we were looping,
            // but here we create exactly one inner list, so it handles all data.
        } else {
            // BASE CASE (Bottom Layer):
            // Dump all collected values here.
            if (leavesToPreserve != null && !leavesToPreserve.isEmpty()) {
                for (Expression leaf : leavesToPreserve) {
                    asList.arguments().add((Expression) ASTNode.copySubtree(ast, leaf));
                }
            } else {
                // Only add default 0 if we have absolutely no data
                Expression defaultValue = createDefaultInitializer(ast, innerTypeStr);
                asList.arguments().add(defaultValue);
            }
        }

        creation.arguments().add(asList);
        return creation;
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
    // UPDATED: Handles deletion for both types
    public String deleteElementFromList(
            CompilationUnit cu,
            String originalCode,
            ASTNode listNode,
            int elementIndex) {

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);
        List<Expression> expressions;
        ChildListPropertyDescriptor property;

        if (listNode instanceof ArrayInitializer) {
            expressions = ((ArrayInitializer) listNode).expressions();
            property = ArrayInitializer.EXPRESSIONS_PROPERTY;
        } else if (listNode instanceof MethodInvocation) {
            expressions = ((MethodInvocation) listNode).arguments();
            property = MethodInvocation.ARGUMENTS_PROPERTY;
        } else {
            return originalCode;
        }

        if (elementIndex < 0 || elementIndex >= expressions.size()) {
            return originalCode;
        }

        Expression toRemove = expressions.get(elementIndex);
        rewriter.getListRewrite(listNode, property).remove(toRemove, null);

        return applyRewrite(rewriter, originalCode);
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

    private Expression createDefaultExpression(AST ast, AddableExpression type, CompilationUnit cu, ASTRewrite rewriter) {
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

            case LIST:
                // Create: Arrays.asList()
                // This is the versatile list expression used for ArrayLists and Nested Lists
                ImportManager.addImport(cu, rewriter, "java.util.Arrays");

                MethodInvocation asList = ast.newMethodInvocation();
                asList.setExpression(ast.newSimpleName("Arrays"));
                asList.setName(ast.newSimpleName("asList"));

                // Add default elements? No, start empty.
                return asList;

            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                InfixExpression infixExpr = ast.newInfixExpression();
                infixExpr.setLeftOperand(ast.newSimpleName(DefaultNames.DEFAULT_VARIABLE));
                infixExpr.setRightOperand(ast.newNumberLiteral("0"));

                switch (type) {
                    case ADD: infixExpr.setOperator(InfixExpression.Operator.PLUS); break;
                    case SUBTRACT: infixExpr.setOperator(InfixExpression.Operator.MINUS); break;
                    case MULTIPLY: infixExpr.setOperator(InfixExpression.Operator.TIMES); break;
                    case DIVIDE: infixExpr.setOperator(InfixExpression.Operator.DIVIDE); break;
                    case MODULO: infixExpr.setOperator(InfixExpression.Operator.REMAINDER); break;
                }
                return infixExpr;

            default:
                return null;
        }
    }

    private Statement createDefaultStatement(AST ast, AddableBlock type, CompilationUnit cu, ASTRewrite rewriter) {
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
            case DECLARE_ARRAY: {
                if (cu != null && rewriter != null) {
                    ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
                    ImportManager.addImport(cu, rewriter, "java.util.Arrays");
                }

                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("myList"));

                String defaultType = "ArrayList<Integer>";

                // Pass empty list or null
                Expression initializer = createRecursiveListInitializer(ast, defaultType, cu, rewriter, null);

                fragment.setInitializer(initializer);

                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(TypeManager.createTypeNode(ast, defaultType));

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

    private void collectLeafValues(Expression expr, List<Expression> accumulator) {
        if (expr == null) return;

        boolean isContainer = false;

        // Check: new ArrayList<>(...)
        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            if (cic.getType().toString().startsWith("ArrayList")) {
                isContainer = true;
                if (!cic.arguments().isEmpty()) {
                    // Dive into the argument (usually Arrays.asList)
                    collectLeafValues((Expression) cic.arguments().get(0), accumulator);
                }
            }
        }

        // Check: Arrays.asList(...) or List.of(...)
        else if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            String name = mi.getName().getIdentifier();
            if (name.equals("asList") || name.equals("of")) {
                isContainer = true;
                for (Object arg : mi.arguments()) {
                    collectLeafValues((Expression) arg, accumulator);
                }
            }
        }

        // Check: Array Initializer {1, 2}
        else if (expr instanceof ArrayInitializer) {
            isContainer = true;
            ArrayInitializer ai = (ArrayInitializer) expr;
            for (Object e : ai.expressions()) {
                collectLeafValues((Expression) e, accumulator);
            }
        }

        // Check: new int[] { ... }
        else if (expr instanceof ArrayCreation) {
            isContainer = true;
            ArrayCreation ac = (ArrayCreation) expr;
            if (ac.getInitializer() != null) {
                collectLeafValues(ac.getInitializer(), accumulator);
            }
        }

        // BASE CASE: If it wasn't a container, it's a value. Add it.
        if (!isContainer) {
            accumulator.add(expr);
        }
    }

    public String addElementToList(
            CompilationUnit cu,
            String originalCode,
            ASTNode listNode, // Can be ArrayInitializer or MethodInvocation
            com.botmaker.ui.AddableExpression type,
            int insertIndex) {

        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Create the new element
        Expression newElement = createDefaultExpression(ast, type, cu, rewriter);
        if (newElement == null) return originalCode;

        if (listNode instanceof ArrayInitializer) {
            ListRewrite listRewrite = rewriter.getListRewrite(listNode, ArrayInitializer.EXPRESSIONS_PROPERTY);
            listRewrite.insertAt(newElement, insertIndex, null);
        }
        else if (listNode instanceof MethodInvocation) {
            // Handle Arrays.asList(...) or List.of(...)
            ListRewrite listRewrite = rewriter.getListRewrite(listNode, MethodInvocation.ARGUMENTS_PROPERTY);
            listRewrite.insertAt(newElement, insertIndex, null);
        }

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
// [Inside AstRewriter class]

    public String replaceAssignmentOperator(CompilationUnit cu, String originalCode, Assignment assignment, Assignment.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // Create a new assignment with the same operands but new operator
        Assignment newAssignment = ast.newAssignment();
        newAssignment.setLeftHandSide((Expression)
                ASTNode.copySubtree(ast, assignment.getLeftHandSide()));
        newAssignment.setRightHandSide((Expression)
                ASTNode.copySubtree(ast, assignment.getRightHandSide()));
        newAssignment.setOperator(newOp);

        rewriter.replace(assignment, newAssignment, null);

        org.eclipse.jface.text.IDocument document = new org.eclipse.jface.text.Document(originalCode);
        try {
            org.eclipse.text.edits.TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    // Also handle Prefix expressions (++ / --)
    public String replacePrefixOperator(CompilationUnit cu, String originalCode, PrefixExpression prefix, PrefixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        PrefixExpression newPrefix = ast.newPrefixExpression();
        newPrefix.setOperand((Expression)
                ASTNode.copySubtree(ast, prefix.getOperand()));
        newPrefix.setOperator(newOp);

        rewriter.replace(prefix, newPrefix, null);

        org.eclipse.jface.text.IDocument document = new org.eclipse.jface.text.Document(originalCode);
        try {
            org.eclipse.text.edits.TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }

    // Also handle Postfix expressions (variable++ / variable--)
    public String replacePostfixOperator(CompilationUnit cu, String originalCode, PostfixExpression postfix, PostfixExpression.Operator newOp) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        PostfixExpression newPostfix = ast.newPostfixExpression();
        newPostfix.setOperand((Expression)
                ASTNode.copySubtree(ast, postfix.getOperand()));
        newPostfix.setOperator(newOp);

        rewriter.replace(postfix, newPostfix, null);

        org.eclipse.jface.text.IDocument document = new org.eclipse.jface.text.Document(originalCode);
        try {
            org.eclipse.text.edits.TextEdit edits = rewriter.rewriteAST(document, null);
            edits.apply(document);
            return document.get();
        } catch (Exception e) {
            e.printStackTrace();
            return originalCode;
        }
    }
// ADD THIS METHOD TO AstRewriter.java

    public String replaceVariableType(CompilationUnit cu, String originalCode,
                                      VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        if (newTypeName.contains("ArrayList")) {
            ImportManager.addImport(cu, rewriter, "java.util.ArrayList");
            ImportManager.addImport(cu, rewriter, "java.util.List");
        }

        String oldTypeName = varDecl.getType().toString();
        String oldLeaf = TypeManager.getLeafType(oldTypeName);
        String newLeaf = TypeManager.getLeafType(newTypeName);
        boolean baseTypesMatch = oldLeaf.equals(newLeaf);

        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();
            Expression newInitializer = null;

            // Collect ALL leaves
            List<Expression> valuesToPreserve = new ArrayList<>();
            if (baseTypesMatch && currentInitializer != null) {
                collectLeafValues(currentInitializer, valuesToPreserve);
            }

            if (newTypeName.startsWith("ArrayList<")) {
                // Pass the list of values
                newInitializer = createRecursiveListInitializer(ast, newTypeName, cu, rewriter, valuesToPreserve);
            } else if (newTypeName.endsWith("[]")) {
                ArrayCreation creation = ast.newArrayCreation();
                creation.setType((ArrayType) TypeManager.createTypeNode(ast, newTypeName));
                ArrayInitializer ai = ast.newArrayInitializer();

                // Handle Array Creation with preserved values
                if (!valuesToPreserve.isEmpty()) {
                    for(Expression val : valuesToPreserve) {
                        ai.expressions().add(ASTNode.copySubtree(ast, val));
                    }
                }
                creation.setInitializer(ai);
                newInitializer = creation;
            } else {
                // Single Value Case: Take the first one if available
                if (!valuesToPreserve.isEmpty()) {
                    newInitializer = (Expression) ASTNode.copySubtree(ast, valuesToPreserve.get(0));
                } else {
                    newInitializer = createDefaultInitializer(ast, newTypeName);
                }
            }

            if (newInitializer != null) {
                rewriter.replace(currentInitializer, newInitializer, null);
            }
        }

        return applyRewrite(rewriter, originalCode);
    }



    private String extractArrayListElementType(String arrayListType) {
        if (arrayListType.contains("<") && arrayListType.contains(">")) {
            int start = arrayListType.indexOf("<") + 1;
            int end = arrayListType.lastIndexOf(">");
            return arrayListType.substring(start, end);
        }
        return "Object";
    }
}