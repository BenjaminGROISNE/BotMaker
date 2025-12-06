package com.botmaker.parser;

import com.botmaker.blocks.*;
import com.botmaker.core.*;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.parser.BlockIdPrefix;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class BlockFactory {

    private CompilationUnit ast;
    private String currentSourceCode;
    private List<Comment> allComments;
    private boolean markNewIdentifiersAsUnedited = false;
    private BlockParser blockParser;
    private boolean isReadOnlyMode = false;

    public AbstractCodeBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager, boolean isReadOnly) {
        this.currentSourceCode = javaCode;
        this.isReadOnlyMode = isReadOnly;
        // Initialize parser used for method bodies
        this.blockParser = new BlockParser(this, manager, markNewIdentifiersAsUnedited);

        try {
            // 1. Setup AST Parser
            ASTParser parser = ASTParser.newParser(AST.JLS17);
            parser.setSource(javaCode.toCharArray());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setResolveBindings(true);
            parser.setUnitName("Unit.java");
            parser.setEnvironment(null, null, null, true);

            Map<String, String> options = JavaCore.getOptions();
            options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.latestSupportedJavaVersion());
            options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.latestSupportedJavaVersion());
            options.put(JavaCore.COMPILER_SOURCE, JavaCore.latestSupportedJavaVersion());
            parser.setCompilerOptions(options);

            this.ast = (CompilationUnit) parser.createAST(null);

            // 2. Extract Comments
            this.allComments = new ArrayList<>();
            for (Object obj : ast.getCommentList()) {
                if (obj instanceof Comment && !(obj instanceof Javadoc)) allComments.add((Comment) obj);
            }

            if (ast.types().isEmpty()) return null;

            AbstractTypeDeclaration rootNode = (AbstractTypeDeclaration) ast.types().get(0);

            // --- CASE A: Standard Class File ---
            if (rootNode instanceof TypeDeclaration) {
                TypeDeclaration typeDecl = (TypeDeclaration) rootNode;

                ClassBlock classBlock = new ClassBlock(
                        BlockIdPrefix.generate(BlockIdPrefix.CLASS, typeDecl),
                        typeDecl,
                        manager
                );
                applyReadOnly(classBlock);
                nodeToBlockMap.put(typeDecl, classBlock);

                for (Object obj : typeDecl.bodyDeclarations()) {
                    if (obj instanceof MethodDeclaration) {
                        MethodDeclaration method = (MethodDeclaration) obj;
                        MethodDeclarationBlock methodBlock;

                        if (isMainMethod(method)) {
                            methodBlock = new MainBlock(
                                    BlockIdPrefix.generate(BlockIdPrefix.METHOD, method),
                                    method,
                                    manager
                            );
                        } else {
                            methodBlock = new MethodDeclarationBlock(
                                    BlockIdPrefix.generate(BlockIdPrefix.METHOD, method),
                                    method,
                                    manager
                            );
                        }
                        applyReadOnly(methodBlock);
                        nodeToBlockMap.put(method, methodBlock);

                        if (method.getBody() != null) {
                            methodBlock.setBody(parseBodyBlock(method.getBody(), nodeToBlockMap, manager));
                        }

                        classBlock.addBodyDeclaration(methodBlock);
                    }
                    else if (obj instanceof EnumDeclaration) {
                        EnumDeclaration enumDecl = (EnumDeclaration) obj;
                        DeclareEnumBlock enumBlock = new DeclareEnumBlock(
                                BlockIdPrefix.generate(BlockIdPrefix.ENUM, enumDecl),
                                enumDecl
                        );
                        applyReadOnly(enumBlock);
                        nodeToBlockMap.put(enumDecl, enumBlock);
                        classBlock.addBodyDeclaration(enumBlock);
                    }
                    else if (obj instanceof FieldDeclaration) {
                        FieldDeclaration field = (FieldDeclaration) obj;
                        DeclareClassVariableBlock fieldBlock = new DeclareClassVariableBlock(
                                BlockIdPrefix.generate(BlockIdPrefix.FIELD_ACCESS, field),
                                field
                        );
                        applyReadOnly(fieldBlock);
                        nodeToBlockMap.put(field, fieldBlock);

                        VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
                        if (fragment.getInitializer() != null) {
                            parseExpression(fragment.getInitializer(), nodeToBlockMap).ifPresent(fieldBlock::setInitializer);
                        }

                        classBlock.addBodyDeclaration(fieldBlock);
                    }
                }
                return classBlock;
            }
            // --- CASE B: Standalone Enum File ---
            else if (rootNode instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) rootNode;
                DeclareEnumBlock rootEnumBlock = new DeclareEnumBlock(
                        BlockIdPrefix.generate(BlockIdPrefix.ENUM, enumDecl),
                        enumDecl
                );
                applyReadOnly(rootEnumBlock);
                nodeToBlockMap.put(enumDecl, rootEnumBlock);
                return rootEnumBlock;
            }

            return null;

        } catch (Exception e) {
            System.err.println("Critical error in BlockFactory.convert: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            setMarkNewIdentifiersAsUnedited(false);
        }
    }

    // Overload for backward compatibility (defaults to read/write)
    public AbstractCodeBlock convert(String javaCode, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        return convert(javaCode, nodeToBlockMap, manager, false);
    }

    public void applyReadOnly(CodeBlock block) {
        if (isReadOnlyMode) {
            block.setReadOnly(true);
        }
    }

    public boolean isReadOnlyMode() {
        return isReadOnlyMode;
    }

    // ... (Keep rest of existing methods)

    public BodyBlock parseBodyBlock(Block astBlock, Map<ASTNode, CodeBlock> nodeToBlockMap, BlockDragAndDropManager manager) {
        BodyBlock bodyBlock = new BodyBlock(BlockIdPrefix.generate(BlockIdPrefix.BODY, astBlock), astBlock, manager);
        applyReadOnly(bodyBlock);
        nodeToBlockMap.put(astBlock, bodyBlock);

        List<CodeBlock> allChildren = new ArrayList<>();
        for (Object statementObj : astBlock.statements()) {
            blockParser.parseStatement((Statement) statementObj, nodeToBlockMap).ifPresent(allChildren::add);
        }

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

    // Delegate to BlockParser but apply ReadOnly to results
    public Optional<StatementBlock> parseStatement(Statement stmt, Map<ASTNode, CodeBlock> map, BlockDragAndDropManager manager) {
        Optional<StatementBlock> result = blockParser.parseStatement(stmt, map);
        result.ifPresent(this::applyReadOnly);
        return result;
    }

    public Optional<ExpressionBlock> parseExpression(Expression expr, Map<ASTNode, CodeBlock> map) {
        // ... (ArrayCreation logic from original)
        if (expr instanceof ArrayCreation) {
            ArrayCreation ac = (ArrayCreation) expr;
            if (ac.getInitializer() != null) {
                Optional<ExpressionBlock> innerBlock = parseExpression(ac.getInitializer(), map);
                if (innerBlock.isPresent()) {
                    map.put(expr, innerBlock.get());
                }
                return innerBlock;
            }
        }

        Optional<ExpressionBlock> result = blockParser.parseExpression(expr, map);
        result.ifPresent(this::applyReadOnly);
        return result;
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
        applyReadOnly(commentBlock);
        nodeToBlockMap.put(astNode, commentBlock);
        return commentBlock;
    }

    // ... (Keep isPrintStatement, isReadInputStatement, etc)
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

    public boolean isListStructure(Expression expr) {
        if (expr instanceof ArrayInitializer) return true;
        if (expr instanceof ArrayCreation) return true;
        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            String typeName = cic.getType().toString();
            return (typeName.startsWith("ArrayList") || typeName.startsWith("java.util.ArrayList")) && !cic.arguments().isEmpty();
        }
        if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            String scope = mi.getExpression() != null ? mi.getExpression().toString() : "";
            return (scope.equals("Arrays") && mi.getName().getIdentifier().equals("asList")) ||
                    (scope.equals("List") && mi.getName().getIdentifier().equals("of"));
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<Expression> getListItems(Expression expr) {
        if (expr instanceof ArrayInitializer) return ((ArrayInitializer) expr).expressions();
        if (expr instanceof ArrayCreation) {
            ArrayCreation ac = (ArrayCreation) expr;
            return ac.getInitializer() != null ? ac.getInitializer().expressions() : Collections.emptyList();
        }
        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            if (!cic.arguments().isEmpty()) {
                Expression arg = (Expression) cic.arguments().get(0);
                return getListItems(arg);
            }
        }
        if (expr instanceof MethodInvocation) return ((MethodInvocation) expr).arguments();
        return List.of();
    }

    // ... (Keep private helpers like isMainMethod, setMarkNewIdentifiersAsUnedited)
    private boolean isMainMethod(MethodDeclaration method) {
        if (!"main".equals(method.getName().getIdentifier())) return false;
        if (!Modifier.isStatic(method.getModifiers())) return false;
        if (!Modifier.isPublic(method.getModifiers())) return false;
        if (method.parameters().size() != 1) return false;
        return true;
    }
    public void setMarkNewIdentifiersAsUnedited(boolean mark) { this.markNewIdentifiersAsUnedited = mark; }
}