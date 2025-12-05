package com.botmaker.ui;

import com.botmaker.util.TypeInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UPDATED: Uses TypeInfo instead of UI type strings
 * Removes "number", "boolean", "list", "enum" magic strings
 */
public enum AddableExpression {
    // Literals
    TEXT("Text", TypeInfo.STRING),
    NUMBER("Number", TypeInfo.INT),
    TRUE("True", TypeInfo.BOOLEAN),
    FALSE("False", TypeInfo.BOOLEAN),

    // References
    VARIABLE("Variable", TypeInfo.UNKNOWN),  // Can be any type
    FUNCTION_CALL("Function Call", TypeInfo.UNKNOWN),  // Return type depends on function
    ENUM_CONSTANT("Enum Value", null),  // Special: matches any enum
    LIST("Sub-List", null),  // Special: matches any array/list

    // Math operators (return numeric)
    ADD("Addition (+)", "+", TypeInfo.INT),
    SUBTRACT("Subtraction (-)", "-", TypeInfo.INT),
    MULTIPLY("Multiplication (*)", "*", TypeInfo.INT),
    DIVIDE("Division (/)", "/", TypeInfo.INT),
    MODULO("Modulo (%)", "%", TypeInfo.INT),

    // Comparison operators (return boolean)
    EQUALS("Equals (==)", "==", TypeInfo.BOOLEAN),
    NOT_EQUALS("Not Equals (!=)", "!=", TypeInfo.BOOLEAN),
    GREATER("Greater (>)", ">", TypeInfo.BOOLEAN),
    LESS("Less (<)", "<", TypeInfo.BOOLEAN),
    GREATER_EQUALS("Greater Or Equal (>=)", ">=", TypeInfo.BOOLEAN),
    LESS_EQUALS("Less Or Equal (<=)", "<=", TypeInfo.BOOLEAN),

    // Logic operators (return boolean)
    AND("And (&&)", "&&", TypeInfo.BOOLEAN),
    OR("Or (||)", "||", TypeInfo.BOOLEAN),
    NOT("Not (!)", "!", TypeInfo.BOOLEAN);

    private final String displayName;
    private final String operator;
    private final TypeInfo returnType;

    AddableExpression(String displayName, TypeInfo returnType) {
        this(displayName, null, returnType);
    }

    AddableExpression(String displayName, String operator, TypeInfo returnType) {
        this.displayName = displayName;
        this.operator = operator;
        this.returnType = returnType;
    }

    public String getDisplayName() { return displayName; }
    public String getOperator() { return operator; }
    public TypeInfo getReturnType() { return returnType; }

    /**
     * ✨ NEW: Filters expressions compatible with target type using TypeInfo
     */
    public static List<AddableExpression> getForType(TypeInfo targetType) {
        if (targetType == null || targetType.isUnknown()) {
            return Arrays.asList(values());
        }

        return Arrays.stream(values())
                .filter(expr -> expr.isCompatibleWith(targetType))
                .collect(Collectors.toList());
    }

    /**
     * ✨ NEW: Checks if this expression is compatible with a target type
     */
    public boolean isCompatibleWith(TypeInfo targetType) {
        if (targetType == null || targetType.isUnknown()) {
            return true;
        }

        // VARIABLE and FUNCTION_CALL can return any type
        if (this == VARIABLE || this == FUNCTION_CALL) {
            return true;
        }

        // ENUM_CONSTANT only compatible with enum types
        if (this == ENUM_CONSTANT) {
            return targetType.isEnum();
        }

        // LIST only compatible with array types
        if (this == LIST) {
            return targetType.isArray();
        }

        // For other expressions, check if return type is compatible
        if (returnType == null) {
            return true;  // Unknown return type - allow it
        }

        // Check type compatibility
        return returnType.isCompatibleWith(targetType);
    }

    /**
     * Helper: Check if expression is compatible with switch statements
     * Switch allows: int, String, enum (not long, float, double, boolean)
     */
    public boolean isSwitchCompatible() {
        if (this == VARIABLE || this == FUNCTION_CALL || this == ENUM_CONSTANT) {
            return true;
        }

        if (returnType == null) return false;

        // Check if it's a switch-compatible type
        String typeName = returnType.getTypeName();
        return typeName.equals("int") ||
                typeName.equals("String") ||
                typeName.equals("char") ||
                returnType.isEnum();
    }

    /**
     * ✨ DEPRECATED: Old string-based method for backward compatibility
     * Use getForType(TypeInfo) instead
     */
    @Deprecated
    public static List<AddableExpression> getForType(String targetType) {
        // Convert old string types to TypeInfo
        TypeInfo typeInfo = convertLegacyTypeString(targetType);
        return getForType(typeInfo);
    }

    /**
     * Helper to convert legacy UI type strings to TypeInfo
     */
    private static TypeInfo convertLegacyTypeString(String legacyType) {
        if (legacyType == null || legacyType.equals("any")) {
            return TypeInfo.UNKNOWN;
        }

        return switch (legacyType) {
            case "number" -> TypeInfo.INT;
            case "boolean" -> TypeInfo.BOOLEAN;
            case "String", "text" -> TypeInfo.STRING;
            case "list" -> TypeInfo.from("Object[]");  // Generic array
            case "switch_compatible" -> TypeInfo.INT;  // Most common switch type
            default -> TypeInfo.from(legacyType);  // Try to parse as actual type
        };
    }
}