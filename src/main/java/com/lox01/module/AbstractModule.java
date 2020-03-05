package com.lox01.module;

import com.lox01.memmanager.MemoryController;

/**
 * @author Dmitry
 */
public abstract class AbstractModule {

    private MemoryController memoryManager;

    public void init(MemoryController memoryManager) {
    }

    public abstract int getMem8(int id, int address);

    public abstract int getMem16(int id, int address);

    public abstract int getMem32(int id, int address);

    public abstract void setMem8(int id, int address, int value);

    public abstract void setMem16(int id, int address, int value);

    public abstract void setMem32(int id, int address, int value);

}
