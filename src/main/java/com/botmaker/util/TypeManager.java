package com.botmaker.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class TypeManager {

    private static final List<String> FUNDAMENTAL_TYPES = List.of(
        "int", "double", "boolean", "String", "long", "float", "char", "short", "byte"
    );

    /**
     * Returns a list of fundamental Java type names (primitives + String).
     */
    public static List<String> getFundamentalTypeNames() {
        return FUNDAMENTAL_TYPES;
    }

    /**
     * Creates a JDT AST Type node from a string representation of the type name.
     * Handles both primitive types and class/interface names.
     *
     * @param ast The AST instance to use for creating the node.
     * @param typeName The name of the type (e.g., "int", "String", "java.util.List").
     * @return The constructed Type node.
     */
    public static Type createTypeNode(AST ast, String typeName) {
        switch (typeName) {
            case "int": return ast.newPrimitiveType(PrimitiveType.INT);
            case "double": return ast.newPrimitiveType(PrimitiveType.DOUBLE);
            case "boolean": return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
            case "char": return ast.newPrimitiveType(PrimitiveType.CHAR);
            case "long": return ast.newPrimitiveType(PrimitiveType.LONG);
            case "float": return ast.newPrimitiveType(PrimitiveType.FLOAT);
            case "short": return ast.newPrimitiveType(PrimitiveType.SHORT);
            case "byte": return ast.newPrimitiveType(PrimitiveType.BYTE);
            default:
                // For non-primitives, ast.newName() can handle simple and qualified names.
                return ast.newSimpleType(ast.newName(typeName));
        }
    }
}
