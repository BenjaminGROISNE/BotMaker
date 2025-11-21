package com.botmaker.core;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class AbstractStatementBlock extends AbstractCodeBlock implements StatementBlock {
    public AbstractStatementBlock(String id, ASTNode astNode) {
        super(id, astNode);
    }
}
