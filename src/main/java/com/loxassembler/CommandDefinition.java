package com.loxassembler;

/**
 * @author Dmitry
 */
class CommandDefinition {

    private int opcode;
    private String command;
    private LoxAssembler.CommandType commandType;

    public CommandDefinition(int opcode, String command, LoxAssembler.CommandType commandType) {
        this.opcode = opcode;
        this.command = command;
        this.commandType = commandType;
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
