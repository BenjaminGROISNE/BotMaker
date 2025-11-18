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

    // Method to get default name by type
    public static String forType(String typeName) {
        if (typeName == null) return DEFAULT_VARIABLE;

        switch (typeName.toLowerCase()) {
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
                return DEFAULT_VARIABLE;
        }
    }

    private DefaultNames() {} // Prevent instantiation
}