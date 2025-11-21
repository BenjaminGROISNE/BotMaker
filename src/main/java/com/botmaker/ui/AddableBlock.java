package com.botmaker.ui;

import com.botmaker.blocks.*;
import com.botmaker.core.StatementBlock;

public enum AddableBlock {
    // Output
    PRINT("Print", PrintBlock.class),

    // Control Flow
    IF("If", IfBlock.class),
    WHILE("While Loop", WhileBlock.class),
    FOR("For Each", ForBlock.class),
    DO_WHILE("Do While", DoWhileBlock.class),
    SWITCH("Switch", SwitchBlock.class),
    CASE("Case", SwitchBlock.SwitchCaseBlock.class),
    BREAK("Break", BreakBlock.class),
    CONTINUE("Continue", ContinueBlock.class),
    RETURN("Return", ReturnBlock.class),  // Added Return
    WAIT("Wait (ms)", WaitBlock.class),   // Added Wait

    // Variable Declaration
    DECLARE_INT("Declare Int", VariableDeclarationBlock.class),
    DECLARE_DOUBLE("Declare Double", VariableDeclarationBlock.class),
    DECLARE_BOOLEAN("Declare Boolean", VariableDeclarationBlock.class),
    DECLARE_STRING("Declare String", VariableDeclarationBlock.class),

    // Variable Operations
    ASSIGNMENT("Assignment", AssignmentBlock.class),

    // Input
    READ_LINE("Read Line (String)", ReadInputBlock.class),
    READ_INT("Read Int", ReadInputBlock.class),
    READ_DOUBLE("Read Double", ReadInputBlock.class),

    // Comment
    COMMENT("Comment", CommentBlock.class);

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