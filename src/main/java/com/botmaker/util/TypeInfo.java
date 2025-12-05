package com.botmaker.util;

import org.eclipse.jdt.core.dom.*;
import java.util.Objects;

/**
 * Unified type representation for BotMaker.
 * Wraps ITypeBinding (authoritative) with String fallback for cases where bindings aren't resolved yet.
 *
 * Design principles:
 * - ITypeBinding is the source of truth when available
 * - String types are parsed/cached for backward compatibility
 * - Lazy computation of expensive properties (dimensions, leaf types)
 * - Immutable value object with clear equality semantics
 */
public class TypeInfo {

    // Core data
    private final ITypeBinding binding;        // Authoritative (can be null)
    private final String stringType;           // Fallback/cache (never null)

    // Cached computed values (lazy)
    private Integer cachedDimensions;
    private TypeInfo cachedLeafType;
    private Boolean cachedIsArray;
    private Boolean cachedIsEnum;
    private Boolean cachedIsPrimitive;

    // ========================================================================
    // FACTORY METHODS (Primary Creation Points)
    // ========================================================================

    /**
     * Creates TypeInfo from ITypeBinding (preferred method)
     */
    public static TypeInfo from(ITypeBinding binding) {
        if (binding == null) {
            return TypeInfo.UNKNOWN;
        }
        String stringRep = binding.getQualifiedName();
        return new TypeInfo(binding, stringRep);
    }

    /**
     * Creates TypeInfo from string type (fallback for unresolved types)
     */
    public static TypeInfo from(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return TypeInfo.UNKNOWN;
        }
        return new TypeInfo(null, typeName.trim());
    }

    /**
     * Creates TypeInfo from AST Type node
     */
    public static TypeInfo from(Type type) {
        if (type == null) return TypeInfo.UNKNOWN;
        ITypeBinding binding = type.resolveBinding();
        if (binding != null) {
            return from(binding);
        }
        // Fallback to string representation
        return from(type.toString());
    }

    /**
     * Creates TypeInfo from Expression
     */
    public static TypeInfo from(Expression expr) {
        if (expr == null) return TypeInfo.UNKNOWN;
        ITypeBinding binding = expr.resolveTypeBinding();
        if (binding != null) {
            return from(binding);
        }
        // Fallback to string representation
        return TypeInfo.UNKNOWN;
    }

    // Common type constants
    public static final TypeInfo UNKNOWN = new TypeInfo(null, "Object");
    public static final TypeInfo INT = new TypeInfo(null, "int");
    public static final TypeInfo DOUBLE = new TypeInfo(null, "double");
    public static final TypeInfo BOOLEAN = new TypeInfo(null, "boolean");
    public static final TypeInfo STRING = new TypeInfo(null, "String");
    public static final TypeInfo VOID = new TypeInfo(null, "void");

    // ========================================================================
    // CONSTRUCTOR (Private - use factory methods)
    // ========================================================================

    private TypeInfo(ITypeBinding binding, String stringType) {
        this.binding = binding;
        this.stringType = stringType;
    }

    // ========================================================================
    // CORE PROPERTIES
    // ========================================================================

    /**
     * Gets the ITypeBinding if available (may be null for unresolved types)
     */
    public ITypeBinding getBinding() {
        return binding;
    }

    /**
     * Gets the string representation of this type
     */
    public String getTypeName() {
        return stringType;
    }

    /**
     * Returns true if this type has resolved binding information
     */
    public boolean hasBinding() {
        return binding != null;
    }

    /**
     * Returns true if this type is fully unknown/unresolved
     */
    public boolean isUnknown() {
        return this == UNKNOWN || (binding == null && stringType.equals("Object"));
    }

    // ========================================================================
    // TYPE CLASSIFICATION
    // ========================================================================

    public boolean isArray() {
        if (cachedIsArray != null) return cachedIsArray;

        if (binding != null) {
            cachedIsArray = binding.isArray();
        } else {
            cachedIsArray = stringType.endsWith("[]");
        }
        return cachedIsArray;
    }

    public boolean isEnum() {
        if (cachedIsEnum != null) return cachedIsEnum;

        if (binding != null) {
            cachedIsEnum = binding.isEnum();
        } else {
            // Heuristic: uppercase first letter, not in primitives
            cachedIsEnum = Character.isUpperCase(stringType.charAt(0)) &&
                    !TypeCategories.isPrimitive(stringType);
        }
        return cachedIsEnum;
    }

    public boolean isPrimitive() {
        if (cachedIsPrimitive != null) return cachedIsPrimitive;

        if (binding != null) {
            cachedIsPrimitive = binding.isPrimitive();
        } else {
            cachedIsPrimitive = TypeCategories.isPrimitive(stringType);
        }
        return cachedIsPrimitive;
    }

    public boolean isNumeric() {
        return TypeCategories.isNumeric(this);
    }

    public boolean isBoolean() {
        return TypeCategories.isBoolean(this);
    }

    public boolean isString() {
        return TypeCategories.isString(this);
    }

    // ========================================================================
    // ARRAY OPERATIONS
    // ========================================================================

    /**
     * Gets the number of array dimensions (0 if not an array)
     */
    public int getArrayDimensions() {
        if (cachedDimensions != null) return cachedDimensions;

        if (binding != null && binding.isArray()) {
            // Use the fixed method from TypeManager
            cachedDimensions = TypeManager.getArrayDimensions(binding);
        } else if (stringType.endsWith("[]")) {
            // Count "[]" occurrences
            int count = 0;
            String temp = stringType;
            while (temp.endsWith("[]")) {
                count++;
                temp = temp.substring(0, temp.length() - 2);
            }
            cachedDimensions = count;
        } else {
            cachedDimensions = 0;
        }
        return cachedDimensions;
    }

    /**
     * Gets the leaf (element) type of an array, or this type if not an array
     */
    public TypeInfo getLeafType() {
        if (cachedLeafType != null) return cachedLeafType;

        if (!isArray()) {
            cachedLeafType = this;
            return this;
        }

        if (binding != null) {
            ITypeBinding leafBinding = TypeManager.getLeafTypeBinding(binding);
            cachedLeafType = leafBinding != null ? TypeInfo.from(leafBinding) : this;
        } else {
            // Parse string
            String temp = stringType;
            while (temp.endsWith("[]")) {
                temp = temp.substring(0, temp.length() - 2).trim();
            }
            cachedLeafType = TypeInfo.from(temp);
        }
        return cachedLeafType;
    }

    /**
     * Creates an array type from this type with specified dimensions
     */
    public TypeInfo asArray(int dimensions) {
        if (dimensions <= 0) return this;

        if (binding != null) {
            ITypeBinding leafBinding = isArray() ?
                    TypeManager.getLeafTypeBinding(binding) : binding;
            ITypeBinding arrayBinding = TypeManager.createArrayTypeWithDimensions(
                    leafBinding, dimensions);
            return TypeInfo.from(arrayBinding);
        } else {
            String leafType = getLeafType().getTypeName();
            String arrayType = leafType + "[]".repeat(dimensions);
            return TypeInfo.from(arrayType);
        }
    }

    // ========================================================================
    // TYPE COMPATIBILITY
    // ========================================================================

    /**
     * Checks if this type is compatible with (assignable to) another type
     */
    public boolean isCompatibleWith(TypeInfo other) {
        if (other == null || other.isUnknown() || this.isUnknown()) {
            return true; // Unknown types are compatible with everything
        }

        // Use binding-based comparison if both have bindings
        if (this.hasBinding() && other.hasBinding()) {
            return TypeManager.isCompatibleBinding(this.binding, other.binding);
        }

        // Fall back to string-based comparison
        return TypeCompatibility.areCompatible(this, other);
    }

    /**
     * Checks if this type matches another exactly
     */
    public boolean matches(TypeInfo other) {
        if (other == null) return false;
        if (this == other) return true;

        // Binding-based exact match
        if (this.hasBinding() && other.hasBinding()) {
            return this.binding.isEqualTo(other.binding);
        }

        // String-based match with normalization
        String thisNormalized = normalizeTypeName(this.stringType);
        String otherNormalized = normalizeTypeName(other.stringType);
        return thisNormalized.equals(otherNormalized);
    }

    private static String normalizeTypeName(String type) {
        // Remove java.lang prefix
        if (type.startsWith("java.lang.")) {
            type = type.substring(10);
        }
        // Convert wrappers to primitives for comparison
        return switch (type) {
            case "Integer" -> "int";
            case "Double" -> "double";
            case "Boolean" -> "boolean";
            case "Character" -> "char";
            case "Long" -> "long";
            case "Float" -> "float";
            case "Short" -> "short";
            case "Byte" -> "byte";
            default -> type;
        };
    }

    // ========================================================================
    // DISPLAY / DEBUGGING
    // ========================================================================

    /**
     * Gets a simple display name for UI (e.g., "int[]", "String", "MyEnum")
     */
    public String getDisplayName() {
        if (isArray()) {
            return getLeafType().getDisplayName() + "[]".repeat(getArrayDimensions());
        }

        // Remove package names for display
        String name = stringType;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(lastDot + 1);
        }
        return name;
    }

    /**
     * Gets a qualified name (e.g., "java.lang.String")
     */
    public String getQualifiedName() {
        return stringType;
    }

    @Override
    public String toString() {
        return "TypeInfo{" + stringType + (binding != null ? " [bound]" : " [unbound]") + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TypeInfo)) return false;
        TypeInfo other = (TypeInfo) obj;

        // Use binding equality if both have bindings
        if (this.hasBinding() && other.hasBinding()) {
            return this.binding.isEqualTo(other.binding);
        }

        // Fall back to string comparison
        return normalizeTypeName(this.stringType)
                .equals(normalizeTypeName(other.stringType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizeTypeName(stringType));
    }

    // ========================================================================
    // HELPER CLASSES (Inner classes for organization)
    // ========================================================================

    /**
     * Type category checking (replaces UI type constants)
     */
    private static class TypeCategories {

        static boolean isPrimitive(String type) {
            return switch (type) {
                case "int", "double", "boolean", "char", "long", "float", "short", "byte" -> true;
                default -> false;
            };
        }

        static boolean isNumeric(TypeInfo type) {
            String name = type.getLeafType().getTypeName();
            return switch (name) {
                case "int", "double", "float", "long", "short", "byte",
                     "Integer", "Double", "Float", "Long", "Short", "Byte" -> true;
                default -> false;
            };
        }

        static boolean isBoolean(TypeInfo type) {
            String name = type.getLeafType().getTypeName();
            return name.equals("boolean") || name.equals("Boolean");
        }

        static boolean isString(TypeInfo type) {
            String name = type.getLeafType().getTypeName();
            return name.equals("String") || name.equals("char") || name.equals("Character");
        }
    }

    /**
     * Type compatibility logic (replaces TypeManager.isCompatible)
     */
    private static class TypeCompatibility {

        static boolean areCompatible(TypeInfo actual, TypeInfo expected) {
            // Arrays must match dimensions
            if (actual.isArray() != expected.isArray()) {
                return false;
            }

            if (actual.isArray()) {
                if (actual.getArrayDimensions() != expected.getArrayDimensions()) {
                    return false;
                }
                // Check leaf types
                return areCompatible(actual.getLeafType(), expected.getLeafType());
            }

            // Check category compatibility
            if (actual.isNumeric() && expected.isNumeric()) return true;
            if (actual.isBoolean() && expected.isBoolean()) return true;
            if (actual.isString() && expected.isString()) return true;

            // Exact match
            return actual.matches(expected);
        }
    }
}