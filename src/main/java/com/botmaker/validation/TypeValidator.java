package com.botmaker.validation;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Simple, user-friendly type validation for non-coders.
 * Prevents common mistakes without technical jargon.
 */
public class TypeValidator {

    // Variables that should be hidden from users (system/special variables)
    private static final Set<String> HIDDEN_VARIABLES = Set.of(
            "args",      // Command line arguments array
            "this",      // Current object reference
            "super",     // Parent class reference
            "scanner"    // System scanner object
    );

    /**
     * Check if a variable should be visible to users in autocomplete
     */
    public static boolean isUserVariable(String variableName) {
        if (variableName == null) return false;

        // Hide system variables
        if (HIDDEN_VARIABLES.contains(variableName)) {
            return false;
        }

        // Hide variables starting with underscore (convention for internal)
        if (variableName.startsWith("_")) {
            return false;
        }

        return true;
    }

    /**
     * Get a user-friendly type name (no technical jargon)
     */
    public static String getFriendlyTypeName(ITypeBinding typeBinding) {
        if (typeBinding == null) return "unknown";

        String typeName = typeBinding.getName();

        // Handle arrays
        if (typeBinding.isArray()) {
            ITypeBinding elementType = typeBinding.getElementType();
            return "list of " + getFriendlyTypeName(elementType);
        }

        // Handle common types with friendly names
        switch (typeName) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "short":
            case "Short":
            case "byte":
            case "Byte":
                return "number (whole)";

            case "double":
            case "Double":
            case "float":
            case "Float":
                return "number (decimal)";

            case "boolean":
            case "Boolean":
                return "true/false";

            case "String":
                return "text";

            case "char":
            case "Character":
                return "character";

            default:
                // For collections, extract simple name
                if (typeName.contains("<")) {
                    return "collection";
                }
                return typeName;
        }
    }

    /**
     * Check if two types are compatible for assignment
     */
    public static boolean areTypesCompatible(ITypeBinding targetType, ITypeBinding sourceType) {
        if (targetType == null || sourceType == null) return true; // Can't check, allow

        // Same type - always OK
        if (targetType.isEqualTo(sourceType)) return true;

        // Handle numeric widening (safe conversions)
        if (isNumericWideningAllowed(targetType, sourceType)) return true;

        // Check if source is subtype of target (inheritance)
        if (sourceType.isAssignmentCompatible(targetType)) return true;

        return false;
    }

    /**
     * Check if numeric widening conversion is allowed (safe)
     */
    private static boolean isNumericWideningAllowed(ITypeBinding target, ITypeBinding source) {
        String targetName = target.getQualifiedName();
        String sourceName = source.getQualifiedName();

        // Widening conversions that don't lose data
        Map<String, Set<String>> wideningMap = Map.of(
                "int", Set.of("byte", "short"),
                "long", Set.of("byte", "short", "int"),
                "float", Set.of("byte", "short", "int", "long"),
                "double", Set.of("byte", "short", "int", "long", "float")
        );

        Set<String> allowedSources = wideningMap.get(targetName);
        return allowedSources != null && allowedSources.contains(sourceName);
    }

    /**
     * Get a user-friendly error message for type mismatch
     */
    public static String getTypeMismatchMessage(ITypeBinding expected, ITypeBinding actual) {
        String expectedName = getFriendlyTypeName(expected);
        String actualName = getFriendlyTypeName(actual);

        return String.format(
                "Type mismatch: You're using %s but %s is needed here.",
                actualName,
                expectedName
        );
    }

    /**
     * Check if an expression can be used as a boolean (in if/while conditions)
     */
    public static boolean isBooleanExpression(Expression expr) {
        if (expr == null) return false;

        ITypeBinding typeBinding = expr.resolveTypeBinding();
        if (typeBinding == null) return true; // Can't check, allow

        return typeBinding.getName().equals("boolean") ||
                typeBinding.getName().equals("Boolean");
    }

    /**
     * Check if an expression is numeric
     */
    public static boolean isNumericExpression(Expression expr) {
        if (expr == null) return false;

        ITypeBinding typeBinding = expr.resolveTypeBinding();
        if (typeBinding == null) return true; // Can't check, allow

        String name = typeBinding.getName();
        return name.equals("int") || name.equals("Integer") ||
                name.equals("long") || name.equals("Long") ||
                name.equals("float") || name.equals("Float") ||
                name.equals("double") || name.equals("Double") ||
                name.equals("byte") || name.equals("Byte") ||
                name.equals("short") || name.equals("Short");
    }

    /**
     * Check if an expression is a collection (List, array, etc.)
     */
    public static boolean isCollectionExpression(Expression expr) {
        if (expr == null) return false;

        ITypeBinding typeBinding = expr.resolveTypeBinding();
        if (typeBinding == null) return false;

        return typeBinding.isArray() ||
                typeBinding.getName().contains("List") ||
                typeBinding.getName().contains("Collection") ||
                typeBinding.getName().contains("Set");
    }

    /**
     * Suggest appropriate variable name based on type
     */
    public static String suggestVariableName(ITypeBinding typeBinding) {
        if (typeBinding == null) return "value";

        String typeName = typeBinding.getName();

        if (typeBinding.isArray()) {
            return "items";
        }

        switch (typeName) {
            case "int":
            case "Integer":
            case "long":
            case "Long":
                return "number";

            case "double":
            case "Double":
            case "float":
            case "Float":
                return "decimal";

            case "boolean":
            case "Boolean":
                return "flag";

            case "String":
                return "text";

            case "char":
            case "Character":
                return "letter";

            default:
                if (typeName.contains("List")) return "list";
                if (typeName.contains("Map")) return "dictionary";
                if (typeName.contains("Set")) return "collection";
                return "value";
        }
    }
}