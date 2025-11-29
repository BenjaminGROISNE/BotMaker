package com.botmaker.parser.helpers;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for working with enum declarations in the AST.
 * Used by both AstRewriter and NodeCreator to avoid duplication.
 */
public class EnumNodeHelper {

    /**
     * Finds the first constant name in an enum declaration.
     * @param cu The compilation unit to search
     * @param enumName The name of the enum to find
     * @return The name of the first constant, or null if not found
     */
    public static String findFirstEnumConstant(CompilationUnit cu, String enumName) {
        if (cu == null || enumName == null) return null;

        EnumDeclaration enumDecl = findEnumDeclaration(cu, enumName);
        if (enumDecl != null && !enumDecl.enumConstants().isEmpty()) {
            EnumConstantDeclaration first = (EnumConstantDeclaration) enumDecl.enumConstants().get(0);
            return first.getName().getIdentifier();
        }
        return null;
    }

    /**
     * Finds an enum declaration by name.
     * Searches both top-level types and class body declarations.
     * @param cu The compilation unit to search
     * @param enumName The name of the enum
     * @return The EnumDeclaration if found, null otherwise
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

    /**
     * Searches for an enum within a TypeDeclaration's body.
     */
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
     * Gets all constant names from an enum declaration.
     * @param enumDecl The enum declaration
     * @return List of constant names
     */
    public static List<String> getAllEnumConstantNames(EnumDeclaration enumDecl) {
        List<String> names = new ArrayList<>();
        if (enumDecl != null) {
            for (Object obj : enumDecl.enumConstants()) {
                EnumConstantDeclaration constant = (EnumConstantDeclaration) obj;
                names.add(constant.getName().getIdentifier());
            }
        }
        return names;
    }
}