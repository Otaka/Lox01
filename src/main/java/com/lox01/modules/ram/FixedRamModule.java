package com.lox01.modules.ram;

import com.lox01.memmanager.MemoryController;

/**
 * @author Dmitry
 */
public class FixedRamModule extends RamModule {

    private int position;

    public FixedRamModule(int kbSize, int position) {
        super(kbSize);
        this.position = position;
    }

    @Override
    public void init(MemoryController memoryManager) {
        super.init(memoryManager);
        memoryManager.addMemoryMapping(this, position, bank.length, 0);
    }

}
