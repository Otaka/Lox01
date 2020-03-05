package com.loxassembler;

/**
 * @author Dmitry
 */
public class CompilationException extends RuntimeException {

    private int position;
    private int line;

    public CompilationException(int line, int position, String message) {
        super(message);
        this.position = position;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

}
