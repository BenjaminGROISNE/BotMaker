package com.astblocks.demo;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.ArrayList;
import java.util.List;

public class CodeBlock {
    private final String id;
    private final String type;
    private String code;
    private ASTNode astNode;
    private final List<CodeBlock> children = new ArrayList<>();

    public CodeBlock(String id, String type, String code) {
        this.id = id;
        this.type = type;
        this.code = code;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public ASTNode getAstNode() { return astNode; }
    public void setAstNode(ASTNode astNode) { this.astNode = astNode; }
    public List<CodeBlock> getChildren() { return children; }
    public void addChild(CodeBlock child) { children.add(child); }

    @Override
    public String toString() {
        return String.format("CodeBlock[id=%s, type=%s, code=%s]",
                id, type, code.substring(0, Math.min(code.length(), 30)) + (code.length() > 30 ? "..." : ""));
    }
}
