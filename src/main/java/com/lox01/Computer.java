package com.lox01;

import com.lox01.memmanager.MemoryController;
import com.lox01.module.AbstractModule;
import com.lox01.module.ModulesController;
import com.lox01.processor.LoxProcessor;

/**
 * @author Dmitry
 */
public class Computer {

    private LoxProcessor processor;
    private MemoryController controller;
    private ModulesController modulesController;

    public Computer() {
        controller = new MemoryController();
        modulesController = new ModulesController(this);
        processor = new LoxProcessor();
        processor.setMemoryController(controller);
    }

    public void insertModule(AbstractModule module){
        modulesController.insertModule(module);
    }
    
    public MemoryController getController() {
        return controller;
    }

    public ModulesController getModulesController() {
        return modulesController;
    }

    public LoxProcessor getProcessor() {
        return processor;
    }

}
