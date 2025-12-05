package com.botmaker.ui;

import com.botmaker.util.TypeInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AddableExpression {
    // Literals
    TEXT("Text", "text", TypeInfo.STRING, Category.LITERAL, true),
    NUMBER("Number", "0", TypeInfo.INT, Category.LITERAL, true),
    TRUE("True", "true", TypeInfo.BOOLEAN, Category.LITERAL, true),
    FALSE("False", "false", TypeInfo.BOOLEAN, Category.LITERAL, true),

    // References
    VARIABLE("Variable", null, TypeInfo.UNKNOWN, Category.REFERENCE, false),
    FUNCTION_CALL("Function Call", null, TypeInfo.UNKNOWN, Category.REFERENCE, false),
    ENUM_CONSTANT("Enum Value", null, null, Category.LITERAL, true), // Special: compatibility checked dynamically
    LIST("Sub-List", null, null, Category.STRUCTURE, false),

    // Math
    ADD("Addition (+)", "+", TypeInfo.INT, Category.MATH, false),
    SUBTRACT("Subtraction (-)", "-", TypeInfo.INT, Category.MATH, false),
    MULTIPLY("Multiplication (*)", "*", TypeInfo.INT, Category.MATH, false),
    DIVIDE("Division (/)", "/", TypeInfo.INT, Category.MATH, false),
    MODULO("Modulo (%)", "%", TypeInfo.INT, Category.MATH, false),

    // Comparison
    EQUALS("Equals (==)", "==", TypeInfo.BOOLEAN, Category.COMPARISON, false),
    NOT_EQUALS("Not Equals (!=)", "!=", TypeInfo.BOOLEAN, Category.COMPARISON, false),
    GREATER("Greater (>)", ">", TypeInfo.BOOLEAN, Category.COMPARISON, false),
    LESS("Less (<)", "<", TypeInfo.BOOLEAN, Category.COMPARISON, false),
    GREATER_EQUALS("Greater Or Equal (>=)", ">=", TypeInfo.BOOLEAN, Category.COMPARISON, false),
    LESS_EQUALS("Less Or Equal (<=)", "<=", TypeInfo.BOOLEAN, Category.COMPARISON, false),

    // Logic
    AND("And (&&)", "&&", TypeInfo.BOOLEAN, Category.LOGIC, false),
    OR("Or (||)", "||", TypeInfo.BOOLEAN, Category.LOGIC, false),
    NOT("Not (!)", "!", TypeInfo.BOOLEAN, Category.LOGIC, false);

    private final String displayName;
    private final String operator;
    private final TypeInfo returnType;
    private final Category category;
    private final boolean isConstant;

    AddableExpression(String displayName, String operator, TypeInfo returnType, Category category, boolean isConstant) {
        this.displayName = displayName;
        this.operator = operator;
        this.returnType = returnType;
        this.category = category;
        this.isConstant = isConstant;
    }

    public String getDisplayName() { return displayName; }
    public Category getCategory() { return category; }
    public boolean isConstant() { return isConstant; }

    public enum Category {
        LITERAL("Values"),
        REFERENCE("References"),
        MATH("Math"),
        COMPARISON("Comparison"),
        LOGIC("Logic"),
        STRUCTURE("Structure");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public static List<AddableExpression> getForType(TypeInfo targetType) {
        return getForType(targetType, false);
    }

    public static List<AddableExpression> getForType(TypeInfo targetType, boolean constantOnly) {
        if (targetType == null || targetType.isUnknown()) {
            // If unknown target, allow everything (unless constant filtering is on)
            return Arrays.stream(values())
                    .filter(e -> !constantOnly || e.isConstant)
                    .collect(Collectors.toList());
        }

        return Arrays.stream(values())
                .filter(expr -> !constantOnly || expr.isConstant)
                .filter(expr -> expr.isCompatibleWith(targetType))
                .collect(Collectors.toList());
    }

    public boolean isCompatibleWith(TypeInfo targetType) {
        if (targetType == null || targetType.isUnknown()) return true;

        // Special case: Enum Constant only matches Enums
        if (this == ENUM_CONSTANT) return targetType.isEnum();

        // Special case: List only matches Arrays
        if (this == LIST) return targetType.isArray();

        // Variables and Functions can be anything
        if (this == VARIABLE || this == FUNCTION_CALL) return true;

        if (returnType == null) return true;

        return returnType.isCompatibleWith(targetType);
    }
}