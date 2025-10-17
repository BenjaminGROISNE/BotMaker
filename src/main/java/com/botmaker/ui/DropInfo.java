package com.botmaker.ui;

import com.botmaker.core.BodyBlock;

public record DropInfo(AddableBlock type, BodyBlock targetBody, int insertionIndex) {
}
