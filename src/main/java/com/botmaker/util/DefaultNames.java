package com.botmaker.util;

/**
 * Provides sensible default names for auto-generated elements
 */
public class DefaultNames {

    // Variable names by type
    public static final String DEFAULT_INT = "number";
    public static final String DEFAULT_DOUBLE = "decimal";
    public static final String DEFAULT_BOOLEAN = "flag";
    public static final String DEFAULT_STRING = "text";
    public static final String DEFAULT_VARIABLE = "variable";
    public static final String DEFAULT_ENUM = "value"; // NEW

    // Method to get default name by type
    public static String forType(String typeName) {
        if (typeName == null) return DEFAULT_VARIABLE;

        String cleanType = typeName.trim();

        // Handle ArrayList wrapper
        if (cleanType.startsWith("ArrayList<") && cleanType.endsWith(">")) {
            cleanType = cleanType.substring(10, cleanType.length() - 1);
        }

        switch (cleanType.toLowerCase()) {
            case "int":
            case "long":
            case "short":
            case "byte":
                return DEFAULT_INT;
            case "double":
            case "float":
                return DEFAULT_DOUBLE;
            case "boolean":
                return DEFAULT_BOOLEAN;
            case "string":
                return DEFAULT_STRING;
            default:
                // NEW: If it looks like an enum (starts with uppercase), use enum default
                if (TypeManager.isLikelyEnumType(cleanType)) {
                    return DEFAULT_ENUM;
                }
                return DEFAULT_VARIABLE;
        }
    }

    // NEW: Get default name for enum type with the enum name as context
    public static String forEnumType(String enumTypeName) {
        if (enumTypeName == null || enumTypeName.isEmpty()) {
            return DEFAULT_ENUM;
        }
        // Convert enum name to camelCase variable name
        // e.g., "Color" -> "color", "DayOfWeek" -> "dayOfWeek"
        String camelCase = Character.toLowerCase(enumTypeName.charAt(0)) + enumTypeName.substring(1);
        return camelCase;
    }

    private DefaultNames() {} // Prevent instantiation
}