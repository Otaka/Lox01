package com.loxassembler.parser;

/**
 * @author Dmitry
 */
public class StringWithPosition {

    private String string;
    private int start;
    private int end;

    public StringWithPosition(String string, int start, int end) {
        this.string = string;
        this.start = start;
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }
}
