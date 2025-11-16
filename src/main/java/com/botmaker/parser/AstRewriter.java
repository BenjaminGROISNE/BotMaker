package com.botmaker.parser;

import com.botmaker.core.BodyBlock;
import com.botmaker.ui.AddableBlock;
import com.botmaker.util.TypeManager;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class AstRewriter {

    public String addStatement(CompilationUnit cu, String originalCode, BodyBlock targetBody, AddableBlock type, int index) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        Statement newStatement = createDefaultStatement(ast, type);
        if (newStatement == null) {
            return originalCode; // Or throw an exception
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
            return originalCode; // Return original on failure
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
            return originalCode; // Return original on failure
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
            // Not a literal we can handle, return original code
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
            return originalCode; // Return original on failure
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
            // else already exists, do nothing
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
            case VARIABLE:
                return ast.newSimpleName("defaultVar");
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
                fragment.setName(ast.newSimpleName("i"));
                fragment.setInitializer(ast.newNumberLiteral("0"));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.INT));
                return varDecl;
            }
            case DECLARE_DOUBLE: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("d"));
                fragment.setInitializer(ast.newNumberLiteral("0.0"));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
                return varDecl;
            }
            case DECLARE_BOOLEAN: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("b"));
                fragment.setInitializer(ast.newBooleanLiteral(false));
                VariableDeclarationStatement varDecl = ast.newVariableDeclarationStatement(fragment);
                varDecl.setType(ast.newPrimitiveType(PrimitiveType.BOOLEAN));
                return varDecl;
            }
            case DECLARE_STRING: {
                VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
                fragment.setName(ast.newSimpleName("s"));
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
                literal.setCharValue('a'); // Default char
                return literal;
            case "String":
                StringLiteral stringLiteral = ast.newStringLiteral();
                stringLiteral.setLiteralValue(""); // Default empty string
                return stringLiteral;
            default:
                // For any other object type, the safest default is null.
                return ast.newNullLiteral();
        }
    }

    public String replaceVariableType(CompilationUnit cu, String originalCode, VariableDeclarationStatement varDecl, String newTypeName) {
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        // 1. Create the new type node
        Type newType = TypeManager.createTypeNode(ast, newTypeName);
        rewriter.replace(varDecl.getType(), newType, null);

        // 2. Get the fragment and its current initializer
        if (!varDecl.fragments().isEmpty()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) varDecl.fragments().get(0);
            Expression currentInitializer = fragment.getInitializer();

            // 3. Create a new default initializer based on the new type
            Expression newInitializer = createDefaultInitializer(ast, newTypeName);

            // 4. Replace the old initializer if it exists and we have a new one
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
