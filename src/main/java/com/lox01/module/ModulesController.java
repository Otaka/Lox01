package com.lox01.module;

import com.lox01.Computer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class ModulesController {

    private List<AbstractModule> modules = new ArrayList<>();
    private Computer computer;

    public ModulesController(Computer computer) {
        this.computer = computer;
    }

    public void insertModule(AbstractModule module) {
        modules.add(module);
    }
}
