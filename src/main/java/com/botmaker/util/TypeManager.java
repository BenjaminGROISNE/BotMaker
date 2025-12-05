package com.botmaker.util;

import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Set;

/**
 * UPDATED TypeManager - Slim utility for AST/JDT-specific operations.
 *
 * REMOVED: All type logic moved to TypeInfo
 * KEPT: AST manipulation, UI helpers, compiler-specific operations
 *
 * Philosophy: TypeManager handles "how to work with Eclipse JDT AST"
 *            TypeInfo handles "what types are and how they relate"
 */
public class TypeManager {

    // ========================================================================
    // AST NODE CREATION (Keep - This is AST-specific, not type logic)
    // ========================================================================

    /**
     * Creates an AST Type node from TypeInfo
     */
    public static Type createTypeNode(AST ast, TypeInfo typeInfo) {
        return createTypeNode(ast, typeInfo.getTypeName());
    }

    /**
     * Creates an AST Type node from string type name
     * Handles primitives, arrays, and reference types
     */
    public static Type createTypeNode(AST ast, String typeName) {
        // Count array dimensions
        int dimensions = 0;
        String baseName = typeName;
        while (baseName.endsWith("[]")) {
            dimensions++;
            baseName = baseName.substring(0, baseName.length() - 2).trim();
        }

        // Create base type
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
            case "void": baseType = ast.newPrimitiveType(PrimitiveType.VOID); break;
            default: baseType = ast.newSimpleType(ast.newName(baseName)); break;
        }

        // Add array dimensions if needed
        if (dimensions > 0) {
            return ast.newArrayType(baseType, dimensions);
        }
        return baseType;
    }

    // ========================================================================
    // ENUM DETECTION IN COMPILATION UNIT (Keep - This searches AST structure)
    // ========================================================================

    /**
     * Searches CompilationUnit to verify if a type is actually an enum.
     * Complements TypeInfo.isEnum() which uses heuristics for unbound types.
     */
    public static boolean isEnumType(TypeInfo type, CompilationUnit cu) {
        // If we have binding, TypeInfo already knows
        if (type.hasBinding()) {
            return type.isEnum();
        }

        // Otherwise search the AST
        return findEnumDeclaration(cu, type.getLeafType().getTypeName()) != null;
    }

    /**
     * Finds an enum declaration by name in the compilation unit
     */
    public static EnumDeclaration findEnumDeclaration(CompilationUnit cu, String enumName) {
        if (cu == null || enumName == null) return null;

        // Search top-level types
        for (Object obj : cu.types()) {
            if (obj instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) obj;
                if (enumDecl.getName().getIdentifier().equals(enumName)) {
                    return enumDecl;
                }
            }
            // Check class body declarations
            else if (obj instanceof TypeDeclaration) {
                TypeDeclaration typeDecl = (TypeDeclaration) obj;
                EnumDeclaration found = findEnumInTypeDeclaration(typeDecl, enumName);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static EnumDeclaration findEnumInTypeDeclaration(TypeDeclaration typeDecl, String enumName) {
        for (Object bodyObj : typeDecl.bodyDeclarations()) {
            if (bodyObj instanceof EnumDeclaration) {
                EnumDeclaration enumDecl = (EnumDeclaration) bodyObj;
                if (enumDecl.getName().getIdentifier().equals(enumName)) {
                    return enumDecl;
                }
            }
        }
        return null;
    }

    /**
     * Gets all enum constant names from an enum declaration
     */
    public static List<String> getEnumConstantNames(EnumDeclaration enumDecl) {
        return enumDecl.enumConstants().stream()
                .map(obj -> ((EnumConstantDeclaration) obj).getName().getIdentifier())
                .toList();
    }

    // ========================================================================
    // UI HELPERS (Keep - These are UI/display concerns, not type logic)
    // ========================================================================

    /**
     * List of fundamental types for type selection menus
     */
    public static List<String> getFundamentalTypeNames() {
        return List.of("int", "double", "boolean", "String", "long", "float", "char");
    }

    /**
     * Filters out system/internal variables from user-visible lists
     */
    public static boolean isUserVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) return false;
        String cleanName = variableName.split(" ")[0].split(":")[0].trim();
        return !HIDDEN_VARIABLES.contains(cleanName) && !cleanName.startsWith("_");
    }

    private static final Set<String> HIDDEN_VARIABLES = Set.of(
            "args", "this", "super", "scanner", "class"
    );

    // ========================================================================
    // ITYPEBINDING INTERNALS (Keep - TypeInfo delegates to these)
    // ========================================================================

    /**
     * Gets array dimensions from ITypeBinding.
     * USED INTERNALLY BY TypeInfo - not typically called directly.
     *
     * This is the FIXED version that handles multi-dimensional arrays correctly.
     */
    public static int getArrayDimensions(ITypeBinding binding) {
        if (binding == null || !binding.isArray()) {
            return 0;
        }

        // METHOD 1: Try getDimensions() if available (JDT specific)
        try {
            int dims = binding.getDimensions();
            if (dims > 0) return dims;
        } catch (Exception ignored) {
            // Method not available, use fallback
        }

        // METHOD 2: Count dimensions by traversing
        int dimensions = 0;
        ITypeBinding current = binding;
        while (current != null && current.isArray()) {
            dimensions++;
            current = current.getElementType();
        }

        // METHOD 3: Parse qualified name as last resort
        if (dimensions == 0) {
            String qualifiedName = binding.getQualifiedName();
            while (qualifiedName.endsWith("[]")) {
                dimensions++;
                qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 2);
            }
        }

        return dimensions;
    }

    /**
     * Gets the leaf (element) type of an array binding.
     * USED INTERNALLY BY TypeInfo - not typically called directly.
     */
    public static ITypeBinding getLeafTypeBinding(ITypeBinding binding) {
        if (binding == null || !binding.isArray()) {
            return binding;
        }

        ITypeBinding elementType = binding.getElementType();

        // If element type is still array, recurse
        if (elementType != null && elementType.isArray()) {
            return getLeafTypeBinding(elementType);
        }

        return elementType;
    }

    /**
     * Creates array type with specific dimensions from a leaf type.
     * USED INTERNALLY BY TypeInfo - not typically called directly.
     */
    public static ITypeBinding createArrayTypeWithDimensions(ITypeBinding leafType, int dimensions) {
        if (leafType == null || dimensions <= 0) {
            return leafType;
        }

        ITypeBinding result = leafType;
        // Create array incrementally, one dimension at a time
        for (int i = 0; i < dimensions; i++) {
            result = result.createArrayType(1);
        }

        return result;
    }

    /**
     * Checks if two bindings are assignment-compatible.
     * USED INTERNALLY BY TypeInfo - not typically called directly.
     */
    public static boolean isCompatibleBinding(ITypeBinding actualType, ITypeBinding expectedType) {
        if (expectedType == null) return true;
        if (actualType == null) return false;

        // Direct match
        if (actualType.isEqualTo(expectedType)) return true;

        // Check assignability
        if (actualType.isAssignmentCompatible(expectedType)) return true;

        // Check if both are arrays with compatible element types
        if (actualType.isArray() && expectedType.isArray()) {
            return isCompatibleBinding(
                    actualType.getElementType(),
                    expectedType.getElementType()
            );
        }

        return false;
    }

    // ========================================================================
    // DEPRECATED - Use TypeInfo instead
    // ========================================================================

    /**
     * @deprecated Use TypeInfo.from(typeName).isNumeric()
     */
    @Deprecated
    public static boolean isNumeric(String typeName) {
        return TypeInfo.from(typeName).isNumeric();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).isBoolean()
     */
    @Deprecated
    public static boolean isBoolean(String typeName) {
        return TypeInfo.from(typeName).isBoolean();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).isString()
     */
    @Deprecated
    public static boolean isString(String typeName) {
        return TypeInfo.from(typeName).isString();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).isPrimitive()
     */
    @Deprecated
    public static boolean isPrimitive(String typeName) {
        return TypeInfo.from(typeName).isPrimitive();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).isArray()
     */
    @Deprecated
    public static boolean isArray(String typeName) {
        return TypeInfo.from(typeName).isArray();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).getArrayDimensions()
     */
    @Deprecated
    public static int getListNestingLevel(String typeName) {
        return TypeInfo.from(typeName).getArrayDimensions();
    }

    /**
     * @deprecated Use TypeInfo.from(typeName).getLeafType().getTypeName()
     */
    @Deprecated
    public static String getLeafType(String typeName) {
        return TypeInfo.from(typeName).getLeafType().getTypeName();
    }

    /**
     * @deprecated Use actualType.isCompatibleWith(expectedType)
     */
    @Deprecated
    public static boolean isCompatible(String actual, String expected) {
        return TypeInfo.from(actual).isCompatibleWith(TypeInfo.from(expected));
    }

    /**
     * @deprecated Use TypeInfo.from(type).getDisplayName()
     */
    @Deprecated
    public static String getFriendlyTypeName(ITypeBinding typeBinding) {
        return TypeInfo.from(typeBinding).getDisplayName();
    }

    /**
     * @deprecated Primitive/wrapper conversion now handled in TypeInfo
     */
    @Deprecated
    public static String toWrapperType(String typeName) {
        return switch (typeName) {
            case "int" -> "Integer";
            case "double" -> "Double";
            case "boolean" -> "Boolean";
            case "char" -> "Character";
            case "long" -> "Long";
            case "float" -> "Float";
            case "short" -> "Short";
            case "byte" -> "Byte";
            default -> typeName;
        };
    }

    /**
     * @deprecated Primitive/wrapper conversion now handled in TypeInfo
     */
    @Deprecated
    public static String toPrimitiveType(String typeName) {
        return switch (typeName) {
            case "Integer" -> "int";
            case "Double" -> "double";
            case "Boolean" -> "boolean";
            case "Character" -> "char";
            case "Long" -> "long";
            case "Float" -> "float";
            case "Short" -> "short";
            case "Byte" -> "byte";
            default -> typeName;
        };
    }

}