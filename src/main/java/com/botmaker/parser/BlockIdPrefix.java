package com.botmaker.parser;

/**
 * Constants for block ID generation
 */
public class BlockIdPrefix {
    public static final String MAIN = "main_";
    public static final String BODY = "body_";
    public static final String VARIABLE = "var_";
    public static final String IF = "if_";
    public static final String PRINT = "print_";
    public static final String BINARY = "binary_";
    public static final String STRING = "string_";
    public static final String NUMBER_FLOAT = "float_";
    public static final String NUMBER_DOUBLE = "double_";
    public static final String NUMBER_INT = "int_";
    public static final String BOOLEAN = "boolean_";
    public static final String IDENTIFIER = "id_";
    public static final String SYNTHETIC_STRING = "synthetic_string_";

    private BlockIdPrefix() {} // Prevent instantiation

    /**
     * Generate ID with prefix and hash
     */
    public static String generate(String prefix, Object node) {
        return prefix + node.hashCode();
    }
}