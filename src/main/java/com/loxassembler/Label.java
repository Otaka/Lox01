package com.loxassembler;

/**
 * @author Dmitry
 */
class Label {

    private String label;
    private int offset;
    private long virtualOffset;
    public Label() {
    }

    public Label(String label, int offset) {
        this.label = label;
        this.offset = offset;
    }

    public String getLabel() {
        return label;
    }

    public int getOffset() {
        return offset;
    }

}
