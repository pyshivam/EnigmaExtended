package com.chrisney.enigma.parser;

import java.util.ArrayList;

/**
 * Map a Kotlin file
 */
public class KotlinCode {

    /**
     * Root blocks of codes
     */
    private ArrayList<CodeBlock> rootCodeBlocks;

    /**
     * All String values
     */
    private ArrayList<CodeString> codeStrings;

    /**
     * Original source code
     */
    private String sourceCode;

    /**
     * Constructor
     *
     * @param blocks     Blocks of codes
     * @param strings    String values
     * @param sourceCode Original source code
     */
    public KotlinCode(ArrayList<CodeBlock> blocks, ArrayList<CodeString> strings, String sourceCode) {
        this.rootCodeBlocks = blocks;
        this.codeStrings = strings;
        this.sourceCode = sourceCode;
    }

    /**
     * All String values
     *
     * @return String values
     */
    public ArrayList<CodeString> getStringValues() {
        return this.codeStrings;
    }

    /**
     * Return all code blocks
     *
     * @return Code blocks
     */
    public ArrayList<CodeBlock> getAllBlocks() {
        return this.getAllBlocks(this.rootCodeBlocks);
    }

    /**
     * Return all code blocks
     *
     * @param blocks blocks
     * @return Code blocks
     */
    private ArrayList<CodeBlock> getAllBlocks(ArrayList<CodeBlock> blocks) {
        if (blocks == null) return null;
        ArrayList<CodeBlock> result = new ArrayList<>();
        for (CodeBlock block : blocks) {
            result.add(block);
            if (Utils.arrayNotEmpty(block.subBlocks)) {
                ArrayList<CodeBlock> r = getAllBlocks(block.subBlocks);
                if (Utils.arrayNotEmpty(r)) result.addAll(r);
            }
        }
        return result;
    }

    /**
     * Return code blocks from type
     *
     * @param type Type of blocks
     * @return Code blocks
     */
    public ArrayList<CodeBlock> getBlocksByType(CodeBlock.BlockType type) {
        return getBlocksByType(new CodeBlock.BlockType[]{type}, this.rootCodeBlocks);
    }

    /**
     * Return code blocks from types
     *
     * @param types Type of blocks
     * @return Code blocks
     */
    public ArrayList<CodeBlock> getBlocksByTypes(CodeBlock.BlockType[] types) {
        return getBlocksByType(types, this.rootCodeBlocks);
    }

    /**
     * Return code blocks from type (recursive)
     *
     * @param types  Types of block
     * @param blocks block (recursive)
     * @return Code blocks
     */
    private ArrayList<CodeBlock> getBlocksByType(CodeBlock.BlockType[] types, ArrayList<CodeBlock> blocks) {
        if (blocks == null) return null;
        ArrayList<CodeBlock> result = new ArrayList<>();
        for (CodeBlock block : blocks) {
            if (Utils.arrayContains(types, block.type)) {
                result.add(block);
            }
            if (Utils.arrayNotEmpty(block.subBlocks)) {
                ArrayList<CodeBlock> r = getBlocksByType(types, block.subBlocks);
                if (Utils.arrayNotEmpty(r)) result.addAll(r);
            }
        }
        return result;
    }

    // Other functions and methods similar to the KotlinCode class can be implemented here.

    /**
     * Source code formatted
     *
     * @return Print the source code formatted
     */
    public String toCode() {
        StringBuilder sb = new StringBuilder();
        for (CodeBlock block : getAllBlocks()) {
            if (!block.hasParent) sb.append(block.toCode());
        }
        // End Of File
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.rootCodeBlocks != null) {
            for (CodeBlock block : this.rootCodeBlocks) {
                sb.append(block.code);
            }
        }
        return sb.toString();
    }
}
