package com.botmaker.util;

import com.botmaker.core.BodyBlock;
import com.botmaker.core.BlockWithChildren;
import com.botmaker.core.CodeBlock;
import com.botmaker.core.StatementBlock;

import java.util.Map;

/**
 * Helper class for looking up blocks in the block tree.
 */
public class BlockLookupHelper {

    /**
     * Finds a block by its ID in the node-to-block map.
     * @param blockId The ID of the block to find
     * @param nodeToBlockMap The map of AST nodes to CodeBlocks
     * @return The StatementBlock if found, null otherwise
     */
    public static StatementBlock findBlockById(String blockId, Map<?, CodeBlock> nodeToBlockMap) {
        if (blockId == null || nodeToBlockMap == null) {
            return null;
        }

        // Search through all blocks in the map
        for (CodeBlock block : nodeToBlockMap.values()) {
            if (block.getId().equals(blockId) && block instanceof StatementBlock) {
                return (StatementBlock) block;
            }
        }

        return null;
    }

    /**
     * Finds the BodyBlock that contains the given statement block.
     * @param targetBlock The statement block to find the parent for
     * @param nodeToBlockMap The map of AST nodes to CodeBlocks
     * @return The BodyBlock containing the statement, null if not found
     */
    public static BodyBlock findParentBody(StatementBlock targetBlock, Map<?, CodeBlock> nodeToBlockMap) {
        if (targetBlock == null || nodeToBlockMap == null) {
            return null;
        }

        // Search through all blocks to find which BodyBlock contains the target
        for (CodeBlock block : nodeToBlockMap.values()) {
            if (block instanceof BodyBlock) {
                BodyBlock bodyBlock = (BodyBlock) block;
                if (bodyBlock.getStatements().contains(targetBlock)) {
                    return bodyBlock;
                }
            }

            // Also check nested structures
            if (block instanceof BlockWithChildren) {
                BodyBlock found = findParentBodyInChildren(targetBlock, (BlockWithChildren) block);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Recursively searches for the parent body in a block's children.
     */
    private static BodyBlock findParentBodyInChildren(StatementBlock targetBlock, BlockWithChildren parent) {
        for (CodeBlock child : parent.getChildren()) {
            if (child instanceof BodyBlock) {
                BodyBlock bodyBlock = (BodyBlock) child;
                if (bodyBlock.getStatements().contains(targetBlock)) {
                    return bodyBlock;
                }
            }

            if (child instanceof BlockWithChildren) {
                BodyBlock found = findParentBodyInChildren(targetBlock, (BlockWithChildren) child);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}