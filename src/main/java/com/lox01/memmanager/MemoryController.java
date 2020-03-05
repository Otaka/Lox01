package com.lox01.memmanager;

import com.lox01.module.AbstractModule;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class MemoryController {

    private List<MemMapping> mappings = new ArrayList<>();

    public boolean addMemoryMapping(AbstractModule module, int start, int length, int id) {
        if (checkIfMappingIntersects(start, length)) {
            return false;
        }

        MemMapping memoryMapping = new MemMapping(id, start, length, module);
        mappings.add(memoryMapping);
        return true;
    }

    private boolean checkIfMappingIntersects(int start, int length) {
        int end = start + length;
        for (int i = 0; i < mappings.size(); i++) {
            MemMapping existingMapping = mappings.get(i);
            if (testIntervalsIntersections(start, end, existingMapping.getStartAddress(), existingMapping.getEndAddress())) {
                return true;
            }
        }
        return false;
    }

    public int getMem8(int address) {
        return 0;
    }

    public int getMem16(int address) {
        return 0;
    }

    public int getMem32(int address) {
        return 0;
    }

    public void setMem32(int address, int value) {

    }

    public void setMem16(int address, int value) {

    }

    public void setMem8(int address, int value) {

    }

    private boolean testIntervalsIntersections(int x1, int x2, int y1, int y2) {
        return (x1 >= y1 && x1 <= y2)
                || (x2 >= y1 && x2 <= y2)
                || (y1 >= x1 && y1 <= x2)
                || (y2 >= x1 && y2 <= x2);
    }
}
