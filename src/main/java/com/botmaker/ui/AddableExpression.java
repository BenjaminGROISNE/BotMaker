package com.botmaker.ui;

public enum AddableExpression {
    // Literals
    TEXT("Text"),
    NUMBER("Number"),
    VARIABLE("Variable"),

    // Math Operations
    ADD("Addition (+)", "+"),
    SUBTRACT("Subtraction (-)", "-"),
    MULTIPLY("Multiplication (*)", "*"),
    DIVIDE("Division (/)", "/"),
    MODULO("Modulo (%)", "%");

    private final String displayName;
    private final String operator; // null for non-operations

    AddableExpression(String displayName) {
        this.displayName = displayName;
        this.operator = null;
    }

    AddableExpression(String displayName, String operator) {
        this.displayName = displayName;
        this.operator = operator;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isOperation() {
        return operator != null;
    }
}