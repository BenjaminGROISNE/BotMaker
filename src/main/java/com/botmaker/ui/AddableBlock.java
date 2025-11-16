package com.botmaker.ui;

import com.botmaker.blocks.IfBlock;
import com.botmaker.blocks.PrintBlock;
import com.botmaker.blocks.VariableDeclarationBlock;
import com.botmaker.core.StatementBlock;

public enum AddableBlock {
    PRINT("Print", PrintBlock.class),
    IF("If", IfBlock.class),
    DECLARE_INT("Declare Int", VariableDeclarationBlock.class),
    DECLARE_DOUBLE("Declare Double", VariableDeclarationBlock.class),
    DECLARE_BOOLEAN("Declare Boolean", VariableDeclarationBlock.class),
    DECLARE_STRING("Declare String", VariableDeclarationBlock.class);

    private final String displayName;
    private final Class<? extends StatementBlock> blockClass;

    AddableBlock(String displayName, Class<? extends StatementBlock> blockClass) {
        this.displayName = displayName;
        this.blockClass = blockClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Class<? extends StatementBlock> getBlockClass() {
        return blockClass;
    }
}
