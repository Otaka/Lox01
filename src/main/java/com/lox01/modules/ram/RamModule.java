package com.lox01.modules.ram;

import com.lox01.module.AbstractModule;

/**
 * @author Dmitry
 */
public class RamModule extends AbstractModule {

    public byte[] bank;

    public RamModule(int kbSize) {
        bank = new byte[kbSize * 1024];
    }

    @Override
    public int getMem8(int id, int address) {
        return bank[address + 0];
    }

    @Override
    public int getMem16(int id, int address) {
        return bank[address + 0] | bank[address + 1] << 8;
    }

    @Override
    public int getMem32(int id, int address) {
        return bank[address + 0]
                | bank[address + 1] << 8
                | bank[address + 2] << 16
                | bank[address + 3] << 24;
    }

    @Override
    public void setMem8(int id, int address, int value) {
        bank[address + 0] = (byte) value;
    }

    @Override
    public void setMem16(int id, int address, int value) {
        bank[address + 1] = (byte) ((value >> 8) & 0xFF);
        bank[address + 0] = (byte) ((value) & 0xFF);

    }

    @Override
    public void setMem32(int id, int address, int value) {
        bank[address + 3] = (byte) ((value >> 24) & 0xFF);
        bank[address + 2] = (byte) ((value >> 16) & 0xFF);
        bank[address + 1] = (byte) ((value >> 8) & 0xFF);
        bank[address + 0] = (byte) ((value) & 0xFF);
    }
}
