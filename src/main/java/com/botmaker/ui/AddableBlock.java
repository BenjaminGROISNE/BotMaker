package com.botmaker.ui;

import com.botmaker.blocks.*;
import com.botmaker.core.StatementBlock;

public enum AddableBlock {
    // --- OUTPUT ---
    PRINT("Print", PrintBlock.class, BlockCategory.OUTPUT),

    // --- FLOW CONTROL ---
    IF("If Statement", IfBlock.class, BlockCategory.FLOW),
    WHILE("While Loop", WhileBlock.class, BlockCategory.LOOPS),
    FOR("For Each Loop", ForBlock.class, BlockCategory.LOOPS),
    DO_WHILE("Do While", DoWhileBlock.class, BlockCategory.LOOPS),
    SWITCH("Switch", SwitchBlock.class, BlockCategory.FLOW),
    CASE("Case", SwitchBlock.SwitchCaseBlock.class, BlockCategory.FLOW),
    CALL_FUNCTION("Call Function", MethodInvocationBlock.class, BlockCategory.FLOW),
    // --- CONTROL COMMANDS ---
    BREAK("Break", BreakBlock.class, BlockCategory.CONTROL),
    CONTINUE("Continue", ContinueBlock.class, BlockCategory.CONTROL),
    RETURN("Return", ReturnBlock.class, BlockCategory.CONTROL),
    WAIT("Wait (ms)", WaitBlock.class, BlockCategory.CONTROL),

    // --- VARIABLES ---
    DECLARE_INT("Int Variable", VariableDeclarationBlock.class, BlockCategory.VARIABLES),
    DECLARE_DOUBLE("Double Variable", VariableDeclarationBlock.class, BlockCategory.VARIABLES),
    DECLARE_BOOLEAN("Bool Variable", VariableDeclarationBlock.class, BlockCategory.VARIABLES),
    DECLARE_STRING("String Variable", VariableDeclarationBlock.class, BlockCategory.VARIABLES),

    // NEW: Generic Array Declaration
    DECLARE_ARRAY("Create List", VariableDeclarationBlock.class, BlockCategory.VARIABLES),

    ASSIGNMENT("Set Variable", AssignmentBlock.class, BlockCategory.VARIABLES),

    // --- INPUT ---
    READ_LINE("Read Text", ReadInputBlock.class, BlockCategory.INPUT),
    READ_INT("Read Int", ReadInputBlock.class, BlockCategory.INPUT),
    READ_DOUBLE("Read Double", ReadInputBlock.class, BlockCategory.INPUT),

    // --- UTILITY ---
    COMMENT("Comment", CommentBlock.class, BlockCategory.UTILITY);


    private final String displayName;
    private final Class<? extends StatementBlock> blockClass;
    private final BlockCategory category;

    AddableBlock(String displayName, Class<? extends StatementBlock> blockClass, BlockCategory category) {
        this.displayName = displayName;
        this.blockClass = blockClass;
        this.category = category;
    }

    public String getDisplayName() { return displayName; }
    public Class<? extends StatementBlock> getBlockClass() { return blockClass; }
    public BlockCategory getCategory() { return category; }

    public enum BlockCategory {
        OUTPUT("Output"),
        INPUT("Input"),
        VARIABLES("Variables"),
        FLOW("Logic"),
        LOOPS("Loops"),
        CONTROL("Control"),
        UTILITY("Utility");

        private final String label;
        BlockCategory(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}