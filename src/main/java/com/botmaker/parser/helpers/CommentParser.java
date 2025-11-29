package com.botmaker.parser.helpers;

import com.botmaker.blocks.CommentBlock;
import com.botmaker.core.CodeBlock;
import com.botmaker.parser.BlockIdPrefix;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles parsing and extraction of comments from the AST.
 */
public class CommentParser {

    /**
     * Parses a comment AST node into a CommentBlock.
     */
    public static CommentBlock parseCommentBlock(Comment astNode, java.util.Map<ASTNode, CodeBlock> nodeToBlockMap,
                                                 String sourceCode) {
        String text = extractCommentText(astNode, sourceCode);
        CommentBlock commentBlock = new CommentBlock(
                BlockIdPrefix.generate(BlockIdPrefix.COMMENT, astNode),
                astNode,
                text
        );
        nodeToBlockMap.put(astNode, commentBlock);
        return commentBlock;
    }

    /**
     * Extracts the text content from a comment node.
     */
    public static String extractCommentText(Comment astNode, String sourceCode) {
        String text = "Comment";
        if (sourceCode != null) {
            try {
                String raw = sourceCode.substring(
                        astNode.getStartPosition(),
                        astNode.getStartPosition() + astNode.getLength()
                );

                if (astNode.isLineComment()) {
                    text = raw.substring(2).trim();
                } else {
                    text = raw.substring(2, raw.length() - 2).trim();
                }
            } catch (Exception ignored) {
                // Keep default "Comment" text
            }
        }
        return text;
    }

    /**
     * Finds comments that are orphaned (not inside any specific statement).
     * These are comments that belong to the body but aren't nested inside statements.
     */
    public static List<Comment> findOrphanedComments(Block block, List<Comment> allComments) {
        List<Comment> orphaned = new ArrayList<>();

        int blockStart = block.getStartPosition() + 1;
        int blockEnd = block.getStartPosition() + block.getLength() - 1;

        for (Comment comment : allComments) {
            int cPos = comment.getStartPosition();

            // Check if comment is within block bounds
            if (cPos > blockStart && cPos < blockEnd) {
                boolean isInsideChild = false;

                // Check if it's inside a statement
                for (Object stmtObj : block.statements()) {
                    Statement s = (Statement) stmtObj;
                    if (cPos >= s.getStartPosition() &&
                            cPos <= s.getStartPosition() + s.getLength()) {
                        isInsideChild = true;
                        break;
                    }
                }

                if (!isInsideChild) {
                    orphaned.add(comment);
                }
            }
        }

        return orphaned;
    }
}