package com.astblocks.demo;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AstToBlocksConverter {

    private String source;
    private CompilationUnit ast;

    public CodeBlock convert(String javaCode) {
        this.source = javaCode;
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(javaCode.toCharArray());
        this.ast = (CompilationUnit) parser.createAST(null);

        // The root block that will contain everything inside the main method
        CodeBlock rootBlock = new CodeBlock("root", "root", "");
        rootBlock.setAstNode(ast);

        ast.accept(new ASTVisitor() {
            private final Stack<CodeBlock> parentStack = new Stack<>();

            @Override
            public boolean visit(MethodDeclaration node) {
                // We only care about the main method
                if (node.getName().getIdentifier().equals("main")) {
                    parentStack.push(rootBlock);
                    return true;
                }
                return false; // Don't visit other methods
            }

            @Override
            public void endVisit(MethodDeclaration node) {
                if (node.getName().getIdentifier().equals("main")) {
                    parentStack.pop();
                }
            }

            @Override
            public boolean visit(IfStatement node) {
                if (parentStack.isEmpty()) return true; // Not in main method, traverse children

                CodeBlock ifBlock = new CodeBlock("if_" + node.hashCode(), "if", "if");
                ifBlock.setAstNode(node);
                parentStack.peek().addChild(ifBlock);
                parentStack.push(ifBlock);

                // Visit condition
                CodeBlock conditionWrapper = new CodeBlock("condition_wrapper", "condition_wrapper", "");
                parentStack.peek().addChild(conditionWrapper);
                parentStack.push(conditionWrapper);
                node.getExpression().accept(this);
                parentStack.pop();

                // Visit then statement
                CodeBlock thenWrapper = new CodeBlock("then_wrapper", "then_wrapper", "");
                parentStack.peek().addChild(thenWrapper);
                parentStack.push(thenWrapper);
                node.getThenStatement().accept(this);
                parentStack.pop();

                parentStack.pop();
                return false; // We handled children manually
            }

            @Override
            public boolean visit(VariableDeclarationStatement node) {
                if (parentStack.isEmpty()) return true; // Not in main method, traverse children

                for (Object frag : node.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) frag;
                    String varName = fragment.getName().getIdentifier();
                    CodeBlock varBlock = new CodeBlock("var_" + varName, "variable_declaration", getSourceText(node));
                    varBlock.setAstNode(node);

                    parentStack.peek().addChild(varBlock);
                    parentStack.push(varBlock);

                    if (fragment.getInitializer() != null) {
                        fragment.getInitializer().accept(this);
                    }
                    parentStack.pop();
                }
                return false; // We handled children manually
            }

            @Override
            public boolean visit(MethodInvocation node) {
                if (parentStack.isEmpty()) return true; // Not in main method, traverse children

                // Specifically for System.out.println
                if (node.getExpression() instanceof QualifiedName && ((QualifiedName) node.getExpression()).getFullyQualifiedName().equals("System.out") && node.getName().getIdentifier().equals("println")) {
                    CodeBlock printBlock = new CodeBlock("print_" + node.hashCode(), "print", "print");
                    printBlock.setAstNode(node);
                    parentStack.peek().addChild(printBlock);
                    parentStack.push(printBlock);

                    // Visit arguments
                    for (Object arg : node.arguments()) {
                        ((ASTNode) arg).accept(this);
                    }

                    parentStack.pop();
                    return false;
                }
                return true;
            }

            @Override
            public boolean visit(StringLiteral node) {
                if (parentStack.isEmpty()) return true; // Not in main method, traverse children

                CodeBlock stringBlock = new CodeBlock("string_" + node.hashCode(), "string_literal", node.getLiteralValue());
                stringBlock.setAstNode(node);
                parentStack.peek().addChild(stringBlock);
                return false;
            }

            @Override
            public boolean visit(SimpleName node) {
                if (parentStack.isEmpty()) return true; // Not in main method, traverse children

                // Could be a variable reference
                CodeBlock nameBlock = new CodeBlock("name_" + node.getIdentifier(), "simple_name", node.getIdentifier());
                nameBlock.setAstNode(node);
                parentStack.peek().addChild(nameBlock);
                return false;
            }
            
            @Override
            public boolean visit(Block node) {
                // This is a block of statements, like { ... }. We visit its children directly.
                return true;
            }
        });

        return rootBlock;
    }

    private String getSourceText(ASTNode node) {
        try {
            return source.substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
        } catch (Exception e) {
            return "Error getting source";
        }
    }
    
    public CompilationUnit getCompilationUnit() {
        return ast;
    }
}