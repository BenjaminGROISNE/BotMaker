package com.botmaker.ui;

import com.botmaker.core.BodyBlock;

/**
 * Information about moving an existing block to a new position.
 * @param blockId The ID of the block being moved
 * @param targetBody The BodyBlock where the block should be moved to
 * @param insertionIndex The index where the block should be inserted
 */
public record MoveBlockInfo(String blockId, BodyBlock targetBody, int insertionIndex) {
}