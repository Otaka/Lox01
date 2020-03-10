package com.loxassembler;

import com.loxassembler.parser.MatchedToken;
import com.loxassembler.parser.StringMatcher;
import com.loxassembler.parser.StringParser;
import com.loxassembler.parser.StringWithPosition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class LoxAssembler {

    private Map<String, List<CommandDefinition>> commandDefinitions = new HashMap<>();
    private StringParser parser;
    private Map<String, Label> labels = new HashMap<>();
    private String[] registers = new String[]{"zero register", "r1", "r2", "r3", "r4", "rg", "sp", "pc"};
    private final String[] flags = new String[]{"z", "c", "o", "n", "i"};
    private List<LabelRelocation> labelRelocations = new ArrayList<>();

    static enum CommandType {
        None, indirectReg, indirectRegOffset, Reg, JmpDest32, JmpDest16, TwoRegs, Flag, IntIndex, Imm32ToReg,
        Reg2RegMul1, Reg2RegMul2, Reg2RegMul4, Reg2RegMul1Offset, Reg2RegMul2Offset, Reg2RegMul4Offset,
        RegMul1_2Reg, RegMul2_2Reg, RegMul4_2Reg, RegMul1Offset_2Reg, RegMul2Offset_2Reg, RegMul4Offset_2Reg,
    }
    final static String registerPattern = "(r1|r2|r3|r4|rg|sp|pc)";
    final static String tokenPattern = "[a-zA-Z_][a-zA-Z_0-9]*";
    final static String numberPattern = "(?<hex>0x[0-9A-Fa-f_]+)|(?<bin>0b[01_]+)|(?<dec>\\-?[0-9_]+)";
    final static String number2RegPattern = "(?<reg>" + registerPattern + ")\\s*,\\s*(" + numberPattern + ")";
    final static String addressOrLabelPattern = "(?<label>" + tokenPattern + ")|" + numberPattern;
    final static String oneRegPattern = "(?<regArg>" + registerPattern + ")";
    final static String twoRegsPattern = "(?<arg1>" + registerPattern + ")\\s*,\\s*(?<arg2>" + registerPattern + ")";
    final static String flagsPattern = "(?<flag>[zconiZCONI])";
    final static String complexAddressingPartPattern = "((?<baseReg>" + registerPattern + ")\\s*:\\s*)?\\[\\s*(?<indexReg>" + registerPattern + ")?$multiplier$indmultiplier\\s*\\]"; //(\\s*\\+\\s*(?<indOffset>" + numberPattern + "))?
    final static String regMulOffset2RegPattern = "(?<destReg>" + registerPattern + ")\\s*,\\s*" + complexAddressingPartPattern;
    final static String reg2RegMulOffsetPattern = complexAddressingPartPattern + "\\s*,\\s*(?<sourceReg>" + registerPattern + ")";
    final static String indexPattern = "(\\s*\\+?\\s*(?<indOffset>" + numberPattern + "))";

    public LoxAssembler() {
        parser = new StringParser();
        parser.addSkipMatcher(new StringMatcher("\\/\\/.*"));
        parser.addSkipMatcher(new StringMatcher("\\s+"));
        parser.addMatcher("label", new StringMatcher("(?<labelText>[a-zA-Z_][a-zA-Z_0-9]*)\\s*:"));
        parser.addMatcher("numberPattern", new StringMatcher(numberPattern));
        parser.addMatcher("token", new StringMatcher("(?<token>" + tokenPattern + ")"));
        parser.addMatcher("addressOrLabelPattern", new StringMatcher(addressOrLabelPattern));
        parser.addMatcher("number2RegPattern", new StringMatcher(number2RegPattern));
        parser.addMatcher("oneRegPattern", new StringMatcher(oneRegPattern));
        parser.addMatcher("twoRegsPattern", new StringMatcher(twoRegsPattern));
        parser.addMatcher("flagsPattern", new StringMatcher(flagsPattern));
        parser.addMatcher("indirectReg", new StringMatcher(complexAddressingPartPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", "")));
        parser.addMatcher("indirectRegOffset", new StringMatcher(complexAddressingPartPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("regMul_1_2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", "")));
        parser.addMatcher("regMul_2_2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "\\s*\\*\\s*2").replace("$indmultiplier", "")));
        parser.addMatcher("regMul_4_2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "\\s*\\*\\s*4").replace("$indmultiplier", "")));
        parser.addMatcher("regMul_1_Offset2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("regMul_2_Offset2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "\\s*\\*\\s*2").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("regMul_4_Offset2RegPattern", new StringMatcher(regMulOffset2RegPattern.replace("$multiplier", "\\s*\\*\\s*4").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("reg2RegMul_1_Pattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", "")));
        parser.addMatcher("reg2RegMul_2_Pattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "\\s*\\*\\s*2").replace("$indmultiplier", "")));
        parser.addMatcher("reg2RegMul_4_Pattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "\\s*\\*\\s*4").replace("$indmultiplier", "")));
        parser.addMatcher("reg2RegMul_1_OffsetPattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "(\\s*\\*\\s*1)?").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("reg2RegMul_2_OffsetPattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "\\s*\\*\\s*2").replace("$indmultiplier", indexPattern)));
        parser.addMatcher("reg2RegMul_4_OffsetPattern", new StringMatcher(reg2RegMulOffsetPattern.replace("$multiplier", "\\s*\\*\\s*4").replace("$indmultiplier", indexPattern)));

        addCommandDefinition(0, "brk", CommandType.None, null);//////////////////////////////////////////
        addCommandDefinition(1, "jmp", CommandType.Reg, "oneRegPattern");////////////////////////////////
        addCommandDefinition(2, "jmp", CommandType.JmpDest32, "addressOrLabelPattern");//////////////////
        addCommandDefinition(3, "jz", CommandType.JmpDest16, "addressOrLabelPattern");///////////////////
        addCommandDefinition(4, "jnz", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(5, "jg", CommandType.JmpDest16, "addressOrLabelPattern");///////////////////
        addCommandDefinition(6, "jge", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(6, "jn", CommandType.JmpDest16, "addressOrLabelPattern");///////////////////
        addCommandDefinition(7, "jl", CommandType.JmpDest16, "addressOrLabelPattern");///////////////////
        addCommandDefinition(7, "jnn", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(8, "jle", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(9, "jgu", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(10, "jgeu", CommandType.JmpDest16, "addressOrLabelPattern");////////////////
        addCommandDefinition(10, "jnc", CommandType.JmpDest16, "addressOrLabelPattern");/////////////////
        addCommandDefinition(11, "jlu", CommandType.JmpDest16, "addressOrLabelPattern");/////////////////
        addCommandDefinition(11, "jc", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(12, "jleu", CommandType.JmpDest16, "addressOrLabelPattern");////////////////
        addCommandDefinition(13, "jo", CommandType.JmpDest16, "addressOrLabelPattern");//////////////////
        addCommandDefinition(14, "jno", CommandType.JmpDest16, "addressOrLabelPattern");/////////////////

        addCommandDefinition(20, "add", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(21, "adc", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(22, "sub", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(23, "subc", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////
        addCommandDefinition(24, "cmp", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(25, "mul", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(26, "mulu", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////
        addCommandDefinition(27, "div", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(28, "divu", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////
        addCommandDefinition(29, "rem", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(30, "and", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(31, "or", CommandType.TwoRegs, "twoRegsPattern");///////////////////////////
        addCommandDefinition(32, "xor", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(33, "shr", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(34, "sar", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(35, "shl", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(36, "rol", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(37, "ror", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////
        addCommandDefinition(38, "addf", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////
        addCommandDefinition(39, "subf", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////   
        addCommandDefinition(40, "cmpf", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////   
        addCommandDefinition(41, "mulf", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////   
        addCommandDefinition(42, "divf", CommandType.TwoRegs, "twoRegsPattern");/////////////////////////   
        addCommandDefinition(43, "clearf", CommandType.Flag, "flagsPattern");////////////////////////////
        addCommandDefinition(44, "setf", CommandType.Flag, "flagsPattern");//////////////////////////////
        addCommandDefinition(45, "not", CommandType.Reg, "oneRegPattern");///////////////////////////////
        addCommandDefinition(46, "call", CommandType.JmpDest32, "addressOrLabelPattern");///////////////

        addCommandDefinition(48, "call", CommandType.indirectRegOffset, "indirectRegOffset");////////////
        addCommandDefinition(49, "call", CommandType.indirectReg, "indirectReg");////////////////////////
        addCommandDefinition(50, "ret", CommandType.None, null);/////////////////////////////////////////
        addCommandDefinition(51, "push", CommandType.Reg, "oneRegPattern");//////////////////////////////
        addCommandDefinition(52, "pop", CommandType.Reg, "oneRegPattern");///////////////////////////////
        addCommandDefinition(53, "pushf", CommandType.None, null);///////////////////////////////////////
        addCommandDefinition(54, "popf", CommandType.None, null);////////////////////////////////////////
        addCommandDefinition(55, "nop", CommandType.None, null);/////////////////////////////////////////
        addCommandDefinition(56, "int", CommandType.IntIndex, "numberPattern");//////////////////////////

        addCommandDefinition(60, "mov", CommandType.Imm32ToReg, "number2RegPattern");////////////////////
        addCommandDefinition(61, "mov", CommandType.TwoRegs, "twoRegsPattern");//////////////////////////

        addCommandDefinition(62, "mov8", CommandType.RegMul1_2Reg, "regMul_1_2RegPattern");//////////////
        addCommandDefinition(63, "mov8", CommandType.RegMul2_2Reg, "regMul_2_2RegPattern");//////////////
        addCommandDefinition(64, "mov8", CommandType.RegMul4_2Reg, "regMul_4_2RegPattern");//////////////
        addCommandDefinition(65, "mov8", CommandType.RegMul1Offset_2Reg, "regMul_1_Offset2RegPattern");//
        addCommandDefinition(66, "mov8", CommandType.RegMul2Offset_2Reg, "regMul_2_Offset2RegPattern");//
        addCommandDefinition(67, "mov8", CommandType.RegMul4Offset_2Reg, "regMul_4_Offset2RegPattern");//
        addCommandDefinition(68, "mov8", CommandType.Reg2RegMul1, "reg2RegMul_1_Pattern");///////////////
        addCommandDefinition(69, "mov8", CommandType.Reg2RegMul2, "reg2RegMul_2_Pattern");///////////////
        addCommandDefinition(70, "mov8", CommandType.Reg2RegMul4, "reg2RegMul_4_Pattern");///////////////
        addCommandDefinition(71, "mov8", CommandType.Reg2RegMul1Offset, "reg2RegMul_1_OffsetPattern");///
        addCommandDefinition(72, "mov8", CommandType.Reg2RegMul2Offset, "reg2RegMul_2_OffsetPattern");///
        addCommandDefinition(73, "mov8", CommandType.Reg2RegMul4Offset, "reg2RegMul_4_OffsetPattern");///

        addCommandDefinition(74, "mov16", CommandType.RegMul1_2Reg, "regMul_1_2RegPattern");
        addCommandDefinition(75, "mov16", CommandType.RegMul2_2Reg, "regMul_2_2RegPattern");
        addCommandDefinition(76, "mov16", CommandType.RegMul4_2Reg, "regMul_4_2RegPattern");
        addCommandDefinition(77, "mov16", CommandType.RegMul1Offset_2Reg, "regMul_1_Offset2RegPattern");
        addCommandDefinition(78, "mov16", CommandType.RegMul2Offset_2Reg, "regMul_2_Offset2RegPattern");
        addCommandDefinition(79, "mov16", CommandType.RegMul4Offset_2Reg, "regMul_4_Offset2RegPattern");
        addCommandDefinition(80, "mov16", CommandType.Reg2RegMul1, "reg2RegMul_1_Pattern");
        addCommandDefinition(81, "mov16", CommandType.Reg2RegMul2, "reg2RegMul_2_Pattern");
        addCommandDefinition(82, "mov16", CommandType.Reg2RegMul4, "reg2RegMul_4_Pattern");
        addCommandDefinition(83, "mov16", CommandType.Reg2RegMul1Offset, "reg2RegMul_1_OffsetPattern");
        addCommandDefinition(84, "mov16", CommandType.Reg2RegMul2Offset, "reg2RegMul_2_OffsetPattern");
        addCommandDefinition(85, "mov16", CommandType.Reg2RegMul4Offset, "reg2RegMul_4_OffsetPattern");

        addCommandDefinition(86, "mov32", CommandType.RegMul1_2Reg, "regMul_1_2RegPattern");
        addCommandDefinition(87, "mov32", CommandType.RegMul2_2Reg, "regMul_2_2RegPattern");
        addCommandDefinition(88, "mov32", CommandType.RegMul4_2Reg, "regMul_4_2RegPattern");
        addCommandDefinition(89, "mov32", CommandType.RegMul1Offset_2Reg, "regMul_1_Offset2RegPattern");
        addCommandDefinition(90, "mov32", CommandType.RegMul2Offset_2Reg, "regMul_2_Offset2RegPattern");
        addCommandDefinition(91, "mov32", CommandType.RegMul4Offset_2Reg, "regMul_4_Offset2RegPattern");
        addCommandDefinition(92, "mov32", CommandType.Reg2RegMul1, "reg2RegMul_1_Pattern");
        addCommandDefinition(93, "mov32", CommandType.Reg2RegMul2, "reg2RegMul_2_Pattern");
        addCommandDefinition(94, "mov32", CommandType.Reg2RegMul4, "reg2RegMul_4_Pattern");
        addCommandDefinition(95, "mov32", CommandType.Reg2RegMul1Offset, "reg2RegMul_1_OffsetPattern");
        addCommandDefinition(96, "mov32", CommandType.Reg2RegMul2Offset, "reg2RegMul_2_OffsetPattern");
        addCommandDefinition(97, "mov32", CommandType.Reg2RegMul4Offset, "reg2RegMul_4_OffsetPattern");
    }

    private void addCommandDefinition(int opcode, String command, CommandType commandType, String patternName) {
        CommandDefinition cd = new CommandDefinition(opcode, command, commandType, patternName);
        List<CommandDefinition> list = commandDefinitions.get(command);
        if (list == null) {
            list = new ArrayList<>();
            commandDefinitions.put(command, list);
        }

        list.add(cd);
    }

    public byte[] assemble(String sourceCode) throws IOException {
        labels.clear();
        labelRelocations.clear();
        ByteOutputStream result = new ByteOutputStream();
        String[] lines = sourceCode.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            parser.reset(line);
            assembleLine(parser, i, result);
        }

        byte[] code = processLabelRelocations(result.getBytes());
        return code;
    }

    private byte[] processLabelRelocations(byte[] code) {
        for (LabelRelocation labelRelocation : labelRelocations) {
            Label label = labels.get(labelRelocation.getLabel());
            if (label == null) {
                throw new CompilationException(labelRelocation.getLineIndex(), 0, "Cannot find label [" + labelRelocation.getLabel() + "]");
            }

            int offsetDelta = label.getOffset() - labelRelocation.getVirtualOffset() - ((labelRelocation.getSize() == 16) ? 2 : 4);
            if (labelRelocation.getSize() == 16) {
                if (offsetDelta >= Short.MAX_VALUE) {
                    throw new CompilationException(labelRelocation.getLineIndex(), 0, "Cannot reference label beyond than " + Short.MAX_VALUE + " but requested jump offset is " + offsetDelta);
                }
                if (offsetDelta <= Short.MIN_VALUE) {
                    throw new CompilationException(labelRelocation.getLineIndex(), 0, "Cannot reference label beyond than " + Short.MIN_VALUE + " but requested jump offset is " + offsetDelta);
                }

                System.arraycopy(Utils.shortToByteArray((short) offsetDelta), 0, code, labelRelocation.getByteOffset(), 2);
            } else {
                System.arraycopy(Utils.intToByteArray(offsetDelta), 0, code, labelRelocation.getByteOffset(), 4);
            }
        }

        return code;
    }

    private void assembleLine(StringParser parser, int lineIndex, ByteOutputStream result) throws IOException {
        //process label definition
        MatchedToken matchedToken = new MatchedToken();
        if (parser.match("label", matchedToken)) {
            StringWithPosition textLabel = matchedToken.getMatchedGroups().get("labelText");
            processLabel(textLabel.getString(), lineIndex, parser, result);
        }

        parser.skip();
        if (parser.isFinished()) {
            return;
        }
        //process instructions
        if (parser.match("token", matchedToken)) {
            StringWithPosition instruction = matchedToken.getMatchedGroups().get("token");
            List<CommandDefinition> instructionCommandDefinitions = commandDefinitions.get(instruction.getString());
            if (instructionCommandDefinitions == null) {
                throw new CompilationException(lineIndex, instruction.getStart(), "Unknown instruction or directive [" + instruction.getString() + "]");
            }
            boolean found = false;
            for (CommandDefinition commandDefinition : instructionCommandDefinitions) {
                if (commandDefinition.getPatternType() == null) {
                    result.write(commandDefinition.getOpcode());
                    found = true;
                    break;
                } else if (parser.match(commandDefinition.getPatternType(), matchedToken)) {
                    processCommand(commandDefinition, lineIndex, matchedToken, result);
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new CompilationException(lineIndex, matchedToken.getPosition(), "Cannot parse arguments of command " + instruction.getString());
            }
        }

        parser.skip();
        if (!parser.isFinished()) {
            throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Expected line end, but found [" + parser.getTextFromCurrentPosition(20) + "]");
        }
    }

    private void processCommand(CommandDefinition commandDefinition, int lineIndex, MatchedToken matchedToken, ByteOutputStream result) throws IOException {
        result.write(commandDefinition.getOpcode());
        if (commandDefinition.getCommandType() == CommandType.None) {
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.Reg) {
            StringWithPosition registerName = matchedToken.getMatchedGroups().get("regArg");
            int regIndex = getRegisterIndex(registerName, lineIndex, true);
            result.write(createModRRByte(regIndex, 0, 0));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.Flag) {
            StringWithPosition flagName = matchedToken.getMatchedGroups().get("flag");
            int flagIndex = getFlagIndex(flagName, lineIndex, true);
            result.write(flagIndex);
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.TwoRegs) {
            StringWithPosition registerName1 = matchedToken.getMatchedGroups().get("arg1");
            StringWithPosition registerName2 = matchedToken.getMatchedGroups().get("arg2");
            int regIndex1 = getRegisterIndex(registerName1, lineIndex, true);
            int regIndex2 = getRegisterIndex(registerName2, lineIndex, true);
            result.write(createModRRByte(regIndex1, regIndex2, 0));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.JmpDest32) {
            result.write(processJmpLabelOrNumber(commandDefinition, lineIndex, matchedToken, 32, result));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.JmpDest16) {
            result.write(processJmpLabelOrNumber(commandDefinition, lineIndex, matchedToken, 16, result));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.IntIndex) {
            int value = parseNumber(matchedToken) & 0xFFFFFFFF;
            if (value > 255) {
                throw new CompilationException(lineIndex, matchedToken.getPosition(), "Interrupt number cannot be more than 255, but found " + value);
            }
            result.write(value);
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.indirectReg) {
            ComplexAddressing complexAddressing = parseComplexAddressing(matchedToken, lineIndex);
            result.write(createModRRByte(complexAddressing.getSourceDestRegIndex(), complexAddressing.getBaseRegister(), complexAddressing.getIndexRegister()));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.indirectRegOffset) {
            ComplexAddressing complexAddressing = parseComplexAddressing(matchedToken, lineIndex);
            result.write(createModRRByte(complexAddressing.getSourceDestRegIndex(), complexAddressing.getBaseRegister(), complexAddressing.getIndexRegister(), complexAddressing.getOffset()));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.Imm32ToReg) {
            StringWithPosition registerName = matchedToken.getMatchedGroups().get("reg");
            int regIndex = getRegisterIndex(registerName, lineIndex, true);
            int integerValue = parseNumber(matchedToken);
            result.write(createModRRByte(regIndex, 0, 0));
            result.write(Utils.intToByteArray(integerValue));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.RegMul1_2Reg
                || commandDefinition.getCommandType() == CommandType.RegMul2_2Reg
                || commandDefinition.getCommandType() == CommandType.RegMul4_2Reg
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul1
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul2
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul4) {
            ComplexAddressing complexAddressing = parseComplexAddressing(matchedToken, lineIndex);
            result.write(createModRRByte(complexAddressing.getSourceDestRegIndex(), complexAddressing.getBaseRegister(), complexAddressing.getIndexRegister()));
            return;
        }

        if (commandDefinition.getCommandType() == CommandType.RegMul1Offset_2Reg
                || commandDefinition.getCommandType() == CommandType.RegMul2Offset_2Reg
                || commandDefinition.getCommandType() == CommandType.RegMul4Offset_2Reg
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul1Offset
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul2Offset
                || commandDefinition.getCommandType() == CommandType.Reg2RegMul4Offset) {
            ComplexAddressing complexAddressing = parseComplexAddressing(matchedToken, lineIndex);
            result.write(createModRRByte(complexAddressing.getSourceDestRegIndex(), complexAddressing.getBaseRegister(), complexAddressing.getIndexRegister()));
            result.write(Utils.intToByteArray(complexAddressing.getOffset()));
            return;
        }

        throw new IllegalStateException("Command Type [" + commandDefinition.getCommandType() + "] is not implemented");
    }

    private byte[] processJmpLabelOrNumber(CommandDefinition commandDefinition, int lineIndex, MatchedToken matchedToken, int size, ByteOutputStream result) {
        if (matchedToken.getMatchedGroups().containsKey("label")) {
            StringWithPosition label = matchedToken.getMatchedGroups().get("label");
            if (labels.containsKey(label.getString())) {
                int currentOffset = result.getCurrentVirtualAddress();
                currentOffset += ((size == 32) ? 4 : 2);
                int offsetDelta = labels.get(label.getString()).getOffset() - currentOffset;
                if (size == 16) {
                    if (offsetDelta >= Short.MAX_VALUE) {
                        throw new CompilationException(lineIndex, matchedToken.getMatchedGroups().get("label").getStart(), "Cannot reference to label to more than " + Short.MAX_VALUE + " but requested jump offset is " + offsetDelta);
                    }
                    if (offsetDelta <= Short.MIN_VALUE) {
                        throw new CompilationException(lineIndex, matchedToken.getMatchedGroups().get("label").getStart(), "Cannot reference to label to more than " + Short.MIN_VALUE + " but requested jump offset is " + offsetDelta);
                    }
                    return Utils.shortToByteArray((short) offsetDelta);
                } else {
                    return Utils.intToByteArray(offsetDelta);
                }
            } else {
                int currentVirtualOffset = result.getCurrentVirtualAddress();
                int currentByteOffset = result.getCurrentByteOffset();
                labelRelocations.add(new LabelRelocation(label.getString(), currentVirtualOffset, currentByteOffset, size, lineIndex));
                if (size == 16) {
                    return Utils.shortToByteArray((short) 0);
                } else {
                    return Utils.intToByteArray(0);
                }
            }
        } else if (hasNumber(matchedToken)) {
            int offset = parseNumber(matchedToken);
            if (size == 32) {
                return Utils.intToByteArray(offset);
            } else {
                if (offset >= Short.MAX_VALUE) {
                    throw new CompilationException(lineIndex, matchedToken.getMatchedGroups().get("label").getStart(), "Cannot make jump to more than " + Short.MAX_VALUE + " but requested jump to " + offset);
                }
                if (offset <= Short.MIN_VALUE) {
                    throw new CompilationException(lineIndex, matchedToken.getMatchedGroups().get("label").getStart(), "Cannot make jump to more than " + Short.MIN_VALUE + " but requested jump to " + offset);
                }
                return Utils.shortToByteArray((short) offset);
            }
        } else {
            throw new CompilationException(lineIndex, matchedToken.getPosition(), "Expected number or label, but found [" + matchedToken.getText() + "]");
        }
    }

    private boolean hasNumber(MatchedToken m) {
        Map<String, StringWithPosition> map = m.getMatchedGroups();
        return map.containsKey("hex") || map.containsKey("bin") || map.containsKey("dec");
    }

    private int parseNumber(MatchedToken m) {
        if (m.getMatchedGroups().containsKey("hex")) {
            String hexValue = m.getMatchedGroups().get("hex").getString();
            hexValue = hexValue.replace("_", "").replace("0x", "");
            return Integer.parseUnsignedInt(hexValue, 16);
        }
        if (m.getMatchedGroups().containsKey("dec")) {
            String decValue = m.getMatchedGroups().get("dec").getString();
            decValue = decValue.replace("_", "");
            return Integer.parseUnsignedInt(decValue);
        }
        if (m.getMatchedGroups().containsKey("bin")) {
            String binValue = m.getMatchedGroups().get("bin").getString();
            binValue = binValue.replace("_", "").replace("0b", "");
            return Integer.parseUnsignedInt(binValue, 2);
        }
        throw new RuntimeException("Cannot compile string [" + m.getText() + "] as number");
    }

    private void processLabel(String labelName, int lineIndex, StringParser parser, ByteOutputStream byteArrayOutputStream) throws CompilationException {
        if (labels.containsKey(labelName)) {
            throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Label with name [" + labelName + "] already exists");
        }

        Label label = new Label(labelName, byteArrayOutputStream.getCurrentVirtualAddress());
        labels.put(labelName, label);
    }

    private int getRegisterIndex(StringWithPosition registerName, int lineIndex, boolean fail) {
        int regIndex = Utils.indexOf(registers, registerName.getString());
        if (fail && regIndex == -1) {
            throw new CompilationException(lineIndex, registerName.getStart(), "Expected register name, but found [" + registerName + "]");
        }
        return regIndex;
    }

    private int getFlagIndex(StringWithPosition flagName, int lineIndex, boolean fail) {
        int flagIndex = Utils.indexOf(flags, flagName.getString());
        if (fail && flagIndex == -1) {
            throw new CompilationException(lineIndex, flagName.getStart(), "Expected flag name, but found [" + flagName + "]");
        }
        return flagIndex;
    }

    private byte[] createModRRByte(int reg1, int reg2, int indexReg) {
        int value = (reg1 & 0b111) | ((reg2 & 0b111) << 3) | ((indexReg & 0b11) << 6);
        return new byte[]{(byte) value};
    }

    private byte[] createModRRByte(int reg1, int reg2, int indexReg, int offset32) {
        byte[] result = Arrays.copyOf(createModRRByte(reg1, reg2, indexReg), 5);
        byte[] offsetByteArray = Utils.intToByteArray(offset32);
        System.arraycopy(offsetByteArray, 0, result, 1, 4);
        return result;
    }

    private ComplexAddressing parseComplexAddressing(MatchedToken mt, int lineIndex) {
        int sourceDestReg = 0;
        int baseReg = 0;
        int indexRegister = 0;
        int offset = 0;
        if (mt.getMatchedGroups().containsKey("baseReg")) {
            baseReg = getRegisterIndex(mt.getMatchedGroups().get("baseReg"), lineIndex, true);
        }
        if (mt.getMatchedGroups().containsKey("indexReg")) {
            StringWithPosition indexRegString = mt.getMatchedGroups().get("indexReg");
            indexRegister = getRegisterIndex(indexRegString, lineIndex, true);
            if (indexRegister > 3) {
                throw new CompilationException(lineIndex, indexRegString.getStart(), "You cannot use " + indexRegString.getString() + " as index register. Only r1,r2,r3 are allowed there");
            }
        }
        if (mt.getMatchedGroups().containsKey("indOffset")) {
            offset = parseNumber(mt);
        }
        if (mt.getMatchedGroups().containsKey("destReg")) {
            sourceDestReg = getRegisterIndex(mt.getMatchedGroups().get("destReg"), lineIndex, true);
        }
        if (mt.getMatchedGroups().containsKey("sourceReg")) {
            sourceDestReg = getRegisterIndex(mt.getMatchedGroups().get("sourceReg"), lineIndex, true);
        }

        return new ComplexAddressing(offset, sourceDestReg, baseReg, indexRegister);
    }
}
