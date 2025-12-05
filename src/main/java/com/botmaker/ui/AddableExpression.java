package com.botmaker.ui;

import com.botmaker.util.TypeManager;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AddableExpression {
    // ... (Keep existing literals: TEXT, NUMBER, TRUE, FALSE, VARIABLE, FUNCTION_CALL, ENUM_CONSTANT, LIST) ...
    TEXT("Text", "String"),
    NUMBER("Number", "number"),
    TRUE("True", "boolean"),
    FALSE("False", "boolean"),
    VARIABLE("Variable", "any"),
    FUNCTION_CALL("Function Call", "any"),
    ENUM_CONSTANT("Enum Value", "enum"),
    LIST("Sub-List", "list"),

    // Math
    ADD("Addition (+)", "+", "number"),
    SUBTRACT("Subtraction (-)", "-", "number"),
    MULTIPLY("Multiplication (*)", "*", "number"),
    DIVIDE("Division (/)", "/", "number"),
    MODULO("Modulo (%)", "%", "number"),

    // Comparisons
    EQUALS("Equals (==)", "==", "boolean"),
    NOT_EQUALS("Not Equals (!=)", "!=", "boolean"),
    GREATER("Greater (>)", ">", "boolean"),
    LESS("Less (<)", "<", "boolean"),
    GREATER_EQUALS("Greater Or Equal (>=)", ">=", "boolean"),
    LESS_EQUALS("Less Or Equal (<=)", "<=", "boolean"),

    // Logic
    AND("And (&&)", "&&", "boolean"),
    OR("Or (||)", "||", "boolean"),
    NOT("Not (!)", "!", "boolean"); // <-- NEW

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
    public String getReturnType() { return returnType; }

    public static List<AddableExpression> getForType(String targetType) {
        // ... (Keep existing logic) ...
        if (targetType == null || targetType.equals("any")) return Arrays.asList(values());

        if (targetType.equals(TypeManager.UI_TYPE_SWITCH_COMPATIBLE)) {
            return Arrays.stream(values())
                    .filter(e -> e == VARIABLE || e == TEXT || e == FUNCTION_CALL || e == ENUM_CONSTANT || e == NUMBER || e.returnType.equals("number"))
                    .collect(Collectors.toList());
        }

        if (targetType.equals("enum")) {
            return Arrays.stream(values())
                    .filter(e -> e == ENUM_CONSTANT || e == VARIABLE || e == FUNCTION_CALL)
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