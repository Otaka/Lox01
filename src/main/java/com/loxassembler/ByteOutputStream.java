package com.loxassembler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Dmitry
 */
public class ByteOutputStream {

    private ByteArrayOutputStream stream;
    private int currentVirtualAddress;

    public ByteOutputStream() {
        stream = new ByteArrayOutputStream();
        currentVirtualAddress = 0;
    }

    public void write(byte value) {
        stream.write(value & 0xFF);
        currentVirtualAddress++;
    }

    public void write(int value) {
        stream.write(value);
        currentVirtualAddress++;
    }

    public void write(byte[] value) {
        try {
            stream.write(value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        currentVirtualAddress += value.length;
    }

    public void setCurrentVirtualAddress(int address) {
        currentVirtualAddress = address;
    }

    public int getCurrentVirtualAddress() {
        return currentVirtualAddress;
    }

    public int getCurrentByteOffset() {
        return stream.size();
    }

    public byte[] getBytes() {
        return stream.toByteArray();
    }
}
