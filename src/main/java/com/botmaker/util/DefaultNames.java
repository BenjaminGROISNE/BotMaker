package com.botmaker.util;

/**
 * Provides sensible default names for auto-generated elements
 *
 * UPDATED: Uses TypeInfo for type classification
 */
public class DefaultNames {

    // Variable names by type
    public static final String DEFAULT_INT = "number";
    public static final String DEFAULT_DOUBLE = "decimal";
    public static final String DEFAULT_BOOLEAN = "flag";
    public static final String DEFAULT_STRING = "text";
    public static final String DEFAULT_VARIABLE = "variable";
    public static final String DEFAULT_ENUM = "value";

    /**
     * Get default name by type
     * UPDATED: Uses TypeInfo for classification
     */
    public static String forType(String typeName) {
        if (typeName == null) return DEFAULT_VARIABLE;

        // UPDATED: Use TypeInfo for proper type classification
        TypeInfo type = TypeInfo.from(typeName);

        // Get leaf type (unwrap arrays/collections)
        TypeInfo leafType = type.getLeafType();

        // Check type classifications
        if (leafType.isBoolean()) {
            return DEFAULT_BOOLEAN;
        }

        if (leafType.isString()) {
            return DEFAULT_STRING;
        }

        if (leafType.isNumeric()) {
            // Distinguish between integer and floating point
            String leafTypeName = leafType.getTypeName();
            if (leafTypeName.equals("double") || leafTypeName.equals("float") ||
                    leafTypeName.equals("Double") || leafTypeName.equals("Float")) {
                return DEFAULT_DOUBLE;
            }
            return DEFAULT_INT;
        }

        if (leafType.isEnum()) {
            return DEFAULT_ENUM;
        }

        return DEFAULT_VARIABLE;
    }

    /**
     * Get default name for enum type with the enum name as context
     * UPDATED: No changes needed - this method is already good
     */
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