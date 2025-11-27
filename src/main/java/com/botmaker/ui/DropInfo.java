// In DropInfo.java - update the record
package com.botmaker.ui;

import com.botmaker.blocks.ClassBlock;
import com.botmaker.core.BodyBlock;

public record DropInfo(
        AddableBlock type,
        BodyBlock targetBody,
        int insertionIndex,
        ClassBlock targetClass // NEW: for method declarations
) {
    // Convenience constructor for statement drops (existing behavior)
    public DropInfo(AddableBlock type, BodyBlock targetBody, int insertionIndex) {
        this(type, targetBody, insertionIndex, null);
    }
}