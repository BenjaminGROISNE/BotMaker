package com.botmaker.parser;

import com.botmaker.blocks.*;
import com.botmaker.core.*;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.util.BlockIdPrefix;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class BlockFactory {

    private CompilationUnit ast;
    private String currentSourceCode;
    private List<Comment> allComments;
    private boolean markNewIdentifiersAsUnedited = false;
    private BlockParser blockParser;

    public MainBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        this.currentSourceCode = javaCode;
        // Initialize parser helper with current context
        this.blockParser = new BlockParser(this, manager, markNewIdentifiersAsUnedited);

        try {
            ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setSource(javaCode.toCharArray());
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setUnitName("Demo.java");
            parser.setEnvironment(null, null, null, true);
            this.ast = (CompilationUnit) parser.createAST(null);

            this.allComments = new ArrayList<>();
            for (Object obj : ast.getCommentList()) {
                if (obj instanceof Comment && !(obj instanceof Javadoc)) allComments.add((Comment) obj);
            }

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

    public void setMarkNewIdentifiersAsUnedited(boolean mark) {
        this.markNewIdentifiersAsUnedited = mark;
    }

    public BodyBlock parseBodyBlock(Block astBlock, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        BodyBlock bodyBlock = new BodyBlock(BlockIdPrefix.generate(BlockIdPrefix.BODY, astBlock), astBlock, manager);
        nodeToBlockMap.put(astBlock, bodyBlock);

        List<CodeBlock> allChildren = new ArrayList<>();

        for (Object statementObj : astBlock.statements()) {
            blockParser.parseStatement((Statement) statementObj, nodeToBlockMap).ifPresent(allChildren::add);
        }

        // Handle Comments
        int blockStart = astBlock.getStartPosition() + 1;
        int blockEnd = astBlock.getStartPosition() + astBlock.getLength() - 1;

        for (Comment comment : allComments) {
            int cPos = comment.getStartPosition();
            if (cPos > blockStart && cPos < blockEnd) {
                boolean isInsideChild = false;
                for (Object stmtObj : astBlock.statements()) {
                    Statement s = (Statement) stmtObj;
                    if (cPos >= s.getStartPosition() && cPos <= s.getStartPosition() + s.getLength()) {
                        isInsideChild = true;
                        break;
                    }
                }
                if (!isInsideChild) {
                    allChildren.add(parseCommentBlock(comment, nodeToBlockMap));
                }
            }
        }

        allChildren.sort(Comparator.comparingInt(b -> b.getAstNode().getStartPosition()));
        for (CodeBlock cb : allChildren) {
            if (cb instanceof StatementBlock) bodyBlock.addStatement((StatementBlock) cb);
        }
        return bodyBlock;
    }

    public Optional<StatementBlock> parseStatement(Statement stmt, Map<ASTNode, CodeBlock> map, BlockDragAndDropManager manager) {
        return blockParser.parseStatement(stmt, map);
    }

    public Optional<ExpressionBlock> parseExpression(Expression expr, Map<ASTNode, CodeBlock> map) {
        return blockParser.parseExpression(expr, map);
    }

    private CommentBlock parseCommentBlock(Comment astNode, Map<ASTNode, CodeBlock> nodeToBlockMap) {
        String text = "Comment";
        if (currentSourceCode != null) {
            try {
                String raw = currentSourceCode.substring(astNode.getStartPosition(), astNode.getStartPosition() + astNode.getLength());
                text = astNode.isLineComment() ? raw.substring(2).trim() : raw.substring(2, raw.length() - 2).trim();
            } catch (Exception ignored) {}
        }
        CommentBlock commentBlock = new CommentBlock(BlockIdPrefix.generate(BlockIdPrefix.COMMENT, astNode), astNode, text);
        nodeToBlockMap.put(astNode, commentBlock);
        return commentBlock;
    }

    public boolean isPrintStatement(Expression expression) {
        if (!(expression instanceof MethodInvocation)) return false;
        MethodInvocation method = (MethodInvocation) expression;
        if (!method.getName().getIdentifier().equals("println")) return false;
        return method.getExpression() != null && "System.out".equals(method.getExpression().toString());
    }

    public boolean isReadInputStatement(VariableDeclarationStatement varDecl) {
        if (varDecl.fragments().isEmpty()) return false;
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
        if (!(fragment.getInitializer() instanceof MethodInvocation)) return false;
        MethodInvocation mi = (MethodInvocation) fragment.getInitializer();
        return mi.getExpression() instanceof SimpleName &&
                ((SimpleName) mi.getExpression()).getIdentifier().equals("scanner");
    }

    public CompilationUnit getCompilationUnit() { return ast; }

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
        public Optional<MethodDeclaration> getMainMethodDeclaration() { return Optional.ofNullable(mainMethodDeclaration); }
        public Optional<Block> getMainMethodBody() { return Optional.ofNullable(mainMethodDeclaration != null ? mainMethodDeclaration.getBody() : null); }
    }
}