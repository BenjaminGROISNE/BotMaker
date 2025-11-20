package com.botmaker.ui;

import com.botmaker.blocks.*;
import com.botmaker.core.StatementBlock;

public enum AddableBlock {
    // Output
    PRINT("Print", PrintBlock.class),

    // Control Flow
    IF("If", IfBlock.class),
    WHILE("While Loop", WhileBlock.class),
    FOR("For Loop", ForBlock.class),
    BREAK("Break", BreakBlock.class),
    CONTINUE("Continue", ContinueBlock.class),

    // Variable Declaration
    DECLARE_INT("Declare Int", VariableDeclarationBlock.class),
    DECLARE_DOUBLE("Declare Double", VariableDeclarationBlock.class),
    DECLARE_BOOLEAN("Declare Boolean", VariableDeclarationBlock.class),
    DECLARE_STRING("Declare String", VariableDeclarationBlock.class),

    // Variable Operations
    ASSIGNMENT("Assignment", AssignmentBlock.class),
    INCREMENT("Increment (++)", IncrementDecrementBlock.class),
    DECREMENT("Decrement (--)", IncrementDecrementBlock.class),

    // Input
    READ_LINE("Read Line (String)", ReadInputBlock.class),
    READ_INT("Read Int", ReadInputBlock.class),
    READ_DOUBLE("Read Double", ReadInputBlock.class);

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