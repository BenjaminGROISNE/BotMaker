package com.botmaker.parser;

import com.botmaker.blocks.*;
import com.botmaker.core.*;
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Map;
import java.util.Optional;

public class BlockFactory {

    private CompilationUnit ast;

    public MainBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(javaCode.toCharArray());
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setUnitName("Demo.java"); // A name is required for bindings to be resolved
        parser.setEnvironment(null, null, null, true); // Use default JRE for classpath
        this.ast = (CompilationUnit) parser.createAST(null);

        MainMethodVisitor visitor = new MainMethodVisitor();
        ast.accept(visitor);

        return visitor.getMainMethodDeclaration()
                .map(mainMethodDecl -> {
                    MainBlock mainBlock = new MainBlock("main_" + mainMethodDecl.hashCode(), mainMethodDecl);
                    nodeToBlockMap.put(mainMethodDecl, mainBlock);
                    visitor.getMainMethodBody().ifPresent(bodyAstNode -> {
                        BodyBlock bodyBlock = parseBodyBlock(bodyAstNode, nodeToBlockMap, manager);
                        mainBlock.setMainBody(bodyBlock);
                    });
                    return mainBlock;
                })
                .orElse(null);
    }

    private BodyBlock parseBodyBlock(Block astBlock, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating BodyBlock for: " + astBlock.hashCode());
        BodyBlock bodyBlock = new BodyBlock("body_" + astBlock.hashCode(), astBlock, manager);
        nodeToBlockMap.put(astBlock, bodyBlock);
        for (Object statementObj : astBlock.statements()) {
            Statement statement = (Statement) statementObj;
            parseStatement(statement, nodeToBlockMap, manager).ifPresent(bodyBlock::addStatement);
        }
        return bodyBlock;
    }

    private Optional<StatementBlock> parseStatement(Statement astStatement, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        if (astStatement instanceof VariableDeclarationStatement) {
            return Optional.of(parseVariableDeclaration((VariableDeclarationStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof IfStatement) {
            return Optional.of(parseIfStatement((IfStatement) astStatement, nodeToBlockMap, manager));
        }
        if (astStatement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) astStatement).getExpression();
            if (isPrintStatement(expression)) {
                return Optional.of(parsePrintStatement((ExpressionStatement) astStatement, nodeToBlockMap));
            }
        }
        return Optional.empty();
    }

    private VariableDeclarationBlock parseVariableDeclaration(VariableDeclarationStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating VariableDeclarationBlock for: " + astNode);
        VariableDeclarationBlock varBlock = new VariableDeclarationBlock("var_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, varBlock);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);

        if (fragment.getInitializer() != null) {
            parseExpression(fragment.getInitializer(), nodeToBlockMap).ifPresent(varBlock::setInitializer);
        }
        return varBlock;
    }

    private IfBlock parseIfStatement(IfStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        System.out.println("Creating IfBlock for: " + astNode);
        IfBlock ifBlock = new IfBlock("if_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, ifBlock);
        parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(ifBlock::setCondition);

        if (astNode.getThenStatement() instanceof Block) {
            ifBlock.setThenBody(parseBodyBlock((Block) astNode.getThenStatement(), nodeToBlockMap, manager));
        }

        if (astNode.getElseStatement() != null && astNode.getElseStatement() instanceof Block) {
            ifBlock.setElseBody(parseBodyBlock((Block) astNode.getElseStatement(), nodeToBlockMap, manager));
        }

        return ifBlock;
    }

    private PrintBlock parsePrintStatement(ExpressionStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating PrintBlock for: " + astNode);
        PrintBlock printBlock = new PrintBlock("print_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, printBlock);

        MethodInvocation methodInvocation = (MethodInvocation) astNode.getExpression();

        if (methodInvocation.arguments().isEmpty()) {
            System.out.println("Creating synthetic String LiteralBlock for empty println");
            // For the synthetic block, pass the MethodInvocation node itself.
            // The LiteralBlock will check for this special case.
            LiteralBlock<String> block = new LiteralBlock<>("synthetic_string_" + astNode.hashCode(), methodInvocation, "");
            printBlock.addArgument(block);
        } else {
            for (Object arg : methodInvocation.arguments()) {
                parseExpression((Expression) arg, nodeToBlockMap).ifPresent(printBlock::addArgument);
            }
        }
        return printBlock;
    }

    private BinaryExpressionBlock parseBinaryExpression(InfixExpression astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        System.out.println("Creating BinaryExpressionBlock for: " + astNode);
        BinaryExpressionBlock binaryBlock = new BinaryExpressionBlock("binary_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, binaryBlock);
        parseExpression(astNode.getLeftOperand(), nodeToBlockMap).ifPresent(binaryBlock::setLeftOperand);
        parseExpression(astNode.getRightOperand(), nodeToBlockMap).ifPresent(binaryBlock::setRightOperand);
        return binaryBlock;
    }

    private Optional<ExpressionBlock> parseExpression(Expression astExpression, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        if (astExpression instanceof StringLiteral) {
            System.out.println("Creating String LiteralBlock for: " + astExpression);
            StringLiteral literalNode = (StringLiteral) astExpression;
            LiteralBlock<String> block = new LiteralBlock<>("string_" + literalNode.hashCode(), literalNode, literalNode.getLiteralValue());
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof NumberLiteral) {
            System.out.println("Creating Number LiteralBlock for: " + astExpression);
            NumberLiteral literalNode = (NumberLiteral) astExpression;
            String token = literalNode.getToken();
            ExpressionBlock block;
            if (token.toLowerCase().endsWith("f")) {
                block = new LiteralBlock<>("float_" + literalNode.hashCode(), literalNode, Float.parseFloat(token));
            } else if (token.contains(".") || token.toLowerCase().endsWith("d")) {
                block = new LiteralBlock<>("double_" + literalNode.hashCode(), literalNode, Double.parseDouble(token));
            } else {
                block = new LiteralBlock<>("int_" + literalNode.hashCode(), literalNode, Integer.parseInt(token));
            }
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof BooleanLiteral) {
            System.out.println("Creating Boolean LiteralBlock for: " + astExpression);
            BooleanLiteral literalNode = (BooleanLiteral) astExpression;
            LiteralBlock<Boolean> block = new LiteralBlock<>("boolean_" + literalNode.hashCode(), literalNode, literalNode.booleanValue());
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof SimpleName) {
            System.out.println("Creating IdentifierBlock for: " + astExpression);
            IdentifierBlock block = new IdentifierBlock("id_" + astExpression.hashCode(), (SimpleName) astExpression);
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

        // Per user instruction, handle the no-argument case differently.
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