package com.botmaker.validation;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorTranslator {

    private static final Map<Integer, ErrorInfo> ERROR_MAPPINGS = new HashMap<>();
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("\\b(\\d{7,})\\b");

    static class ErrorInfo {
        String userMessage;
        String suggestion;
        DiagnosticSeverity severity;

        ErrorInfo(String userMessage, String suggestion, DiagnosticSeverity severity) {
            this.userMessage = userMessage;
            this.suggestion = suggestion;
            this.severity = severity;
        }

        ErrorInfo(String userMessage, String suggestion) {
            this(userMessage, suggestion, DiagnosticSeverity.Error);
        }
    }

    static {
        // PRIORITY 1: Critical Errors (Must Handle)

        // 16777233 - TypeMismatch
        ERROR_MAPPINGS.put(16777233, new ErrorInfo(
                "Wrong type used: You're trying to use a {0} where a {1} is expected",
                "Check that you're using the right type of value (number, text, true/false, etc.)"
        ));

        // 570425394 - UndefinedName (Variable doesn't exist)
        ERROR_MAPPINGS.put(570425394, new ErrorInfo(
                "Variable or name '{0}' doesn't exist",
                "Did you forget to create this variable? Check for typos in the name."
        ));

        // 536870963 - UninitializedLocalVariable
        ERROR_MAPPINGS.put(536870963, new ErrorInfo(
                "Variable '{0}' is used before being given a value",
                "Set a value to this variable before using it."
        ));

        // 536870967 - RedefinedLocal (Duplicate variable)
        ERROR_MAPPINGS.put(536870967, new ErrorInfo(
                "A variable named '{0}' already exists",
                "Choose a different name or remove the duplicate variable."
        ));

        // 67108979 - ParameterMismatch
        ERROR_MAPPINGS.put(67108979, new ErrorInfo(
                "Wrong number of parameters: Expected {0} but got {1}",
                "Check how many inputs this function needs."
        ));

        // 603979884 - ShouldReturnValue
        ERROR_MAPPINGS.put(603979884, new ErrorInfo(
                "This function must return a value",
                "Add a return statement with a value at the end of the function."
        ));

        // PRIORITY 2: Type System Errors

        // 16777218 - UndefinedType
        ERROR_MAPPINGS.put(16777218, new ErrorInfo(
                "Type '{0}' cannot be found",
                "This type doesn't exist. Check for typos or missing imports."
        ));

        // 67108964 - UndefinedMethod
        ERROR_MAPPINGS.put(67108964, new ErrorInfo(
                "Method '{0}' doesn't exist",
                "Check the spelling of the method name or if it's available."
        ));

        // 33554502 - UndefinedField
        ERROR_MAPPINGS.put(33554502, new ErrorInfo(
                "Field '{0}' doesn't exist",
                "This field is not defined. Check the name and spelling."
        ));

        // PRIORITY 3: Syntax Errors (If user can edit code)

        // 1610612960 - MissingSemiColon
        ERROR_MAPPINGS.put(1610612960, new ErrorInfo(
                "Missing semicolon (;) at the end of the line",
                "Add a semicolon (;) at the end of this statement."
        ));

        // 1610612995 - UnterminatedString
        ERROR_MAPPINGS.put(1610612995, new ErrorInfo(
                "Text is missing a closing quote",
                "Add a closing quote (\") at the end of the text."
        ));

        // 1610612941 - ParsingErrorNoSuggestion
        ERROR_MAPPINGS.put(1610612941, new ErrorInfo(
                "Syntax error: The code structure is incorrect",
                "Check for missing brackets, parentheses, or other syntax issues."
        ));

        // 1610612956 - UnmatchedBracket
        ERROR_MAPPINGS.put(1610612956, new ErrorInfo(
                "Unmatched bracket - missing opening or closing bracket",
                "Check that all { } brackets are properly paired."
        ));

        // PRIORITY 4: Flow Control

        // 536870908 - InvalidBreak
        ERROR_MAPPINGS.put(536870908, new ErrorInfo(
                "'break' can only be used inside a loop or switch",
                "Move this break statement inside a loop block."
        ));

        // 536870909 - InvalidContinue
        ERROR_MAPPINGS.put(536870909, new ErrorInfo(
                "'continue' can only be used inside a loop",
                "Move this continue statement inside a loop block."
        ));

        // 536870161 - CodeCannotBeReached
        ERROR_MAPPINGS.put(536870161, new ErrorInfo(
                "This code will never run (unreachable code)",
                "Remove this code or fix the logic that prevents it from running.",
                DiagnosticSeverity.Warning
        ));

        // PRIORITY 5: Warnings

        // 536870973 - LocalVariableIsNeverUsed
        ERROR_MAPPINGS.put(536870973, new ErrorInfo(
                "Variable '{0}' is created but never used",
                "Remove this variable or use it somewhere in your code.",
                DiagnosticSeverity.Warning
        ));

        // 536870974 - ArgumentIsNeverUsed
        ERROR_MAPPINGS.put(536870974, new ErrorInfo(
                "Parameter '{0}' is never used",
                "Remove this parameter or use it in the function.",
                DiagnosticSeverity.Warning
        ));

        // 536871185 - AssignmentHasNoEffect
        ERROR_MAPPINGS.put(536871185, new ErrorInfo(
                "This assignment does nothing",
                "You're assigning a variable to itself. Remove this line or fix the logic.",
                DiagnosticSeverity.Warning
        ));
    }

    /**
     * Extracts the JDT error code from a diagnostic message
     */
    private static Integer extractErrorCode(String message) {
        Matcher matcher = ERROR_CODE_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Translates diagnostics to user-friendly messages
     */
    public static String translate(List<Diagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "✅ No errors found. Your code looks good!";
        }

        StringBuilder result = new StringBuilder();
        int errorCount = 0;
        int warningCount = 0;

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getSeverity() == DiagnosticSeverity.Error) {
                errorCount++;
            } else if (diagnostic.getSeverity() == DiagnosticSeverity.Warning) {
                warningCount++;
            }
        }

        if (errorCount > 0) {
            result.append(String.format("❌ Found %d error%s:\n\n", errorCount, errorCount == 1 ? "" : "s"));
        }
        if (warningCount > 0) {
            result.append(String.format("⚠️  Found %d warning%s:\n\n", warningCount, warningCount == 1 ? "" : "s"));
        }

        for (Diagnostic diagnostic : diagnostics) {
            String icon = diagnostic.getSeverity() == DiagnosticSeverity.Error ? "❌" : "⚠️";
            int lineNumber = diagnostic.getRange().getStart().getLine() + 1;

            String translated = translateSingleDiagnostic(diagnostic);
            result.append(String.format("%s Line %d: %s\n\n", icon, lineNumber, translated));
        }

        return result.toString().trim();
    }

    /**
     * Translates a single diagnostic to a user-friendly message
     */
    public static String translateSingleDiagnostic(Diagnostic diagnostic) {
        String originalMessage = diagnostic.getMessage();

        // Try to extract error code
        Integer errorCode = extractErrorCode(originalMessage);

        if (errorCode != null && ERROR_MAPPINGS.containsKey(errorCode)) {
            ErrorInfo info = ERROR_MAPPINGS.get(errorCode);

            // Try to extract variable/type names from the original message
            String enrichedMessage = enrichMessage(info.userMessage, originalMessage);

            return String.format("%s\n   💡 %s", enrichedMessage, info.suggestion);
        }

        // Fallback: Try pattern matching for common error messages (backward compatibility)
        return translateByPattern(originalMessage);
    }

    /**
     * Enriches the user message with context from the original error message
     */
    private static String enrichMessage(String template, String originalMessage) {
        // Extract quoted strings (variable names, type names, etc.)
        Pattern quotedPattern = Pattern.compile("'([^']+)'|\"([^\"]+)\"");
        Matcher matcher = quotedPattern.matcher(originalMessage);

        int index = 0;
        String result = template;
        while (matcher.find() && result.contains("{" + index + "}")) {
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            result = result.replace("{" + index + "}", "'" + value + "'");
            index++;
        }

        // Remove unreplaced placeholders
        result = result.replaceAll("\\{\\d+\\}", "the value");

        return result;
    }

    /**
     * Fallback pattern-based translation (for backward compatibility)
     */
    private static String translateByPattern(String message) {
        if (message.contains("cannot be resolved to a type")) {
            return "A type or class could not be found.\n   💡 Check for typos or if the type exists.";
        }
        if (message.contains("cannot be resolved")) {
            return "A variable, method, or name could not be found.\n   💡 Check for typos or if it was declared.";
        }
        if (message.contains("Syntax error, insert")) {
            try {
                String suggestion = message.split("insert \"")[1].split("\" to")[0];
                return String.format("Syntax error: Something is missing.\n   💡 Try adding '%s'", suggestion);
            } catch (Exception e) {
                return "Syntax error: Something is missing in the code structure.\n   💡 Check for missing semicolons, brackets, or parentheses.";
            }
        }
        if (message.contains("incompatible types") || message.contains("Type mismatch")) {
            return "Wrong type used: You're using a value of the wrong type.\n   💡 Make sure you're using the right kind of value (number, text, etc.)";
        }
        if (message.contains("might not have been initialized")) {
            return "Variable used before being set.\n   💡 Give this variable a value before using it.";
        }
        if (message.contains("is not a statement")) {
            return "This line is not a valid statement.\n   💡 It might be an incomplete expression or command.";
        }
        if (message.contains("Duplicate local variable")) {
            return "A variable with this name already exists.\n   💡 Choose a different name for this variable.";
        }

        // Return original message if no translation found
        return message;
    }

    /**
     * Get a short summary for UI display (e.g., tooltip)
     */
    public static String getShortSummary(Diagnostic diagnostic) {
        String originalMessage = diagnostic.getMessage();
        Integer errorCode = extractErrorCode(originalMessage);

        if (errorCode != null && ERROR_MAPPINGS.containsKey(errorCode)) {
            ErrorInfo info = ERROR_MAPPINGS.get(errorCode);
            return enrichMessage(info.userMessage, originalMessage);
        }

        // Fallback: Return first line of original message
        return originalMessage.split("\n")[0];
    }

    /**
     * Get just the suggestion part
     */
    public static String getSuggestion(Diagnostic diagnostic) {
        String originalMessage = diagnostic.getMessage();
        Integer errorCode = extractErrorCode(originalMessage);

        if (errorCode != null && ERROR_MAPPINGS.containsKey(errorCode)) {
            return ERROR_MAPPINGS.get(errorCode).suggestion;
        }

        return "Check your code for issues.";
    }
}
