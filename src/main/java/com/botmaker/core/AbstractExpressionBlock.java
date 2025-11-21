package com.botmaker.core;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class AbstractExpressionBlock extends AbstractCodeBlock implements ExpressionBlock {
    public AbstractExpressionBlock(String id, ASTNode astNode) {
        super(id, astNode);
    }
}
