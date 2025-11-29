package com.botmaker.parser.helpers;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for type conversions and value preservation across type changes.
 */
public class TypeConversionHelper {

    /**
     * Infers the expected type from the parent context of an expression.
     * Used when replacing expressions to provide type context.
     */
    public static String inferContextType(Expression expr) {
        ASTNode parent = expr.getParent();

        // Variable declaration
        if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment frag = (VariableDeclarationFragment) parent;
            if (frag.getParent() instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement varDecl = (VariableDeclarationStatement) frag.getParent();
                String typeName = varDecl.getType().toString();
                return unwrapArrayListType(typeName);
            }
        }

        // Assignment
        if (parent instanceof Assignment) {
            Assignment assign = (Assignment) parent;
            Expression lhs = assign.getLeftHandSide();
            if (lhs instanceof SimpleName) {
                ITypeBinding binding = lhs.resolveTypeBinding();
                if (binding != null) {
                    return binding.getName();
                }
            }
        }

        return null;
    }

    /**
     * Unwraps ArrayList<T> to get T.
     */
    public static String unwrapArrayListType(String typeName) {
        if (typeName.startsWith("ArrayList<") && typeName.endsWith(">")) {
            return typeName.substring(10, typeName.length() - 1);
        }
        return typeName;
    }

    /**
     * Extracts the leaf type from a potentially nested type.
     * E.g., "ArrayList<ArrayList<Integer>>" -> "Integer"
     */
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

    /**
     * Collects all leaf values from a potentially nested list structure.
     * This preserves user data when converting between list types.
     */
    public static void collectLeafValues(Expression expr, List<Expression> accumulator) {
        if (expr == null) return;

        boolean isContainer = false;

        if (expr instanceof ClassInstanceCreation) {
            ClassInstanceCreation cic = (ClassInstanceCreation) expr;
            if (cic.getType().toString().startsWith("ArrayList") && !cic.arguments().isEmpty()) {
                isContainer = true;
                collectLeafValues((Expression) cic.arguments().get(0), accumulator);
            }
        } else if (expr instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) expr;
            String name = mi.getName().getIdentifier();
            if (name.equals("asList") || name.equals("of")) {
                isContainer = true;
                for (Object arg : mi.arguments()) {
                    collectLeafValues((Expression) arg, accumulator);
                }
            }
        } else if (expr instanceof ArrayInitializer) {
            isContainer = true;
            for (Object e : ((ArrayInitializer) expr).expressions()) {
                collectLeafValues((Expression) e, accumulator);
            }
        } else if (expr instanceof ArrayCreation) {
            isContainer = true;
            if (((ArrayCreation) expr).getInitializer() != null) {
                collectLeafValues(((ArrayCreation) expr).getInitializer(), accumulator);
            }
        }

        if (!isContainer) {
            accumulator.add(expr);
        }
    }

    /**
     * Checks if two types have the same leaf type (ignoring list wrappers).
     */
    public static boolean haveSameLeafType(String type1, String type2) {
        String leaf1 = getLeafType(type1);
        String leaf2 = getLeafType(type2);
        return leaf1.equals(leaf2);
    }
}