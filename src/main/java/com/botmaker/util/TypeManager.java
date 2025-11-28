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
    public static final String UI_TYPE_TEXT = "Text";
    public static final String UI_TYPE_LIST = "list";
    public static final String UI_TYPE_ENUM = "enum"; // NEW
    // Special type for Switch statements
    public static final String UI_TYPE_SWITCH_COMPATIBLE = "switch_compatible";

    // --- Mapping Tables ---

    private static final List<String> FUNDAMENTAL_TYPES = List.of(
            "int", "double", "boolean", "String", "long", "float", "char"
    );

    private static final Set<String> HIDDEN_VARIABLES = Set.of(
            "args", "this", "super", "scanner", "class"
    );

    private static final Set<String> NUMBER_TYPES = Set.of(
            "int", "double", "float", "long", "short", "byte",
            "java.lang.Integer", "java.lang.Double", "java.lang.Float",
            "java.lang.Long", "java.lang.Short", "java.lang.Byte",
            "Integer", "Double", "Float", "Long", "Short", "Byte"
    );

    private static final Set<String> BOOLEAN_TYPES = Set.of(
            "boolean", "java.lang.Boolean", "Boolean"
    );

    private static final Set<String> STRING_TYPES = Set.of(
            "String", "java.lang.String", "char", "java.lang.Character", "Character"
    );

    // Types explicitly FORBIDDEN in a switch statement (pre-pattern matching)
    private static final Set<String> SWITCH_FORBIDDEN_TYPES = Set.of(
            "long", "java.lang.Long", "Long",
            "float", "java.lang.Float", "Float",
            "double", "java.lang.Double", "Double",
            "boolean", "java.lang.Boolean", "Boolean"
    );

    public static List<String> getFundamentalTypeNames() {
        return FUNDAMENTAL_TYPES;
    }

    public static boolean isUserVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) return false;
        String cleanName = variableName.split(" ")[0].split(":")[0].trim();
        if (HIDDEN_VARIABLES.contains(cleanName)) return false;
        if (cleanName.startsWith("_")) return false;
        return true;
    }

    // NEW: Check if a type is actually an enum by searching the compilation unit
    public static boolean isEnumType(String typeName, CompilationUnit cu) {
        if (typeName == null || typeName.isEmpty() || cu == null) return false;

        String clean = typeName.trim();

        // Extract base type if it's wrapped in ArrayList
        if (clean.startsWith("ArrayList<") && clean.endsWith(">")) {
            clean = clean.substring(10, clean.length() - 1);
        }

        // Search for enum declaration
        for (Object obj : cu.types()) {
            if (obj instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) obj;
                if (enumDecl.getName().getIdentifier().equals(clean)) {
                    return true;
                }
            }
            // Check class body declarations
            else if (obj instanceof TypeDeclaration) {
                TypeDeclaration typeDecl = (TypeDeclaration) obj;
                for (Object bodyObj : typeDecl.bodyDeclarations()) {
                    if (bodyObj instanceof EnumDeclaration) {
                        EnumDeclaration enumDecl = (EnumDeclaration) bodyObj;
                        if (enumDecl.getName().getIdentifier().equals(clean)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // Keep the old method but mark it as a heuristic
    public static boolean isLikelyEnumType(String typeName) {
        if (typeName == null || typeName.isEmpty()) return false;
        String clean = typeName.trim();

        // Extract base type if it's wrapped in ArrayList
        if (clean.startsWith("ArrayList<") && clean.endsWith(">")) {
            clean = clean.substring(10, clean.length() - 1);
        }

        // Heuristic: Enums typically start with uppercase and aren't in our known primitive/standard types
        if (!Character.isUpperCase(clean.charAt(0))) return false;
        if (FUNDAMENTAL_TYPES.contains(clean)) return false;
        if (STRING_TYPES.contains(clean)) return false;
        if (clean.equals("Integer") || clean.equals("Double") || clean.equals("Boolean")) return false;

        return true;
    }

    // Add an overload that takes CompilationUnit for accurate enum detection
    public static String determineUiType(String typeName, CompilationUnit cu) {
        if (typeName == null || typeName.isBlank()) return UI_TYPE_ANY;
        String clean = typeName.trim();

        if (isArrayList(clean)) return UI_TYPE_LIST;
        if (clean.endsWith("[]")) return UI_TYPE_LIST;

        if (NUMBER_TYPES.contains(clean)) return UI_TYPE_NUMBER;
        if (BOOLEAN_TYPES.contains(clean)) return UI_TYPE_BOOLEAN;
        if (STRING_TYPES.contains(clean)) return UI_TYPE_STRING;

        // Check if it's actually an enum
        if (isEnumType(clean, cu)) return UI_TYPE_ENUM;

        // Fallback to heuristic
        if (isLikelyEnumType(clean)) return UI_TYPE_ENUM;

        return UI_TYPE_ANY;
    }

    public static String determineUiType(String typeName) {
        if (typeName == null || typeName.isBlank()) return UI_TYPE_ANY;
        String clean = typeName.trim();

        if (isArrayList(clean)) return UI_TYPE_LIST;
        if (clean.endsWith("[]")) return UI_TYPE_LIST;

        if (NUMBER_TYPES.contains(clean)) return UI_TYPE_NUMBER;
        if (BOOLEAN_TYPES.contains(clean)) return UI_TYPE_BOOLEAN;
        if (STRING_TYPES.contains(clean)) return UI_TYPE_STRING;

        // NEW: Check if it looks like an enum
        if (isLikelyEnumType(clean)) return UI_TYPE_ENUM;

        return UI_TYPE_ANY;
    }

    // --- Type Compatibility (ITypeBinding) ---

    public static boolean isCompatible(ITypeBinding binding, String targetUiType) {
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;
        if (binding == null) return true;

        String qualifiedName = binding.getQualifiedName();
        String simpleName = binding.getName();

        // NEW: Handle Switch Compatibility - enums ARE allowed in switches
        if (targetUiType.equals(UI_TYPE_SWITCH_COMPATIBLE)) {
            if (binding.isArray()) return false;
            // Enums are compatible with switches
            if (binding.isEnum()) return true;
            // Blacklist check for forbidden types
            return !SWITCH_FORBIDDEN_TYPES.contains(simpleName) &&
                    !SWITCH_FORBIDDEN_TYPES.contains(qualifiedName);
        }

        if (binding.isArray()) {
            return targetUiType.equals(UI_TYPE_LIST) || targetUiType.endsWith("[]");
        }

        // NEW: Enum type matching
        if (targetUiType.equals(UI_TYPE_ENUM)) {
            return binding.isEnum() || isLikelyEnumType(simpleName);
        }

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

    // --- Type Compatibility (String) ---

    public static boolean isCompatible(String typeName, String targetUiType) {
        if (targetUiType == null || targetUiType.equals(UI_TYPE_ANY)) return true;
        if (typeName == null || typeName.isBlank()) return true;

        String cleanType = typeName.trim();

        // NEW: Handle Switch Compatibility - enums ARE allowed
        if (targetUiType.equals(UI_TYPE_SWITCH_COMPATIBLE)) {
            if (cleanType.endsWith("[]") || cleanType.contains("ArrayList")) return false;
            // Enums are switch-compatible
            if (isLikelyEnumType(cleanType)) return true;
            return !SWITCH_FORBIDDEN_TYPES.contains(cleanType);
        }

        if (cleanType.endsWith("[]")) {
            return targetUiType.equals(UI_TYPE_LIST) || targetUiType.endsWith("[]");
        }

        // NEW: Enum type matching
        if (targetUiType.equals(UI_TYPE_ENUM)) {
            return isLikelyEnumType(cleanType);
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

    public static String getFriendlyTypeName(ITypeBinding typeBinding) {
        if (typeBinding == null) return "unknown";
        if (typeBinding.isArray()) return "list";
        if (typeBinding.isEnum()) return "enum"; // NEW
        String name = typeBinding.getName();
        if (NUMBER_TYPES.contains(name)) return "number";
        if (STRING_TYPES.contains(name)) return "text";
        if (BOOLEAN_TYPES.contains(name)) return "bool";
        return name;
    }

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
            default: return typeName;
        }
    }

    public static boolean isPrimitive(String typeName) {
        if (typeName == null) return false;
        return typeName.equals("int") || typeName.equals("double") || typeName.equals("boolean") ||
                typeName.equals("char") || typeName.equals("long") || typeName.equals("float") ||
                typeName.equals("short") || typeName.equals("byte");
    }

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
            default: return typeName;
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

    public static boolean isArrayList(String typeName) {
        if (typeName == null) return false;
        return typeName.startsWith("ArrayList<") || typeName.equals("ArrayList");
    }

    public static Type createTypeNode(AST ast, String typeName) {
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            int start = typeName.indexOf("<") + 1;
            int end = typeName.lastIndexOf(">");
            String elementTypeName = typeName.substring(start, end);
            ParameterizedType paramType = ast.newParameterizedType(ast.newSimpleType(ast.newName("ArrayList")));
            Type elementType = createTypeNode(ast, elementTypeName);
            paramType.typeArguments().add(elementType);
            return paramType;
        }
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
            default: baseType = ast.newSimpleType(ast.newName(baseName)); break; // Includes enums and custom types
        }
        if (dimensions > 0) {
            return ast.newArrayType(baseType, dimensions);
        } else {
            return baseType;
        }
    }
}