package com.loxassembler;

/**
 * @author Dmitry
 */
public class LabelRelocation {

    private String label;
    private int virtualOffset;
    private int byteOffset;
    private int size;
    private int lineIndex;

    public LabelRelocation(String label, int virtualOffset, int byteOffset, int size, int lineIndex) {
        this.label = label;
        this.byteOffset = byteOffset;
        this.virtualOffset = virtualOffset;
        this.size = size;
        this.lineIndex = lineIndex;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public String getLabel() {
        return label;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public int getSize() {
        return size;
    }

    public int getVirtualOffset() {
        return virtualOffset;
    }
}
