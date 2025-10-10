package com.botmaker;

import org.eclipse.jdt.core.dom.*;

import java.util.Map;
import java.util.Optional;

public class BlockFactory {

    private CompilationUnit ast;

    public BodyBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(javaCode.toCharArray());
        this.ast = (CompilationUnit) parser.createAST(null);

        MainMethodVisitor visitor = new MainMethodVisitor();
        ast.accept(visitor);

        return visitor.getMainMethodBody()
                .map(block -> parseBodyBlock(block, nodeToBlockMap))
                .orElse(null);
    }

    private BodyBlock parseBodyBlock(Block astBlock, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        BodyBlock bodyBlock = new BodyBlock("body_" + astBlock.hashCode(), astBlock);
        nodeToBlockMap.put(astBlock, bodyBlock);
        for (Object statementObj : astBlock.statements()) {
            Statement statement = (Statement) statementObj;
            parseStatement(statement, nodeToBlockMap).ifPresent(bodyBlock::addStatement);
        }
        return bodyBlock;
    }

    private Optional<StatementBlock> parseStatement(Statement astStatement, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        if (astStatement instanceof VariableDeclarationStatement) {
            return Optional.of(parseVariableDeclaration((VariableDeclarationStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof IfStatement) {
            return Optional.of(parseIfStatement((IfStatement) astStatement, nodeToBlockMap));
        }
        if (astStatement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) astStatement).getExpression();
            if (isPrintStatement(expression)) {
                return Optional.of(parsePrintStatement((MethodInvocation) expression, nodeToBlockMap));
            }
        }
        return Optional.empty();
    }

    private VariableDeclarationBlock parseVariableDeclaration(VariableDeclarationStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        VariableDeclarationBlock varBlock = new VariableDeclarationBlock("var_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, varBlock);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);

        if (fragment.getInitializer() != null) {
            parseExpression(fragment.getInitializer(), nodeToBlockMap).ifPresent(varBlock::setInitializer);
        }
        return varBlock;
    }

    private IfBlock parseIfStatement(IfStatement astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        IfBlock ifBlock = new IfBlock("if_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, ifBlock);
        parseExpression(astNode.getExpression(), nodeToBlockMap).ifPresent(ifBlock::setCondition);

        if (astNode.getThenStatement() instanceof Block) {
            ifBlock.setThenBody(parseBodyBlock((Block) astNode.getThenStatement(), nodeToBlockMap));
        }

        if (astNode.getElseStatement() != null && astNode.getElseStatement() instanceof Block) {
            ifBlock.setElseBody(parseBodyBlock((Block) astNode.getElseStatement(), nodeToBlockMap));
        }

        return ifBlock;
    }

    private PrintBlock parsePrintStatement(MethodInvocation astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        PrintBlock printBlock = new PrintBlock("print_" + astNode.hashCode(), astNode);
        nodeToBlockMap.put(astNode, printBlock);
        for (Object arg : astNode.arguments()) {
            parseExpression((Expression) arg, nodeToBlockMap).ifPresent(printBlock::addArgument);
        }
        return printBlock;
    }

    private Optional<ExpressionBlock> parseExpression(Expression astExpression, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        if (astExpression instanceof StringLiteral) {
            StringLiteral literalNode = (StringLiteral) astExpression;
            LiteralBlock<String> block = new LiteralBlock<>("string_" + literalNode.hashCode(), literalNode, literalNode.getLiteralValue());
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        if (astExpression instanceof NumberLiteral) {
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
        if (astExpression instanceof SimpleName) {
            IdentifierBlock block = new IdentifierBlock("id_" + astExpression.hashCode(), (SimpleName) astExpression);
            nodeToBlockMap.put(astExpression, block);
            return Optional.of(block);
        }
        return Optional.empty();
    }

    private boolean isPrintStatement(Expression expression) {
        if (!(expression instanceof MethodInvocation)) return false;
        MethodInvocation method = (MethodInvocation) expression;
        return method.getName().getIdentifier().equals("println") &&
               method.getExpression() instanceof QualifiedName &&
               ((QualifiedName) method.getExpression()).getFullyQualifiedName().equals("System.out");
    }

    public CompilationUnit getCompilationUnit() {
        return ast;
    }

    private static class MainMethodVisitor extends ASTVisitor {
        private Block mainMethodBody;

        @Override
        public boolean visit(MethodDeclaration node) {
            if ("main".equals(node.getName().getIdentifier())) {
                mainMethodBody = node.getBody();
                return false;
            }
            return true;
        }

        public Optional<Block> getMainMethodBody() {
            return Optional.ofNullable(mainMethodBody);
        }
    }
}
