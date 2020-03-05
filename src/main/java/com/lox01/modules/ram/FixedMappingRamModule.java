package com.lox01.modules.ram;

import com.lox01.memmanager.MemoryController;
import com.lox01.module.AbstractModule;

/**
 * @author Dmitry
 */
public class FixedMappingRamModule extends AbstractModule {

    private byte[] bank;
    private int mapPosition;

    public FixedMappingRamModule(int kbSize, int mapPosition) {
        bank = new byte[kbSize * 1024];
    }

    @Override
    public void init(MemoryController memoryManager) {
        super.init(memoryManager);
        memoryManager.addMemoryMapping(this, mapPosition, bank.length, 0);
    }

    @Override
    public int getMem8(int id, int address) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMem16(int id, int address) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMem32(int id, int address) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMem8(int id, int address, int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMem16(int id, int address, int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMem32(int id, int address, int value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
