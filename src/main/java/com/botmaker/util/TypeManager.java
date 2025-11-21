package com.botmaker.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeManager {

    // --- Constants for UI Categories ---
    public static final String UI_TYPE_ANY = "any";
    public static final String UI_TYPE_NUMBER = "number";
    public static final String UI_TYPE_BOOLEAN = "boolean";
    public static final String UI_TYPE_STRING = "String";
    public static final String UI_TYPE_TEXT = "Text"; // Alias for String
    public static final String UI_TYPE_LIST = "list";

    // --- Mapping Tables ---

    // Fundamental types for dropdown menus
    private static final List<String> FUNDAMENTAL_TYPES = List.of(
            "int", "double", "boolean", "String", "long", "float", "char"
    );

    // Hidden variables that shouldn't appear in autocomplete
    private static final Set<String> HIDDEN_VARIABLES = Set.of(
            "args", "this", "super", "scanner", "class"
    );

    // Groupings for Type Checking
    private static final Set<String> NUMBER_TYPES = Set.of(
            "int", "double", "float", "long", "short", "byte",
            "java.lang.Integer", "java.lang.Double", "java.lang.Float",
            "java.lang.Long", "java.lang.Short", "java.lang.Byte",
            "Integer", "Double", "Float", "Long", "Short", "Byte" // Simple names
    );

    private static final Set<String> BOOLEAN_TYPES = Set.of(
            "boolean", "java.lang.Boolean", "Boolean"
    );

    private static final Set<String> STRING_TYPES = Set.of(
            "String", "java.lang.String", "char", "java.lang.Character", "Character"
    );

    public static List<String> getFundamentalTypeNames() {
        return FUNDAMENTAL_TYPES;
    }

    // --- Validation Logic ---

    public static boolean isUserVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) return false;
        // Remove type hints if present (e.g. "myVar : int")
        String cleanName = variableName.split(" ")[0].split(":")[0].trim();
        if (HIDDEN_VARIABLES.contains(cleanName)) return false;
        if (cleanName.startsWith("_")) return false;
        return true;
    }

    // --- Type Compatibility (ITypeBinding - Precise) ---

    public static boolean isCompatible(ITypeBinding binding, String targetUiType) {
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;
        if (binding == null) return true; // Optimistic fallback if binding missing

        // Handle Arrays/Lists
        if (binding.isArray()) {
            return targetUiType.equals(UI_TYPE_LIST) || targetUiType.endsWith("[]");
        }

        String qualifiedName = binding.getQualifiedName();
        String simpleName = binding.getName();

        switch (targetUiType) {
            case UI_TYPE_NUMBER:
                return NUMBER_TYPES.contains(simpleName) || NUMBER_TYPES.contains(qualifiedName);
            case UI_TYPE_BOOLEAN:
                return BOOLEAN_TYPES.contains(simpleName) || BOOLEAN_TYPES.contains(qualifiedName);
            case UI_TYPE_STRING:
            case UI_TYPE_TEXT:
                return STRING_TYPES.contains(simpleName) || STRING_TYPES.contains(qualifiedName);
            default:
                // Exact match fallback
                return targetUiType.equals(simpleName) || targetUiType.equals(qualifiedName);
        }
    }

    // --- Type Compatibility (String - Fallback for LSP) ---

    /**
     * Checks compatibility using String names.
     * Used when processing LSP completion items where we only have text labels/details.
     */
    public static boolean isCompatible(String typeName, String targetUiType) {
        // If target accepts anything, we are good.
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;

        // CRITICAL FIX: If type is unknown (null/empty), allow it (Fail Open).
        // It is better to show an invalid variable than to hide a valid one because the server was lazy.
        if (typeName == null || typeName.isBlank()) return true;

        String cleanType = typeName.trim();

        // Handle Array notation in string
        if (cleanType.endsWith("[]")) {
            return targetUiType.equals(UI_TYPE_LIST) || targetUiType.endsWith("[]");
        }

        switch (targetUiType) {
            case UI_TYPE_NUMBER:
                return NUMBER_TYPES.contains(cleanType);
            case UI_TYPE_BOOLEAN:
                return BOOLEAN_TYPES.contains(cleanType);
            case UI_TYPE_STRING:
            case UI_TYPE_TEXT:
                return STRING_TYPES.contains(cleanType) || cleanType.endsWith("String");
            default:
                return cleanType.equals(targetUiType);
        }
    }

    // --- Display Utilities ---

    public static String getFriendlyTypeName(ITypeBinding typeBinding) {
        if (typeBinding == null) return "unknown";
        if (typeBinding.isArray()) return "list";

        String name = typeBinding.getName();
        if (NUMBER_TYPES.contains(name)) return "number";
        if (STRING_TYPES.contains(name)) return "text";
        if (BOOLEAN_TYPES.contains(name)) return "bool";

        return name;
    }

    // --- AST Node Creation ---

    public static Type createTypeNode(AST ast, String typeName) {
        int dimensions = 0;
        String baseName = typeName;

        while (baseName.endsWith("[]")) {
            dimensions++;
            baseName = baseName.substring(0, baseName.length() - 2);
        }

        Type baseType;
        switch (baseName) {
            case "int": baseType = ast.newPrimitiveType(PrimitiveType.INT); break;
            case "double": baseType = ast.newPrimitiveType(PrimitiveType.DOUBLE); break;
            case "boolean": baseType = ast.newPrimitiveType(PrimitiveType.BOOLEAN); break;
            case "char": baseType = ast.newPrimitiveType(PrimitiveType.CHAR); break;
            case "long": baseType = ast.newPrimitiveType(PrimitiveType.LONG); break;
            case "float": baseType = ast.newPrimitiveType(PrimitiveType.FLOAT); break;
            case "short": baseType = ast.newPrimitiveType(PrimitiveType.SHORT); break;
            case "byte": baseType = ast.newPrimitiveType(PrimitiveType.BYTE); break;
            default: baseType = ast.newSimpleType(ast.newName(baseName)); break;
        }

        if (dimensions > 0) {
            return ast.newArrayType(baseType, dimensions);
        } else {
            return baseType;
        }
    }
}