package com.loxassembler;

/**
 * @author Dmitry
 */
class CommandDefinition {

    private int opcode;
    private String command;
    private LoxAssembler.CommandType commandType;
    private String patternType;

    public CommandDefinition(int opcode, String command, LoxAssembler.CommandType commandType, String patternType) {
        this.opcode = opcode;
        this.command = command;
        this.commandType = commandType;
        this.patternType = patternType;
    }

    public String getPatternType() {
        return patternType;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getCommand() {
        return command;
    }

    LoxAssembler.CommandType getCommandType() {
        return commandType;
    }
}
