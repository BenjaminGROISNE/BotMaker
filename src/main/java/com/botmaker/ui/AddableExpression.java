package com.botmaker.ui;

public enum AddableExpression {
    TEXT("Text"),
    VARIABLE("Variable");

    private final String displayName;

    AddableExpression(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
