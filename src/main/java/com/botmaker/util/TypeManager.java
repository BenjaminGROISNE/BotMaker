package com.botmaker.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class TypeManager {

    private static final List<String> FUNDAMENTAL_TYPES = List.of(
            "int", "double", "boolean", "String", "long", "float", "char"
    );

    public static List<String> getFundamentalTypeNames() {
        return FUNDAMENTAL_TYPES;
    }

    /**
     * Creates a JDT AST Type node from a string representation.
     * Handles arrays (e.g., "int[]", "String[][]").
     */
    public static Type createTypeNode(AST ast, String typeName) {
        // count array dimensions
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