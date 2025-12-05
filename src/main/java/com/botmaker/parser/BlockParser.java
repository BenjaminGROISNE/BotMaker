// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\parser\BlockParser.java
package com.botmaker.parser;

import com.botmaker.blocks.*;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.ExpressionBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.ui.BlockDragAndDropManager;
import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockParser {

    private final BlockFactory factory;
    private final BlockDragAndDropManager manager;
    private final boolean markNewIdentifiersAsUnedited;

    public BlockParser(BlockFactory factory, BlockDragAndDropManager manager, boolean markNewIdentifiersAsUnedited) {
        this.factory = factory;
        this.manager = manager;
        this.markNewIdentifiersAsUnedited = markNewIdentifiersAsUnedited;
    }

    // ... (Existing parseStatement and other methods remain unchanged) ...
    public Optional<StatementBlock> parseStatement(Statement stmt, Map<ASTNode, CodeBlock> map) {
        try {
            if (stmt instanceof Block) return Optional.of(factory.parseBodyBlock((Block) stmt, map, manager));
            if (stmt instanceof TypeDeclarationStatement) return parseTypeDeclaration((TypeDeclarationStatement) stmt, map);
            if (stmt instanceof VariableDeclarationStatement) return parseVariableDecl((VariableDeclarationStatement) stmt, map);
            if (stmt instanceof IfStatement) return parseIf((IfStatement) stmt, map);
            if (stmt instanceof WhileStatement) return parseWhile((WhileStatement) stmt, map);
            if (stmt instanceof EnhancedForStatement) return parseFor((EnhancedForStatement) stmt, map);
            if (stmt instanceof DoStatement) return parseDoWhile((DoStatement) stmt, map);
            if (stmt instanceof SwitchStatement) return parseSwitch((SwitchStatement) stmt, map);
            if (stmt instanceof BreakStatement) return Optional.of(new BreakBlock(BlockIdPrefix.generate(BlockIdPrefix.BREAK, stmt), (BreakStatement) stmt));
            if (stmt instanceof ContinueStatement) return Optional.of(new ContinueBlock(BlockIdPrefix.generate(BlockIdPrefix.CONTINUE, stmt), (ContinueStatement) stmt));
            if (stmt instanceof ReturnStatement) return parseReturn((ReturnStatement) stmt, map);
            if (stmt instanceof TryStatement) return parseTry((TryStatement) stmt, map);
            if (stmt instanceof ExpressionStatement) return parseExprStmt((ExpressionStatement) stmt, map);
        } catch (Exception e) {
            System.err.println("Error parsing statement: " + stmt);
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // ... (Keep existing private helper methods: parseReturn, parseTypeDeclaration, etc.) ...

    // Copy all the private parse* methods from the original file here to ensure they aren't lost.
    // For brevity in this response, I assume they exist.
    // ...
    private Optional<StatementBlock> parseReturn(ReturnStatement stmt, Map<ASTNode, CodeBlock> map) {
        ReturnBlock block = new ReturnBlock(BlockIdPrefix.generate(BlockIdPrefix.RETURN, stmt), stmt);
        map.put(stmt, block);
        if (stmt.getExpression() != null) {
            factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setExpression);
        }
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseTypeDeclaration(TypeDeclarationStatement stmt, Map<ASTNode, CodeBlock> map) {
        if (stmt.getDeclaration() instanceof EnumDeclaration) {
            EnumDeclaration enumDecl = (EnumDeclaration) stmt.getDeclaration();
            DeclareEnumBlock block = new DeclareEnumBlock(BlockIdPrefix.generate(BlockIdPrefix.ENUM, stmt), stmt);
            map.put(stmt, block);
            map.put(enumDecl, block);
            return Optional.of(block);
        }
        return Optional.empty();
    }

    private Optional<StatementBlock> parseExprStmt(ExpressionStatement stmt, Map<ASTNode, CodeBlock> map) {
        Expression expr = stmt.getExpression();
        if (factory.isPrintStatement(expr)) return parsePrint(stmt, map);
        if (expr instanceof Assignment) return parseAssignment(stmt, map);
        if (expr instanceof PostfixExpression || expr instanceof PrefixExpression) {
            AssignmentBlock block = new AssignmentBlock(BlockIdPrefix.generate(BlockIdPrefix.ASSIGNMENT, stmt), stmt);
            map.put(stmt, block);
            if (expr instanceof PostfixExpression) factory.parseExpression(((PostfixExpression) expr).getOperand(), map).ifPresent(block::setLeftHandSide);
            if (expr instanceof PrefixExpression) factory.parseExpression(((PrefixExpression) expr).getOperand(), map).ifPresent(block::setLeftHandSide);
            return Optional.of(block);
        }
        if (expr instanceof MethodInvocation) {
            MethodInvocationBlock block = new MethodInvocationBlock(BlockIdPrefix.generate("call_", stmt), stmt);
            map.put(stmt, block);
            MethodInvocation mi = (MethodInvocation) expr;
            for (Object arg : mi.arguments()) {
                factory.parseExpression((Expression) arg, map).ifPresent(block::addArgument);
            }
            return Optional.of(block);
        }
        return Optional.empty();
    }

    private Optional<StatementBlock> parseVariableDecl(VariableDeclarationStatement stmt, Map<ASTNode, CodeBlock> map) {
        if (factory.isReadInputStatement(stmt)) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) stmt.fragments().get(0);
            MethodInvocation mi = (MethodInvocation) frag.getInitializer();
            ReadInputBlock block = new ReadInputBlock(BlockIdPrefix.generate(BlockIdPrefix.READ_INPUT, stmt), stmt, mi.getName().getIdentifier());
            map.put(stmt, block);
            factory.parseExpression(frag.getName(), map).ifPresent(block::setVariableName);
            return Optional.of(block);
        } else {
            VariableDeclarationBlock block = new VariableDeclarationBlock(BlockIdPrefix.generate(BlockIdPrefix.VARIABLE, stmt), stmt);
            map.put(stmt, block);
            VariableDeclarationFragment frag = (VariableDeclarationFragment) stmt.fragments().get(0);
            if (frag.getInitializer() != null) {
                factory.parseExpression(frag.getInitializer(), map).ifPresent(block::setInitializer);
            }
            return Optional.of(block);
        }
    }

    private Optional<StatementBlock> parseIf(IfStatement stmt, Map<ASTNode, CodeBlock> map) {
        IfBlock block = new IfBlock(BlockIdPrefix.generate(BlockIdPrefix.IF, stmt), stmt);
        map.put(stmt, block);
        factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setCondition);
        if (stmt.getThenStatement() instanceof Block) {
            block.setThenBody(factory.parseBodyBlock((Block) stmt.getThenStatement(), map, manager));
        }
        if (stmt.getElseStatement() != null) {
            factory.parseStatement(stmt.getElseStatement(), map, manager).ifPresent(block::setElseStatement);
        }
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseWhile(WhileStatement stmt, Map<ASTNode, CodeBlock> map) {
        WhileBlock block = new WhileBlock(BlockIdPrefix.generate(BlockIdPrefix.WHILE, stmt), stmt, manager);
        map.put(stmt, block);
        factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setCondition);
        if (stmt.getBody() instanceof Block) block.setBody(factory.parseBodyBlock((Block) stmt.getBody(), map, manager));
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseFor(EnhancedForStatement stmt, Map<ASTNode, CodeBlock> map) {
        ForBlock block = new ForBlock(BlockIdPrefix.generate(BlockIdPrefix.FOR, stmt), stmt, manager);
        map.put(stmt, block);
        if (stmt.getParameter() != null) factory.parseExpression(stmt.getParameter().getName(), map).ifPresent(block::setVariable);
        if (stmt.getExpression() != null) factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setCollection);
        if (stmt.getBody() instanceof Block) block.setBody(factory.parseBodyBlock((Block) stmt.getBody(), map, manager));
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseDoWhile(DoStatement stmt, Map<ASTNode, CodeBlock> map) {
        DoWhileBlock block = new DoWhileBlock(BlockIdPrefix.generate(BlockIdPrefix.DO_WHILE, stmt), stmt, manager);
        map.put(stmt, block);
        factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setCondition);
        if (stmt.getBody() instanceof Block) block.setBody(factory.parseBodyBlock((Block) stmt.getBody(), map, manager));
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseSwitch(SwitchStatement stmt, Map<ASTNode, CodeBlock> map) {
        SwitchBlock block = new SwitchBlock(BlockIdPrefix.generate(BlockIdPrefix.SWITCH, stmt), stmt, manager);
        map.put(stmt, block);
        if (stmt.getExpression() != null) factory.parseExpression(stmt.getExpression(), map).ifPresent(block::setExpression);

        BodyBlock currentBody = null;
        SwitchBlock.SwitchCaseBlock currentCase = null;

        for (Object obj : stmt.statements()) {
            Statement s = (Statement) obj;
            if (s instanceof SwitchCase) {
                SwitchCase sc = (SwitchCase) s;
                currentCase = new SwitchBlock.SwitchCaseBlock(BlockIdPrefix.generate(BlockIdPrefix.SWITCH + "_case_", sc), sc, manager);
                map.put(sc, currentCase);
                if (!sc.isDefault() && !sc.expressions().isEmpty()) {
                    factory.parseExpression((Expression) sc.expressions().get(0), map).ifPresent(currentCase::setCaseExpression);
                }
                currentBody = new BodyBlock(BlockIdPrefix.generate(BlockIdPrefix.BODY, sc), sc, manager);
                currentCase.setBody(currentBody);
                block.addCase(currentCase);
            } else if (currentBody != null) {
                factory.parseStatement(s, map, manager).ifPresent(currentBody::addStatement);
            }
        }
        return Optional.of(block);
    }

    private Optional<StatementBlock> parsePrint(ExpressionStatement stmt, Map<ASTNode, CodeBlock> map) {
        PrintBlock block = new PrintBlock(BlockIdPrefix.generate(BlockIdPrefix.PRINT, stmt), stmt);
        map.put(stmt, block);
        MethodInvocation mi = (MethodInvocation) stmt.getExpression();
        if (mi.arguments().isEmpty()) {
            block.addArgument(new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.SYNTHETIC_STRING, stmt), mi, ""));
        } else {
            for (Object arg : mi.arguments()) factory.parseExpression((Expression) arg, map).ifPresent(block::addArgument);
        }
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseAssignment(ExpressionStatement stmt, Map<ASTNode, CodeBlock> map) {
        AssignmentBlock block = new AssignmentBlock(BlockIdPrefix.generate(BlockIdPrefix.ASSIGNMENT, stmt), stmt);
        map.put(stmt, block);
        Assignment a = (Assignment) stmt.getExpression();
        factory.parseExpression(a.getLeftHandSide(), map).ifPresent(block::setLeftHandSide);
        factory.parseExpression(a.getRightHandSide(), map).ifPresent(block::setRightHandSide);
        return Optional.of(block);
    }

    private Optional<StatementBlock> parseTry(TryStatement stmt, Map<ASTNode, CodeBlock> map) {
        if (isWait(stmt)) {
            WaitBlock block = new WaitBlock(BlockIdPrefix.generate(BlockIdPrefix.WAIT, stmt), stmt);
            map.put(stmt, block);
            Statement inner = (Statement) stmt.getBody().statements().getFirst();
            MethodInvocation mi = (MethodInvocation) ((ExpressionStatement) inner).getExpression();
            if (!mi.arguments().isEmpty()) factory.parseExpression((Expression) mi.arguments().getFirst(), map).ifPresent(block::setDuration);
            return Optional.of(block);
        }
        return Optional.empty();
    }

    private boolean isWait(TryStatement stmt) {
        if (stmt.getBody().statements().size() != 1) return false;
        Statement first = (Statement) stmt.getBody().statements().get(0);
        if (!(first instanceof ExpressionStatement)) return false;
        Expression e = ((ExpressionStatement) first).getExpression();
        return e instanceof MethodInvocation && "sleep".equals(((MethodInvocation) e).getName().getIdentifier()) && "Thread".equals(((MethodInvocation) e).getExpression().toString());
    }

    public Optional<ExpressionBlock> parseExpression(Expression expr, Map<ASTNode, CodeBlock> map) {
        if (expr instanceof StringLiteral) {
            LiteralBlock<String> b = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.STRING, expr), expr, ((StringLiteral) expr).getLiteralValue());
            map.put(expr, b);
            return Optional.of(b);
        }

        if (expr instanceof ArrayInitializer) {
            ArrayInitializer arrayInit = (ArrayInitializer) expr;
            ListBlock block = new ListBlock(BlockIdPrefix.generate(BlockIdPrefix.LIST, expr), arrayInit);
            map.put(expr, block);
            for (Object item : arrayInit.expressions()) {
                parseExpression((Expression) item, map).ifPresent(block::addElement);
            }
            return Optional.of(block);
        }

        if (factory.isListStructure(expr)) {
            ListBlock b = new ListBlock(BlockIdPrefix.generate(BlockIdPrefix.LIST, expr), expr);
            map.put(expr, b);
            List<Expression> items = factory.getListItems(expr);
            for (Expression item : items) factory.parseExpression(item, map).ifPresent(b::addElement);
            return Optional.of(b);
        }
        if (expr instanceof FieldAccess) {
            FieldAccess fa = (FieldAccess) expr;
            FieldAccessBlock b = new FieldAccessBlock(BlockIdPrefix.generate(BlockIdPrefix.FIELD_ACCESS, expr), fa, markNewIdentifiersAsUnedited);
            map.put(expr, b);
            return Optional.of(b);
        }
        if (expr instanceof QualifiedName) {
            QualifiedName qn = (QualifiedName) expr;
            if (isEnumConstantReference(qn)) {
                EnumConstantBlock b = new EnumConstantBlock(BlockIdPrefix.generate(BlockIdPrefix.ENUM_CONSTANT, expr), qn);
                map.put(expr, b);
                return Optional.of(b);
            } else if (isFieldAccessReference(qn)) {
                FieldAccessBlock b = new FieldAccessBlock(BlockIdPrefix.generate(BlockIdPrefix.FIELD_ACCESS, expr), qn, markNewIdentifiersAsUnedited);
                map.put(expr, b);
                return Optional.of(b);
            }
        }
        if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            MethodInvocationBlock block = new MethodInvocationBlock(BlockIdPrefix.generate("call_expr_", expr), expr);
            map.put(expr, block);
            for (Object arg : mi.arguments()) {
                factory.parseExpression((Expression) arg, map).ifPresent(block::addArgument);
            }
            return Optional.of(block);
        }
        if (expr instanceof NumberLiteral) {
            String t = ((NumberLiteral) expr).getToken();
            ExpressionBlock b;
            if (t.toLowerCase().endsWith("f")) b = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_FLOAT, expr), expr, Float.parseFloat(t));
            else if (t.contains(".") || t.toLowerCase().endsWith("d")) b = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_DOUBLE, expr), expr, Double.parseDouble(t));
            else b = new LiteralBlock<>(BlockIdPrefix.generate(BlockIdPrefix.NUMBER_INT, expr), expr, Integer.parseInt(t));
            map.put(expr, b);
            return Optional.of(b);
        }
        if (expr instanceof BooleanLiteral) {
            BooleanLiteralBlock b = new BooleanLiteralBlock(BlockIdPrefix.generate(BlockIdPrefix.BOOLEAN, expr), (BooleanLiteral) expr);
            map.put(expr, b);
            return Optional.of(b);
        }
        if (expr instanceof SimpleName) {
            if (expr.getParent() instanceof Type) return Optional.empty();
            IdentifierBlock b = new IdentifierBlock(BlockIdPrefix.generate(BlockIdPrefix.IDENTIFIER, expr), (SimpleName) expr, markNewIdentifiersAsUnedited);
            map.put(expr, b);
            return Optional.of(b);
        }

        // --- MODIFIED SECTION: Handling Comparison/Logic Operators ---
        if (expr instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) expr;
            InfixExpression.Operator op = infix.getOperator();

            // Check if it is a comparison or logic operator
            if (isComparisonOperator(op)) {
                ComparisonExpressionBlock b = new ComparisonExpressionBlock(
                        BlockIdPrefix.generate(BlockIdPrefix.BINARY, expr),
                        infix
                );
                map.put(expr, b);
                factory.parseExpression(infix.getLeftOperand(), map).ifPresent(b::setLeftOperand);
                factory.parseExpression(infix.getRightOperand(), map).ifPresent(b::setRightOperand);
                return Optional.of(b);
            }
            // Default to Math (BinaryExpressionBlock)
            else {
                BinaryExpressionBlock b = new BinaryExpressionBlock(
                        BlockIdPrefix.generate(BlockIdPrefix.BINARY, expr),
                        infix
                );
                map.put(expr, b);
                factory.parseExpression(infix.getLeftOperand(), map).ifPresent(b::setLeftOperand);
                factory.parseExpression(infix.getRightOperand(), map).ifPresent(b::setRightOperand);
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    private boolean isComparisonOperator(InfixExpression.Operator op) {
        return op == InfixExpression.Operator.EQUALS ||
                op == InfixExpression.Operator.NOT_EQUALS ||
                op == InfixExpression.Operator.LESS ||
                op == InfixExpression.Operator.GREATER ||
                op == InfixExpression.Operator.LESS_EQUALS ||
                op == InfixExpression.Operator.GREATER_EQUALS ||
                op == InfixExpression.Operator.CONDITIONAL_AND ||
                op == InfixExpression.Operator.CONDITIONAL_OR;
    }

    // Helper methods for validation
    private boolean isEnumConstantReference(QualifiedName qn) {
        Name qualifier = qn.getQualifier();
        if (qualifier instanceof SimpleName) {
            String constantName = qn.getName().getIdentifier();
            return constantName.equals(constantName.toUpperCase());
        }
        return false;
    }

    private boolean isFieldAccessReference(QualifiedName qn) {
        Name qualifier = qn.getQualifier();
        if (qualifier instanceof SimpleName) {
            String qualifierName = ((SimpleName) qualifier).getIdentifier();
            if (qualifierName.equals("this") || qualifierName.equals("super")) {
                return true;
            }
            if (Character.isLowerCase(qualifierName.charAt(0))) {
                return true;
            }
        }
        return false;
    }
}