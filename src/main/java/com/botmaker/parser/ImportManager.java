package com.botmaker.parser;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportManager {

    /**
     * Ensures that the specific class is imported in the CompilationUnit.
     * @param cu The compilation unit.
     * @param rewriter The ASTRewrite instance.
     * @param qualifiedClassName The full class name (e.g., "java.util.ArrayList").
     */
    public static void addImport(CompilationUnit cu, ASTRewrite rewriter, String qualifiedClassName) {
        if (cu == null || qualifiedClassName == null) return;

        // Check existing imports
        List<ImportDeclaration> imports = cu.imports();
        Set<String> existingImports = new HashSet<>();

        for (ImportDeclaration imp : imports) {
            if (imp.isOnDemand()) {
                // e.g., java.util.*
                String packageName = imp.getName().getFullyQualifiedName();
                String targetPackage = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.'));
                if (packageName.equals(targetPackage)) {
                    return; // Covered by wildcard
                }
            } else {
                existingImports.add(imp.getName().getFullyQualifiedName());
            }
        }

        if (existingImports.contains(qualifiedClassName)) {
            return; // Already imported
        }

        // Create new import
        AST ast = cu.getAST();
        ImportDeclaration newImport = ast.newImportDeclaration();
        newImport.setName(ast.newName(qualifiedClassName));

        // Insert into AST
        ListRewrite listRewrite = rewriter.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);
        listRewrite.insertLast(newImport, null);
    }
}