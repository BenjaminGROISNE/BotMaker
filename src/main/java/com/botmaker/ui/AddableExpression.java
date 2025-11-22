package com.botmaker.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AddableExpression {
    // Literals
    TEXT("Text", "String"),
    NUMBER("Number", "number"),
    TRUE("True", "boolean"),
    FALSE("False", "boolean"),
    VARIABLE("Variable", "any"),

    // NEW: Function Call
    FUNCTION_CALL("Function Call", "any"), // Can return anything

    // Nested
    LIST("Sub-List", "list"),

    // Math Operations
    ADD("Addition (+)", "+", "number"),
    SUBTRACT("Subtraction (-)", "-", "number"),
    MULTIPLY("Multiplication (*)", "*", "number"),
    DIVIDE("Division (/)", "/", "number"),
    MODULO("Modulo (%)", "%", "number");

    private final String displayName;
    private final String operator;
    private final String returnType;

    AddableExpression(String displayName, String returnType) {
        this(displayName, null, returnType);
    }

    AddableExpression(String displayName, String operator, String returnType) {
        this.displayName = displayName;
        this.operator = operator;
        this.returnType = returnType;
    }

    public String getDisplayName() { return displayName; }
    public String getOperator() { return operator; }

    public static List<AddableExpression> getForType(String targetType) {
        if (targetType == null || targetType.equals("any")) {
            return Arrays.asList(values());
        }
        return Arrays.stream(values())
                .filter(e -> {
                    if (e.returnType.equals("any")) return true;
                    return e.returnType.equals(targetType);
                })
                .collect(Collectors.toList());
    }
}