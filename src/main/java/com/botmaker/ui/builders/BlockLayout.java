package com.botmaker.ui.builders;

/**
 * Main entry point for building block UIs.
 * Provides fluent API for common block layouts.
 */
public class BlockLayout {

    // Factory methods for different layout types
    public static HeaderLayoutBuilder header() {
        return new HeaderLayoutBuilder();
    }

    public static BodyLayoutBuilder body() {
        return new BodyLayoutBuilder();
    }

    public static SentenceLayoutBuilder sentence() {
        return new SentenceLayoutBuilder();
    }

    public static LoopLayoutBuilder loop() {
        return new LoopLayoutBuilder();
    }

    public static ConditionalLayoutBuilder conditional() {
        return new ConditionalLayoutBuilder();
    }

    public static ExpressionLayoutBuilder expression() {
        return new ExpressionLayoutBuilder();
    }

    public static DeclarationLayoutBuilder declaration() {
        return new DeclarationLayoutBuilder();
    }

    public static OperatorLayoutBuilder operator() {
        return new OperatorLayoutBuilder();
    }
}