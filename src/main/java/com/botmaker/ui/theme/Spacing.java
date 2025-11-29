package com.botmaker.ui.theme;

import javafx.geometry.Insets;

public class Spacing {
    private double tiny = 2.0;
    private double small = 5.0;
    private double normal = 10.0;
    private double medium = 15.0;
    private double large = 20.0;

    // Standard indentation
    private Insets standardIndent = new Insets(5, 0, 0, 20);

    public double tiny() { return tiny; }
    public double small() { return small; }
    public double normal() { return normal; }
    public double medium() { return medium; }
    public double large() { return large; }

    public Insets standardIndent() { return standardIndent; }
    public Insets custom(double top, double right, double bottom, double left) {
        return new Insets(top, right, bottom, left);
    }
}