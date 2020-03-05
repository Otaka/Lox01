package com.loxassembler;

import com.loxassembler.parser.MatchedToken;
import com.loxassembler.parser.StringMatcher;
import com.loxassembler.parser.StringParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Dmitry
 */
public class LoxAssembler {

    private Map<String, List<CommandDefinition>> commandDefinitions = new HashMap<>();
    private StringParser parser;
    private Map<String, Label> labels = new HashMap<>();
    private String[] registers = new String[]{"r1", "r2", "r3", "r4", "rg", "sp", "pc"};
    private String[] flags = new String[]{"z", "c", "o", "n", "i"};

    static enum CommandType {
        None/**/, RegIndirect, /**/ Reg, JmpDest32, JmpDest16, TwoRegs/**/, Flag, IntIndex, Imm32ToReg,
        Reg2RegMul1, Reg2RegMul2, Reg2RegMul4, Reg2RegMul1Offset, Reg2RegMul2Offset, Reg2RegMul4Offset,
        RegMul1_2Reg, RegMul2_2Reg, RegMul4_2Reg, RegMul1Offset_2Reg, RegMul2Offset_2Reg, RegMul4Offset_2Reg,
    }

    public LoxAssembler() {
        parser.addSkipMatcher(new StringMatcher("\\s+"));
        parser.addSkipMatcher(new StringMatcher("//.*"));
        parser.addMatcher("register", new StringMatcher("r1|r2|r3|r4|rg|sp|pc"));
        parser.addMatcher("flags", new StringMatcher(Pattern.compile("z|c|o|n|i", Pattern.CASE_INSENSITIVE)));
        parser.addMatcher("token", new StringMatcher("[a-zA-Z_][a-zA-Z_0-9]*"));
        parser.addMatcher("label", new StringMatcher("([a-zA-Z_][a-zA-Z_0-9]*)\\s*:", 1));
        parser.addMatcher("binInt", new StringMatcher("0b([01_]+)", 1));
        parser.addMatcher("hexInt", new StringMatcher("0x([0-9a-fA-F_]+)", 1));
        parser.addMatcher("decInt", new StringMatcher("([0-9_]+)", 1));
        parser.addMatcher("comma", new StringMatcher(","));

        addCommandDefinition(0, "brk", CommandType.None);///
        addCommandDefinition(1, "jmp", CommandType.Reg);////
        addCommandDefinition(2, "jmp", CommandType.JmpDest32);
        addCommandDefinition(3, "jz", CommandType.JmpDest16);
        addCommandDefinition(4, "jnz", CommandType.JmpDest16);
        addCommandDefinition(5, "jg", CommandType.JmpDest16);
        addCommandDefinition(6, "jge", CommandType.JmpDest16);
        addCommandDefinition(6, "jn", CommandType.JmpDest16);
        addCommandDefinition(7, "jl", CommandType.JmpDest16);
        addCommandDefinition(7, "jnn", CommandType.JmpDest16);
        addCommandDefinition(8, "jle", CommandType.JmpDest16);
        addCommandDefinition(9, "jgu", CommandType.JmpDest16);
        addCommandDefinition(10, "jgeu", CommandType.JmpDest16);
        addCommandDefinition(10, "jnc", CommandType.JmpDest16);
        addCommandDefinition(11, "jlu", CommandType.JmpDest16);
        addCommandDefinition(11, "jc", CommandType.JmpDest16);
        addCommandDefinition(12, "jleu", CommandType.JmpDest16);
        addCommandDefinition(13, "jo", CommandType.JmpDest16);
        addCommandDefinition(14, "jno", CommandType.JmpDest16);

        addCommandDefinition(20, "add", CommandType.TwoRegs);//////////////
        addCommandDefinition(21, "adc", CommandType.TwoRegs);//////////////

        addCommandDefinition(43, "clearf", CommandType.Flag);//////////////
        addCommandDefinition(44, "setf", CommandType.Flag);////////////////
        addCommandDefinition(45, "not", CommandType.Reg);//////////////////

        addCommandDefinition(60, "mov", CommandType.Imm32ToReg);
        addCommandDefinition(61, "mov", CommandType.TwoRegs);//////////////

        addCommandDefinition(62, "mov8", CommandType.RegMul1_2Reg);
        addCommandDefinition(63, "mov8", CommandType.RegMul2_2Reg);
        addCommandDefinition(64, "mov8", CommandType.RegMul4_2Reg);
        addCommandDefinition(65, "mov8", CommandType.RegMul1Offset_2Reg);
        addCommandDefinition(66, "mov8", CommandType.RegMul2Offset_2Reg);
        addCommandDefinition(67, "mov8", CommandType.RegMul4Offset_2Reg);
        addCommandDefinition(68, "mov8", CommandType.Reg2RegMul1);
        addCommandDefinition(69, "mov8", CommandType.Reg2RegMul2);
        addCommandDefinition(70, "mov8", CommandType.Reg2RegMul4);
        addCommandDefinition(71, "mov8", CommandType.Reg2RegMul1Offset);
        addCommandDefinition(72, "mov8", CommandType.Reg2RegMul2Offset);
        addCommandDefinition(73, "mov8", CommandType.Reg2RegMul4Offset);

        addCommandDefinition(74, "mov16", CommandType.RegMul1_2Reg);
        addCommandDefinition(75, "mov16", CommandType.RegMul2_2Reg);
        addCommandDefinition(76, "mov16", CommandType.RegMul4_2Reg);
        addCommandDefinition(77, "mov16", CommandType.RegMul1Offset_2Reg);
        addCommandDefinition(78, "mov16", CommandType.RegMul2Offset_2Reg);
        addCommandDefinition(79, "mov16", CommandType.RegMul4Offset_2Reg);
        addCommandDefinition(80, "mov16", CommandType.Reg2RegMul1);
        addCommandDefinition(81, "mov16", CommandType.Reg2RegMul2);
        addCommandDefinition(82, "mov16", CommandType.Reg2RegMul4);
        addCommandDefinition(83, "mov16", CommandType.Reg2RegMul1Offset);
        addCommandDefinition(84, "mov16", CommandType.Reg2RegMul2Offset);
        addCommandDefinition(85, "mov16", CommandType.Reg2RegMul4Offset);

        addCommandDefinition(86, "mov32", CommandType.RegMul1_2Reg);
        addCommandDefinition(87, "mov32", CommandType.RegMul2_2Reg);
        addCommandDefinition(88, "mov32", CommandType.RegMul4_2Reg);
        addCommandDefinition(89, "mov32", CommandType.RegMul1Offset_2Reg);
        addCommandDefinition(90, "mov32", CommandType.RegMul2Offset_2Reg);
        addCommandDefinition(91, "mov32", CommandType.RegMul4Offset_2Reg);
        addCommandDefinition(92, "mov32", CommandType.Reg2RegMul1);
        addCommandDefinition(93, "mov32", CommandType.Reg2RegMul2);
        addCommandDefinition(94, "mov32", CommandType.Reg2RegMul4);
        addCommandDefinition(95, "mov32", CommandType.Reg2RegMul1Offset);
        addCommandDefinition(96, "mov32", CommandType.Reg2RegMul2Offset);
        addCommandDefinition(97, "mov32", CommandType.Reg2RegMul4Offset);
    }

    private void addCommandDefinition(int opcode, String command, CommandType commandType) {
        CommandDefinition cd = new CommandDefinition(opcode, command, commandType);
        List<CommandDefinition> list = commandDefinitions.get(command);
        if (list == null) {
            list = new ArrayList<>();
            commandDefinitions.put(command, list);
        }
        list.add(cd);
    }

    public byte[] assembleString(String sourceCode) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(1024);
        String[] lines = sourceCode.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            parser.reset(line);
            assembleLine(parser, i, result);
        }

        return result.toByteArray();
    }

    private void assembleLine(StringParser parser, int lineIndex, ByteArrayOutputStream result) throws IOException {
        MatchedToken m = new MatchedToken();
        if (parser.match("label", m)) {
            processLabel(m.getText(), lineIndex, parser, result);
        }

        if (parser.match("token", m)) {
            String opcode = m.getText();
            List<CommandDefinition> opcodeCommandDefinitions = commandDefinitions.get(opcode);
            boolean matched = false;
            for (CommandDefinition cd : opcodeCommandDefinitions) {
                if (tryToMatchAndProcessCommandDefinition(cd, parser, lineIndex, result, opcodeCommandDefinitions.size() == 1)) {
                    matched = true;
                    break;
                }
            }
            if (matched == false) {
                throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Cannot parse command [" + parser.getTextFromCurrentPosition(20) + "]");
            }
        }

        if (!parser.isFinished()) {
            throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Expected line end, but found [" + parser.getTextFromCurrentPosition(20) + "]");
        }
    }

    private boolean tryToMatchAndProcessCommandDefinition(CommandDefinition cd, StringParser parser, int lineIndex, ByteArrayOutputStream result, boolean throwOnError) throws IOException {
        MatchedToken m = new MatchedToken();
        if (cd.getCommandType() == CommandType.None) {
            result.write(cd.getOpcode());
            return true;
        }

        if (cd.getCommandType() == CommandType.Reg) {
            int registerIndex = parseRegister(parser, lineIndex, throwOnError);
            if (registerIndex == -1) {
                return false;
            }
            result.write(cd.getOpcode());
            result.write(registerIndex);
            return true;
        }

        if (cd.getCommandType() == CommandType.JmpDest16) {
            MutableObject<Integer> intValue = new MutableObject<>();
            if (parseNumber(parser, intValue)) {
                if (intValue.getValue() > Short.MAX_VALUE) {
                    throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Number is bigger than max 16 bit signed number " + Short.MAX_VALUE);
                } else if (intValue.getValue() < Short.MIN_VALUE) {
                    throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Number is smaller than min 16 bit signed number " + Short.MIN_VALUE);
                }
                result.write(cd.getOpcode());
                result.write(shortToByteArray((short)(int)intValue.getValue()));
                return true;
            }
            //try match label
            if(parser.match("token", m)){
                
            }
        }

        if (cd.getCommandType() == CommandType.TwoRegs) {
            int registerIndex = parseRegister(parser, lineIndex, throwOnError);
            if (registerIndex == -1) {
                return false;
            }

            if (!parser.match("comma", m)) {
                if (throwOnError) {
                    throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Expected comma, but found [" + parser.getTextFromCurrentPosition(20) + "]");
                }
                return false;
            }

            int registerIndex2 = parseRegister(parser, lineIndex, throwOnError);
            if (registerIndex == -1) {
                return false;
            }

            result.write(cd.getOpcode());
            result.write(registerIndex | (registerIndex2 << 3));
            return true;
        }

        if (cd.getCommandType() == CommandType.Flag) {
            if (!parser.match("flag", m)) {
                if (throwOnError) {
                    throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Expected flag, but found [" + parser.getTextFromCurrentPosition(20) + "]");
                }
                return false;
            }

            result.write(cd.getOpcode());
            result.write(getFlagIndex(m.getText()));
            return true;
        }

        return false;
    }

    private byte[] shortToByteArray(short x) {
        return new byte[]{
            (byte) (x & 0xff),
            (byte) ((x >> 8) & 0xff)
        };
    }

    private boolean parseNumber(StringParser parser, MutableObject<Integer> output) {
        MatchedToken m = new MatchedToken();
        if (parser.match("binInt", m)) {
            String binNumber = m.getText().replace("_", "");
            output.setValue(Integer.parseInt(binNumber, 2));
            return true;
        }
        if (parser.match("hexInt", m)) {
            String hexNumber = m.getText().replace("_", "");
            output.setValue(Integer.parseInt(hexNumber, 16));
            return true;
        }
        if (parser.match("decInt", m)) {
            String decNumber = m.getText().replace("_", "");
            output.setValue(Integer.parseInt(decNumber, 2));
            return true;
        }
        return false;
    }

    private int parseRegister(StringParser parser, int lineIndex, boolean throwOnError) {
        MatchedToken m = new MatchedToken();
        if (!parser.match("register", m)) {
            if (throwOnError) {
                throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Cannot find register. Found [" + parser.getTextFromCurrentPosition(20) + "]");
            }
            return -1;
        }
        int registerIndex = getRegisterIndex(m.getText());
        if (registerIndex == -1) {
            if (throwOnError) {
                throw new CompilationException(lineIndex, parser.getCurrentOffset(), "Unknown register [" + m.getText() + "]");
            }
            return -1;
        }

        return registerIndex;
    }

    private void processLabel(String labelName, int lineIndex, StringParser parser1, ByteArrayOutputStream byteArrayOutputStream) throws CompilationException {
        if (labels.containsKey(labelName)) {
            throw new CompilationException(lineIndex, parser1.getCurrentOffset(), "Label with name [" + labelName + "] already exists");
        }

        Label label = new Label(labelName, byteArrayOutputStream.size());
        labels.put(labelName, label);
    }

    private int indexOf(Object[] arr, Object key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)) {
                return i;
            }
        }

        return -1;
    }

    private int getRegisterIndex(String registerName) {
        return indexOf(registers, registerName);
    }

    private int getFlagIndex(String flagName) {
        return indexOf(registers, flagName);
    }
}
