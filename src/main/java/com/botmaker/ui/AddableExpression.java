package com.botmaker.ui;

import com.botmaker.util.TypeManager;
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

        // NEW: Filter for Switch Compatibility
        if (targetType.equals(TypeManager.UI_TYPE_SWITCH_COMPATIBLE)) {
            return Arrays.stream(values())
                    .filter(e -> {
                        // Explicitly allow Variable, Text, Function Call
                        if (e == VARIABLE || e == TEXT || e == FUNCTION_CALL) return true;

                        // Allow Number (creates 0, which is an int, thus valid)
                        if (e == NUMBER) return true;

                        // Allow math (creates int expressions usually)
                        if (e.returnType.equals("number")) return true;

                        // Exclude Boolean literals and Lists
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        return Arrays.stream(values())
                .filter(e -> {
                    if (e.returnType.equals("any")) return true;
                    return e.returnType.equals(targetType);
                })
                .collect(Collectors.toList());
    }
}