package com.botmaker.validation;

import org.eclipse.lsp4j.Diagnostic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ErrorTranslator {

    private static final Map<String, String> simpleErrorMappings = new LinkedHashMap<>();

    static {
        simpleErrorMappings.put("cannot be resolved to a type", "A class or type could not be found. Check for typos or missing imports.");
        simpleErrorMappings.put("cannot be resolved", "A variable or method could not be found. Check for typos or if it was declared.");
        simpleErrorMappings.put("Syntax error, insert", "There is a syntax error. The compiler suggests adding %s to fix it."); // Special handling
        simpleErrorMappings.put("incompatible types", "A value of one type is being used where a different type is expected.");
        simpleErrorMappings.put("might not have been initialized", "A variable is being used before it has been given a value.");
        simpleErrorMappings.put("is not a statement", "This line of code is not a valid action. It might be an incomplete expression.");
        simpleErrorMappings.put("Duplicate local variable", "A variable with this name has already been declared in this scope.");
    }

    public static String translate(List<Diagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return "No errors found.";
        }

        StringBuilder result = new StringBuilder("Found errors in your code:\n");

        for (Diagnostic diagnostic : diagnostics) {
            String message = diagnostic.getMessage();
            boolean translated = false;

            for (Map.Entry<String, String> entry : simpleErrorMappings.entrySet()) {
                if (message.contains(entry.getKey())) {
                    String friendlyMessage = entry.getValue();
                    if (friendlyMessage.contains("%s")) {
                        // Simple extraction for "Syntax error, insert \"...\" to complete..."
                        try {
                            String suggestion = message.split("insert \"")[1].split("\" to")[0];
                            friendlyMessage = String.format(friendlyMessage, suggestion);
                        } catch (Exception e) {
                            // fallback
                            friendlyMessage = friendlyMessage.replace("%s", "the required symbols");
                        }
                    }
                    result.append(String.format("- Line %d: %s\n", diagnostic.getRange().getStart().getLine() + 1, friendlyMessage));
                    translated = true;
                    break;
                }
            }

            if (!translated) {
                result.append(String.format("- Line %d: %s\n", diagnostic.getRange().getStart().getLine() + 1, message));
            }
        }
        return result.toString();
    }
}
