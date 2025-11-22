package com.botmaker.util;

import org.eclipse.jdt.core.dom.*;

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
        String cleanName = variableName.split(" ")[0].split(":")[0].trim();
        if (HIDDEN_VARIABLES.contains(cleanName)) return false;
        if (cleanName.startsWith("_")) return false;
        return true;
    }

    // --- UI Type Determination ---

    /**
     * Determines the UI Category (number, boolean, String, list) from a Java type name.
     * Used to filter available expressions in blocks.
     */
    public static String determineUiType(String typeName) {
        if (typeName == null || typeName.isBlank()) return UI_TYPE_ANY;
        String clean = typeName.trim();

        // Check for ArrayList first
        if (isArrayList(clean)) return UI_TYPE_LIST;

        // Then check for arrays
        if (clean.endsWith("[]")) return UI_TYPE_LIST;

        if (NUMBER_TYPES.contains(clean)) return UI_TYPE_NUMBER;
        if (BOOLEAN_TYPES.contains(clean)) return UI_TYPE_BOOLEAN;
        if (STRING_TYPES.contains(clean)) return UI_TYPE_STRING;

        return UI_TYPE_ANY;
    }


    // --- Type Compatibility (ITypeBinding) ---

    public static boolean isCompatible(ITypeBinding binding, String targetUiType) {
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;
        if (binding == null) return true;

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
                return targetUiType.equals(simpleName) || targetUiType.equals(qualifiedName);
        }
    }

    /**
     * Determines if a type string represents an ArrayList
     */
    public static boolean isArrayList(String typeName) {
        if (typeName == null) return false;
        return typeName.startsWith("ArrayList<") || typeName.equals("ArrayList");
    }



    // --- Type Compatibility (String) ---

    public static boolean isCompatible(String typeName, String targetUiType) {
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;
        if (typeName == null || typeName.isBlank()) return true;

        String cleanType = typeName.trim();

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

    /**
     * Converts primitive type names to their wrapper classes.
     * Required for ArrayList and other generics which don't support primitives.
     *
     * @param typeName The type name to convert (e.g., "int", "boolean")
     * @return The wrapper class name (e.g., "Integer", "Boolean")
     */
    public static String toWrapperType(String typeName) {
        if (typeName == null) return typeName;

        switch (typeName) {
            case "int": return "Integer";
            case "double": return "Double";
            case "boolean": return "Boolean";
            case "char": return "Character";
            case "long": return "Long";
            case "float": return "Float";
            case "short": return "Short";
            case "byte": return "Byte";
            default: return typeName; // Already a reference type
        }
    }

    /**
     * Checks if a type name is a primitive type.
     *
     * @param typeName The type name to check
     * @return true if the type is a Java primitive
     */
    public static boolean isPrimitive(String typeName) {
        if (typeName == null) return false;

        return typeName.equals("int") ||
                typeName.equals("double") ||
                typeName.equals("boolean") ||
                typeName.equals("char") ||
                typeName.equals("long") ||
                typeName.equals("float") ||
                typeName.equals("short") ||
                typeName.equals("byte");
    }

    /**
     * Converts wrapper class names back to primitives.
     * Opposite of toWrapperType().
     *
     * @param typeName The wrapper class name (e.g., "Integer")
     * @return The primitive type name (e.g., "int")
     */
    public static String toPrimitiveType(String typeName) {
        if (typeName == null) return typeName;

        switch (typeName) {
            case "Integer": return "int";
            case "Double": return "double";
            case "Boolean": return "boolean";
            case "Character": return "char";
            case "Long": return "long";
            case "Float": return "float";
            case "Short": return "short";
            case "Byte": return "byte";
            default: return typeName; // Not a wrapper
        }
    }

    public static int getListNestingLevel(String typeName) {
        if (typeName == null) return 0;
        int level = 0;
        String temp = typeName;
        while (temp.startsWith("ArrayList<") || temp.startsWith("List<")) {
            level++;
            int start = temp.indexOf("<") + 1;
            int end = temp.lastIndexOf(">");
            if (start < end) {
                temp = temp.substring(start, end);
            } else {
                break;
            }
        }
        return level;
    }

    public static String getLeafType(String typeName) {
        if (typeName == null) return "Object";
        String temp = typeName;
        while (temp.startsWith("ArrayList<") || temp.startsWith("List<")) {
            int start = temp.indexOf("<") + 1;
            int end = temp.lastIndexOf(">");
            if (start < end) {
                temp = temp.substring(start, end);
            } else {
                break;
            }
        }
        return temp;
    }

    public static Type createTypeNode(AST ast, String typeName) {
        // Handle ArrayList<Type>
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            // Extract element type
            int start = typeName.indexOf("<") + 1;
            int end = typeName.lastIndexOf(">");
            String elementTypeName = typeName.substring(start, end);

            // Create parameterized type: ArrayList<ElementType>
            ParameterizedType paramType = ast.newParameterizedType(
                    ast.newSimpleType(ast.newName("ArrayList"))
            );

            // Handle nested ArrayList recursively
            Type elementType = createTypeNode(ast, elementTypeName);
            paramType.typeArguments().add(elementType);

            return paramType;
        }

        // Handle arrays
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