package com.lox01.memmanager;

import com.lox01.module.AbstractModule;

/**
 * @author Dmitry
 */
public class MemMapping {

    private int id;
    private int startAddress;
    private int length;
    private int endAddress;
    private AbstractModule module;

    public MemMapping(int id, int startAddress, int length, AbstractModule module) {
        this.id = id;
        this.startAddress = startAddress;
        this.length = length;
        this.module = module;
        this.endAddress = startAddress + length;
    }

    public int getId() {
        return id;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getLength() {
        return length;
    }

    public int getEndAddress() {
        return endAddress;
    }

    public AbstractModule getModule() {
        return module;
    }

}
