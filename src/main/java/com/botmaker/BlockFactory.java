package com.botmaker;

import org.eclipse.jdt.core.dom.*;

import java.util.Optional;

public class BlockFactory {

    private CompilationUnit ast;

    public BodyBlock convert(String javaCode) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(javaCode.toCharArray());
        this.ast = (CompilationUnit) parser.createAST(null);

        MainMethodVisitor visitor = new MainMethodVisitor();
        ast.accept(visitor);

        return visitor.getMainMethodBody()
                .map(this::parseBodyBlock)
                .orElse(null);
    }

    private BodyBlock parseBodyBlock(Block astBlock) {
        BodyBlock bodyBlock = new BodyBlock("body_" + astBlock.hashCode(), astBlock);
        for (Object statementObj : astBlock.statements()) {
            Statement statement = (Statement) statementObj;
            parseStatement(statement).ifPresent(bodyBlock::addStatement);
        }
        return bodyBlock;
    }

    private Optional<StatementBlock> parseStatement(Statement astStatement) {
        if (astStatement instanceof VariableDeclarationStatement) {
            return Optional.of(parseVariableDeclaration((VariableDeclarationStatement) astStatement));
        }
        if (astStatement instanceof IfStatement) {
            return Optional.of(parseIfStatement((IfStatement) astStatement));
        }
        if (astStatement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) astStatement).getExpression();
            if (isPrintStatement(expression)) {
                return Optional.of(parsePrintStatement((MethodInvocation) expression));
            }
        }
        return Optional.empty();
    }

    private VariableDeclarationBlock parseVariableDeclaration(VariableDeclarationStatement astNode) {
        VariableDeclarationBlock varBlock = new VariableDeclarationBlock("var_" + astNode.hashCode(), astNode);
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) astNode.fragments().get(0);

        if (fragment.getInitializer() != null) {
            parseExpression(fragment.getInitializer()).ifPresent(varBlock::setInitializer);
        }
        return varBlock;
    }

    private IfBlock parseIfStatement(IfStatement astNode) {
        IfBlock ifBlock = new IfBlock("if_" + astNode.hashCode(), astNode);
        parseExpression(astNode.getExpression()).ifPresent(ifBlock::setCondition);

        if (astNode.getThenStatement() instanceof Block) {
            ifBlock.setThenBody(parseBodyBlock((Block) astNode.getThenStatement()));
        }

        if (astNode.getElseStatement() != null && astNode.getElseStatement() instanceof Block) {
            ifBlock.setElseBody(parseBodyBlock((Block) astNode.getElseStatement()));
        }

        return ifBlock;
    }

    private PrintBlock parsePrintStatement(MethodInvocation astNode) {
        PrintBlock printBlock = new PrintBlock("print_" + astNode.hashCode(), astNode);
        for (Object arg : astNode.arguments()) {
            parseExpression((Expression) arg).ifPresent(printBlock::addArgument);
        }
        return printBlock;
    }

    private Optional<ExpressionBlock> parseExpression(Expression astExpression) {
        if (astExpression instanceof StringLiteral) {
            StringLiteral literalNode = (StringLiteral) astExpression;
            return Optional.of(new LiteralBlock<>("string_" + literalNode.hashCode(), literalNode, literalNode.getLiteralValue()));
        }
        if (astExpression instanceof NumberLiteral) {
            NumberLiteral literalNode = (NumberLiteral) astExpression;
            String token = literalNode.getToken();
            if (token.toLowerCase().endsWith("f")) {
                return Optional.of(new LiteralBlock<>("float_" + literalNode.hashCode(), literalNode, Float.parseFloat(token)));
            } else if (token.contains(".") || token.toLowerCase().endsWith("d")) {
                return Optional.of(new LiteralBlock<>("double_" + literalNode.hashCode(), literalNode, Double.parseDouble(token)));
            } else {
                return Optional.of(new LiteralBlock<>("int_" + literalNode.hashCode(), literalNode, Integer.parseInt(token)));
            }
        }
        if (astExpression instanceof SimpleName) {
            return Optional.of(new IdentifierBlock("id_" + astExpression.hashCode(), (SimpleName) astExpression));
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
