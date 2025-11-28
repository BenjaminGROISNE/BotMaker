package com.botmaker.util;

/**
 * Constants for block ID prefixes
 */
public class BlockIdPrefix {
    public static final String MAIN = "main_";
    public static final String BODY = "body_";
    public static final String VARIABLE = "var_";
    public static final String IF = "if_";
    public static final String WHILE = "while_";
    public static final String FOR = "for_";
    public static final String BREAK = "break_";
    public static final String CONTINUE = "continue_";
    public static final String ASSIGNMENT = "assign_";
    public static final String INCREMENT = "inc_";
    public static final String DECREMENT = "dec_";
    public static final String READ_INPUT = "read_";
    public static final String PRINT = "print_";
    public static final String BINARY = "binary_";
    public static final String STRING = "string_";
    public static final String NUMBER_FLOAT = "float_";
    public static final String NUMBER_DOUBLE = "double_";
    public static final String NUMBER_INT = "int_";
    public static final String BOOLEAN = "boolean_";
    public static final String IDENTIFIER = "id_";
    public static final String SYNTHETIC_STRING = "synthetic_string_";
    public static final String DO_WHILE = "do_while_";
    public static final String SWITCH = "switch_";
    public static final String RETURN = "return_";
    public static final String COMMENT = "comment_";
    public static final String WAIT = "wait_";
    public static final String LIST = "list_";
    // In BlockIdPrefix.java
    public static final String CLASS = "class_";
    public static final String METHOD = "method_";
    public static final String ENUM = "enum_";
    public static final String ENUM_CONSTANT = "enum_const_";

    private BlockIdPrefix() {} // Prevent instantiation

    /**
     * Generate ID with prefix and hash
     */
    public static String generate(String prefix, Object node) {
        return prefix + node.hashCode();
    }
}