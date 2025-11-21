package com.botmaker.parser;

import com.botmaker.blocks.*;
import com.botmaker.core.*;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.util.BlockIdPrefix;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockFactory {

    private CompilationUnit ast;
    private boolean markNewIdentifiersAsUnedited = false;

    public MainBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        try {
            ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setSource(javaCode.toCharArray());
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setUnitName("Demo.java");
            parser.setEnvironment(null, null, null, true);
            this.ast = (CompilationUnit) parser.createAST(null);

            MainMethodVisitor visitor = new MainMethodVisitor();
            ast.accept(visitor);

            return visitor.getMainMethodDeclaration()
                    .map(mainMethodDecl -> {
                        MainBlock mainBlock = new MainBlock(BlockIdPrefix.generate(BlockIdPrefix.MAIN, mainMethodDecl), mainMethodDecl);
                        nodeToBlockMap.put(mainMethodDecl, mainBlock);
                        visitor.getMainMethodBody().ifPresent(bodyAstNode -> {
                            BodyBlock bodyBlock = parseBodyBlock(bodyAstNode, nodeToBlockMap, manager);
                            mainBlock.setMainBody(bodyBlock);
                        });
                        return mainBlock;
                    })
                    .orElse(null);
        } finally {
            setMarkNewIdentifiersAsUnedited(false);
        }
    }

    /**
     * Enable marking of newly created identifiers as unedited
     * Call this before converting code that contains newly added blocks
     */
    public void setMarkNewIdentifiersAsUnedited(boolean mark) {
        this.markNewIdentifiersAsUnedited = mark;
    }

    private BodyBlock parseBodyBlock(Block astBlock, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating BodyBlock for: " + astBlock.hashCode());
        BodyBlock bodyBlock = new BodyBlock(BlockIdPrefix.generate(BlockIdPrefix.BODY, astBlock), astBlock, manager);
        nodeToBlockMap.put(astBlock, bodyBlock);
        for (Object statementObj : astBlock.statements()) {
            Statement statement = (Statement) statementObj;
            parseStatement(statement, nodeToBlockMap, manager).ifPresent(bodyBlock::addStatement);
        }
        return bodyBlock;
    }


    private Optional<StatementBlock> parseStatement(Statement astStatement, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        if (astStatement instanceof Block) {
            return Optional.of(parseBodyBlock((Block) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement varDecl = (VariableDeclarationStatement) astStatement;
            // Check if it's a scanner read input
            if (isReadInputStatement(varDecl)) {
                return Optional.of(parseReadInputStatement(varDecl, nodeToBlockMap));
            }
            return Optional.of(parseVariableDeclaration(varDecl, nodeToBlockMap));
        }
        if (astStatement instanceof IfStatement) {
            return Optional.of(parseIfStatement((IfStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof WhileStatement) {
            return Optional.of(parseWhileStatement((WhileStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof ReturnStatement) {
            return Optional.of(parseReturnStatement((ReturnStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof EnhancedForStatement) {
            return Optional.of(parseForStatement((EnhancedForStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof DoStatement) {
            return Optional.of(parseDoWhileStatement((DoStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof SwitchStatement) {
            return Optional.of(parseSwitchStatement((SwitchStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof BreakStatement) {
            return Optional.of(parseBreakStatement((BreakStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof ContinueStatement) {
            return Optional.of(parseContinueStatement((ContinueStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) astStatement).getExpression();
            if (isPrintStatement(expression)) {
                return Optional.of(parsePrintStatement((ExpressionStatement) astStatement, nodeToBlockMap));
            }
            if (expression instanceof Assignment) {
                return Optional.of(parseAssignmentStatement((ExpressionStatement) astStatement, nodeToBlockMap));
            }
            if (expression instanceof PostfixExpression) {
                PostfixExpression postfix = (PostfixExpression) expression;
                if (postfix.getOperator() == PostfixExpression.Operator.INCREMENT ||
                        postfix.getOperator() == PostfixExpression.Operator.DECREMENT) {
                    return Optional.of(parseAssignmentStatement((ExpressionStatement) astStatement, nodeToBlockMap));
                }
            }
            if (expression instanceof PrefixExpression) {
                PrefixExpression prefix = (PrefixExpression) expression;
                if (prefix.getOperator() == PrefixExpression.Operator.INCREMENT ||
                        prefix.getOperator() == PrefixExpression.Operator.DECREMENT) {
                    return Optional.of(parseAssignmentStatement((ExpressionStatement) astStatement, nodeToBlockMap));
                }
            }
        }
        if (astStatement instanceof TryStatement) {
            TryStatement tryStmt = (TryStatement) astStatement;
            if (isWaitStatement(tryStmt)) {
                return Optional.of(parseWaitStatement(tryStmt, nodeToBlockMap));
            }
        }
        return Optional.empty();
    }

    private boolean isWaitStatement(TryStatement tryStmt) {
        // Check if body has exactly one statement
        if (tryStmt.getBody().statements().size() != 1) return false;

        Statement firstStmt = (Statement) tryStmt.getBody().statements().getFirst();
        if (!(firstStmt instanceof ExpressionStatement)) return false;

        Expression expr = ((ExpressionStatement) firstStmt).getExpression();
        if (!(expr instanceof MethodInvocation)) return false;

        MethodInvocation mi = (MethodInvocation) expr;

        // Check for Thread.sleep
        if (!"sleep".equals(mi.getName().getIdentifier())) return false;

        Expression scope = mi.getExpression();
        return scope != null && "Thread".equals(scope.toString());
    }

    // 3. logic to create the visual block
    private WaitBlock parseWaitStatement(TryStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating WaitBlock for: " + astNode);
        WaitBlock waitBlock = new WaitBlock(BlockIdPrefix.generate(BlockIdPrefix.WAIT, astNode), astNode);
        nodeToBlockMap.put(astNode, waitBlock);

        // Extract the argument from Thread.sleep(arg) inside the try block
        Statement innerStmt = (Statement) astNode.getBody().statements().getFirst();
        ExpressionStatement exprStmt = (ExpressionStatement) innerStmt;
        MethodInvocation mi = (MethodInvocation) exprStmt.getExpression();

        if (!mi.arguments().isEmpty()) {
            Expression arg = (Expression) mi.arguments().getFirst();
            parseExpression(arg, nodeToBlockMap).ifPresent(waitBlock::setDuration);
        }

        return waitBlock;
    }

    private ReturnBlock parseReturnStatement(ReturnStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating ReturnBlock for: " + astNode);
        ReturnBlock returnBlock = new ReturnBlock(BlockIdPrefix.generate(BlockIdPrefix.RETURN, astNode), astNode);
        nodeToBlockMap.put(astNode, returnBlock);
        return returnBlock;
    }

    private DoWhileBlock parseDoWhileStatement(DoStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating DoWhileBlock for: " + astNode);
        DoWhileBlock doWhileBlock = new DoWhileBlock(BlockIdPrefix.generate(BlockIdPrefix.DO_WHILE, astNode), astNode, manager);
        nodeToBlockMap.put(astNode, doWhileBlock);

        // Parse body first (do part)
        if (astNode.getBody() instanceof Block) {
            doWhileBlock.setBody(parseBodyBlock((Block) astNode.getBody(), nodeToBlockMap, manager));
        }

        // Parse condition (while part)
        if (astNode.getExpression() != null) {
            parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(doWhileBlock::setCondition);
        }

        return doWhileBlock;
    }

    // Parse switch statement
    private SwitchBlock parseSwitchStatement(SwitchStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating SwitchBlock for: " + astNode);
        SwitchBlock switchBlock = new SwitchBlock(BlockIdPrefix.generate(BlockIdPrefix.SWITCH, astNode), astNode, manager);
        nodeToBlockMap.put(astNode, switchBlock);

        // Parse the switch expression
        if (astNode.getExpression() != null) {
            parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(switchBlock::setExpression);
        }

        // Parse switch cases
        List<Statement> statements = astNode.statements();
        BodyBlock currentCaseBody = null;
        SwitchBlock.SwitchCaseBlock currentCase = null;

        for (Statement stmt : statements) {
            if (stmt instanceof SwitchCase) {
                // Start a new case
                SwitchCase switchCase = (SwitchCase) stmt;
                String caseId = BlockIdPrefix.generate(BlockIdPrefix.SWITCH + "_case_", switchCase);
                currentCase = new SwitchBlock.SwitchCaseBlock(caseId, switchCase, manager);
                nodeToBlockMap.put(switchCase, currentCase);

                // Parse case expression (null for default)
                if (!switchCase.isDefault() && !switchCase.expressions().isEmpty()) {
                    Expression caseExpr = (Expression) switchCase.expressions().getFirst();
                    parseExpression(caseExpr, nodeToBlockMap).ifPresent(currentCase::setCaseExpression);
                }

                // Create body for this case
                currentCaseBody = new BodyBlock(
                        BlockIdPrefix.generate(BlockIdPrefix.BODY, switchCase),
                        ast.getAST().newBlock(), // FIX: Access AST from CompilationUnit
                        manager
                );
                currentCase.setBody(currentCaseBody);
                switchBlock.addCase(currentCase);

            } else if (currentCaseBody != null) {
                // Add statement to current case body
                parseStatement(stmt, nodeToBlockMap, manager).ifPresent(currentCaseBody::addStatement);
            }
        }

        return switchBlock;
    }

    // Parse comment block (for when we add comment support)
    private CommentBlock parseCommentBlock(LineComment astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, String commentText) {
        System.out.println("Creating CommentBlock for: " + astNode);
        CommentBlock commentBlock = new CommentBlock(
                BlockIdPrefix.generate(BlockIdPrefix.COMMENT, astNode),
                astNode,
                commentText
        );
        nodeToBlockMap.put(astNode, commentBlock);
        return commentBlock;
    }

    private VariableDeclarationBlock parseVariableDeclaration(VariableDeclarationStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating VariableDeclarationBlock for: " + astNode);
        VariableDeclarationBlock varBlock = new VariableDeclarationBlock(BlockIdPrefix.generate(BlockIdPrefix.VARIABLE, astNode), astNode);
        nodeToBlockMap.put(astNode, varBlock);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().getFirst();

        if (fragment.getInitializer() != null) {
            parseExpression(fragment.getInitializer(), nodeToBlockMap).ifPresent(varBlock::setInitializer);
        }
        return varBlock;
    }

    private IfBlock parseIfStatement(IfStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating IfBlock for: " + astNode);
        IfBlock ifBlock = new IfBlock(BlockIdPrefix.generate(BlockIdPrefix.IF, astNode), astNode);
        nodeToBlockMap.put(astNode, ifBlock);
        parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(ifBlock::setCondition);

        if (astNode.getThenStatement() instanceof Block) {
            ifBlock.setThenBody(parseBodyBlock((Block) astNode.getThenStatement(), nodeToBlockMap, manager));
        }

        Statement elseStmt = astNode.getElseStatement();
        if (elseStmt != null) {
            parseStatement(elseStmt, nodeToBlockMap, manager).ifPresent(ifBlock::setElseStatement);
        }

        return ifBlock;
    }

    private PrintBlock parsePrintStatement(ExpressionStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating PrintBlock for: " + astNode);
        PrintBlock printBlock = new PrintBlock(BlockIdPrefix.generate(BlockIdPrefix.PRINT, astNode), astNode);
        nodeToBlockMap.put(astNode, printBlock);

        MethodInvocation methodInvocation = (MethodInvocation) astNode.getExpression();

        if (methodInvocation.arguments().isEmpty()) {
            System.out.println("Creating synthetic String LiteralBlock for empty println");
            LiteralBlock<String> block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.SYNTHETIC_STRING, astNode), methodInvocation, "");
            printBlock.addArgument(block);
        } else {
            for (Object arg : methodInvocation.arguments()) {
                parseExpression((Expression) arg, nodeToBlockMap).ifPresent(printBlock::addArgument);
            }
        }
        return printBlock;
    }


    private boolean isReadInputStatement(VariableDeclarationStatement varDecl) {
        if (varDecl.fragments().isEmpty()) return false;
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().getFirst();
        Expression initializer = fragment.getInitializer();

        if (initializer instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) initializer;
            Expression expr = mi.getExpression();
            if (expr instanceof SimpleName) {
                SimpleName name = (SimpleName) expr;
                String methodName = mi.getName().getIdentifier();
                return name.getIdentifier().equals("scanner") &&
                        (methodName.equals("nextLine") || methodName.equals("nextInt") ||
                                methodName.equals("nextDouble") || methodName.equals("nextBoolean"));
            }
        }
        return false;
    }

    // Parse while statement
    private WhileBlock parseWhileStatement(WhileStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating WhileBlock for: " + astNode);
        WhileBlock whileBlock = new WhileBlock(BlockIdPrefix.generate(BlockIdPrefix.WHILE, astNode), astNode, manager);
        nodeToBlockMap.put(astNode, whileBlock);

        parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(whileBlock::setCondition);

        if (astNode.getBody() instanceof Block) {
            whileBlock.setBody(parseBodyBlock((Block) astNode.getBody(), nodeToBlockMap, manager));
        }

        return whileBlock;
    }

    private ForBlock parseForStatement(EnhancedForStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating ForBlock (foreach) for: " + astNode);
        ForBlock forBlock = new ForBlock(BlockIdPrefix.generate(BlockIdPrefix.FOR, astNode), astNode, manager);
        nodeToBlockMap.put(astNode, forBlock);

        // Parse the variable parameter (e.g., "String item")
        SingleVariableDeclaration param = astNode.getParameter();
        if (param != null) {
            // Create an identifier block for the variable name
            parseExpression(param.getName(), nodeToBlockMap).ifPresent(forBlock::setVariable);
        }

        // Parse the collection/array expression
        if (astNode.getExpression() != null) {
            parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(forBlock::setCollection);
        }

        // Parse the body
        if (astNode.getBody() instanceof Block) {
            forBlock.setBody(parseBodyBlock((Block) astNode.getBody(), nodeToBlockMap, manager));
        }

        return forBlock;
    }

    // Parse break statement
    private BreakBlock parseBreakStatement(BreakStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating BreakBlock for: " + astNode);
        BreakBlock breakBlock = new BreakBlock(BlockIdPrefix.generate(BlockIdPrefix.BREAK, astNode), astNode);
        nodeToBlockMap.put(astNode, breakBlock);
        return breakBlock;
    }

    // Parse continue statement
    private ContinueBlock parseContinueStatement(ContinueStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating ContinueBlock for: " + astNode);
        ContinueBlock continueBlock = new ContinueBlock(BlockIdPrefix.generate(BlockIdPrefix.CONTINUE, astNode), astNode);
        nodeToBlockMap.put(astNode, continueBlock);
        return continueBlock;
    }

    // Parse assignment statement
    private AssignmentBlock parseAssignmentStatement(ExpressionStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating AssignmentBlock for: " + astNode);
        AssignmentBlock assignBlock = new AssignmentBlock(BlockIdPrefix.generate(BlockIdPrefix.ASSIGNMENT, astNode), astNode);
        nodeToBlockMap.put(astNode, assignBlock);

        Assignment assignment = (Assignment) astNode.getExpression();
        parseExpression(assignment.getLeftHandSide(), nodeToBlockMap).ifPresent(assignBlock::setLeftHandSide);
        parseExpression(assignment.getRightHandSide(), nodeToBlockMap).ifPresent(assignBlock::setRightHandSide);

        return assignBlock;
    }

    // Parse read input statement
    private ReadInputBlock parseReadInputStatement(VariableDeclarationStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating ReadInputBlock for: " + astNode);

        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().getFirst();
        MethodInvocation mi = (MethodInvocation) fragment.getInitializer();
        String inputType = mi.getName().getIdentifier();

        ReadInputBlock readBlock = new ReadInputBlock(
                BlockIdPrefix.generate(BlockIdPrefix.READ_INPUT, astNode), astNode, inputType
        );
        nodeToBlockMap.put(astNode, readBlock);

        parseExpression(fragment.getName(), nodeToBlockMap).ifPresent(readBlock::setVariableName);

        return readBlock;
    }

    private BinaryExpressionBlock parseBinaryExpression(InfixExpression astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating BinaryExpressionBlock for: " + astNode);
        BinaryExpressionBlock binaryBlock = new BinaryExpressionBlock(BlockIdPrefix.generate(BlockIdPrefix.BINARY, astNode), astNode);
        nodeToBlockMap.put(astNode, binaryBlock);
        parseExpression(astNode.getLeftOperand(), nodeToBlockMap).ifPresent(binaryBlock::setLeftOperand);
        parseExpression(astNode.getRightOperand(), nodeToBlockMap).ifPresent(binaryBlock::setRightOperand);
        return binaryBlock;
    }

    private Optional<ExpressionBlock> parseExpression(Expression astExpression, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        if (astExpression instanceof StringLiteral) {
            System.out.println("Creating String LiteralBlock for: " + astExpression);
            StringLiteral literalNode = (StringLiteral) astExpression;
            LiteralBlock<String> block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.STRING, literalNode), literalNode, literalNode.getLiteralValue());
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof NumberLiteral) {
            System.out.println("Creating Number LiteralBlock for: " + astExpression);
            NumberLiteral literalNode = (NumberLiteral) astExpression;
            String token = literalNode.getToken();
            ExpressionBlock block;
            if (token.toLowerCase().endsWith("f")) {
                block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_FLOAT, literalNode), literalNode, Float.parseFloat(token));
            } else if (token.contains(".") || token.toLowerCase().endsWith("d")) {
                block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_DOUBLE, literalNode), literalNode, Double.parseDouble(token));
            } else {
                block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_INT, literalNode), literalNode, Integer.parseInt(token));
            }
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof BooleanLiteral) {
            System.out.println("Creating Boolean LiteralBlock for: " + astExpression);
            BooleanLiteral literalNode = (BooleanLiteral) astExpression;
            LiteralBlock<Boolean> block = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.BOOLEAN, literalNode), literalNode, literalNode.booleanValue());
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof SimpleName) {
            // Do not convert type names into identifier blocks
            if (astExpression.getParent() instanceof Type) {
                return Optional.empty();
            }
            System.out.println("Creating IdentifierBlock for: " + astExpression);
            SimpleName simpleName = (SimpleName) astExpression;

            // Mark as unedited if this identifier was just auto-generated
            // The flag is set externally via setMarkNewIdentifiersAsUnedited()
            IdentifierBlock block = new IdentifierBlock(
                    BlockIdPrefix.generate(BlockIdPrefix.IDENTIFIER, astExpression),
                    simpleName,
                    markNewIdentifiersAsUnedited
            );
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof InfixExpression) {
            return Optional.of(parseBinaryExpression((InfixExpression) astExpression, nodeToBlockMap));
        }
        return Optional.empty();
    }

    private boolean isPrintStatement(Expression expression) {
        if (!(expression instanceof MethodInvocation)) {
            return false;
        }
        MethodInvocation method = (MethodInvocation) expression;

        if (!method.getName().getIdentifier().equals("println")) {
            return false;
        }

        if (method.arguments().isEmpty()) {
            return method.toString().startsWith("System.out.println");
        } else {
            Expression expr = method.getExpression();
            return expr != null && "System.out".equals(expr.toString());
        }
    }

    public CompilationUnit getCompilationUnit() {
        return ast;
    }

    private static class MainMethodVisitor extends ASTVisitor {
        private MethodDeclaration mainMethodDeclaration;

        @Override
        public boolean visit(MethodDeclaration node) {
            if ("main".equals(node.getName().getIdentifier())) {
                mainMethodDeclaration = node;
                return false;
            }
            return true;
        }

        public Optional<MethodDeclaration> getMainMethodDeclaration() {
            return Optional.ofNullable(mainMethodDeclaration);
        }

        public Optional<Block> getMainMethodBody() {
            return Optional.ofNullable(mainMethodDeclaration != null ? mainMethodDeclaration.getBody() : null);
        }
    }
}