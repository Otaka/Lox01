package com.loxassembler;

/**
 * @author Dmitry
 */
public class ComplexAddressing {

    private int offset;
    private int sourceDestRegIndex;
    private int baseRegister;
    private int indexRegister;

    public ComplexAddressing(int offset, int sourceDestRegIndex, int baseRegister, int indexRegister) {
        this.offset = offset;
        this.sourceDestRegIndex = sourceDestRegIndex;
        this.baseRegister = baseRegister;
        this.indexRegister = indexRegister;
    }

    public int getIndexRegister() {
        return indexRegister;
    }

    public int getBaseRegister() {
        return baseRegister;
    }

    public int getOffset() {
        return offset;
    }

    public int getSourceDestRegIndex() {
        return sourceDestRegIndex;
    }
}
