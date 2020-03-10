package com.loxassembler;

import com.loxassembler.parser.StringParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dmitry
 */
public class LoxAssemblerTest {

    @Test
    public void testPatterns() throws Exception {
        //Number patterns
        assertThat(parseString(LoxAssembler.numberPattern, "987"), is(createMap("dec", "987")));
        assertThat(parseString(LoxAssembler.numberPattern, "0xAAFF"), is(createMap("hex", "0xAAFF")));
        assertThat(parseString(LoxAssembler.numberPattern, "0b01101"), is(createMap("bin", "0b01101")));

        //jump destination pattern
        assertThat(parseString(LoxAssembler.addressOrLabelPattern, "NEXT_ADDR"), is(createMap("label", "NEXT_ADDR")));
        assertThat(parseString(LoxAssembler.addressOrLabelPattern, "0x98"), is(createMap("hex", "0x98")));
        //two regs pattern
        assertThat(parseString(LoxAssembler.twoRegsPattern, "r1,r4"), is(createMap("arg1", "r1", "arg2", "r4")));
    }

    @Test
    public void testAssemblingOneRegArg() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("00"), assembler.assemble("\tbrk "));
        assertArrayEquals(byteArray("32"), assembler.assemble("ret"));
        assertArrayEquals(byteArray("01 01"), assembler.assemble("jmp r1"));
        assertArrayEquals(byteArray("01 02"), assembler.assemble("jmp r2"));
        assertArrayEquals(byteArray("01 03"), assembler.assemble("jmp r3"));
        assertArrayEquals(byteArray("01 04"), assembler.assemble("jmp r4"));
        assertArrayEquals(byteArray("01 05"), assembler.assemble("jmp rg"));
        assertArrayEquals(byteArray("01 06"), assembler.assemble("jmp sp"));
        assertArrayEquals(byteArray("01 07"), assembler.assemble("jmp pc"));
        assertArrayEquals(byteArray("33 01"), assembler.assemble("push r1"));
        assertArrayEquals(byteArray("34 02"), assembler.assemble("pop r2"));
    }

    @Test
    public void testAssemblingJmp() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("37 02 FA FF FF FF"), assembler.assemble("label1:nop\njmp label1"));
        assertArrayEquals(byteArray("02 01 00 00 00 37"), assembler.assemble("jmp label1\nnop\nlabel1:"));
        assertArrayEquals(byteArray("0B 01 00 37"), assembler.assemble("jc label1\nnop\nlabel1:"));
    }

    @Test
    public void testAssemblingInterrupt() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("38 10"), assembler.assemble("int 0x10"));
    }

    @Test
    public void testAssemblingTwoRegArg() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("14 09"), assembler.assemble("add  r1 , r1 "));
        assertArrayEquals(byteArray("14 11"), assembler.assemble("add r1,r2"));
        assertArrayEquals(byteArray("14 19"), assembler.assemble("add r1,r3"));
        assertArrayEquals(byteArray("15 21"), assembler.assemble("adc r1,r4"));
    }

    @Test
    public void testAssemblingFlags() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("2B 00"), assembler.assemble("clearf z"));
        assertArrayEquals(byteArray("2B 01"), assembler.assemble("clearf c"));
        assertArrayEquals(byteArray("2B 02"), assembler.assemble("clearf o"));
        assertArrayEquals(byteArray("2B 03"), assembler.assemble("clearf n"));
        assertArrayEquals(byteArray("2B 04"), assembler.assemble("clearf i"));
    }

    @Test
    public void testAssemblingMov() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("3C 01 DD CC BB AA"), assembler.assemble("mov r1,0xAABBCCDD"));
        assertArrayEquals(byteArray("3D 39"), assembler.assemble("mov r1,pc"));
        assertArrayEquals(byteArray("3E A1"), assembler.assemble("mov8 r1, r4:[r2]"));
        assertArrayEquals(byteArray("3E A1"), assembler.assemble("mov8 r1, r4:[r2*1]"));
        assertArrayEquals(byteArray("3E A1"), assembler.assemble("mov8 r1 , r4 : [ r2 * 1 ]"));
        assertArrayEquals(byteArray("3F A1"), assembler.assemble("mov8 r1,r4:[r2*2]"));
        assertArrayEquals(byteArray("40 A1"), assembler.assemble("mov8 r1,r4:[r2*4]"));

        assertArrayEquals(byteArray("44 A1"), assembler.assemble("mov8 r4:[r2], r1"));
        assertArrayEquals(byteArray("44 A1"), assembler.assemble("mov8 r4:[r2*1], r1"));
        assertArrayEquals(byteArray("44 A1"), assembler.assemble("mov8 r4 : [ r2 * 1 ] , r1"));
        assertArrayEquals(byteArray("45 A1"), assembler.assemble("mov8 r4:[r2*2],r1"));
        assertArrayEquals(byteArray("46 A1"), assembler.assemble("mov8 r4:[r2*4],r1"));

        assertArrayEquals(byteArray("41 01 78 98 00 00"), assembler.assemble("mov8 r1,[0x9878]"));
        assertArrayEquals(byteArray("41 A1 78 98 00 00"), assembler.assemble("mov8 r1,r4:[r2+0x9878]"));
        assertArrayEquals(byteArray("41 A1 78 98 00 00"), assembler.assemble("mov8 r1,r4:[r2 * 1 + 0x9878 ]"));
        assertArrayEquals(byteArray("42 A1 78 98 00 00"), assembler.assemble("mov8 r1,r4:[r2 * 2 + 0x9878 ]"));
        assertArrayEquals(byteArray("43 A1 78 98 00 00"), assembler.assemble("mov8 r1,r4:[r2 * 4 + 0x9878 ]"));

        assertArrayEquals(byteArray("44 41"), assembler.assemble("mov8 [r1],r1"));
    }

    @Test
    public void testAssemblingCall() throws Exception {
        LoxAssembler assembler = new LoxAssembler();
        assertArrayEquals(byteArray("31 40"), assembler.assemble("call [r1]"));
        assertArrayEquals(byteArray("30 00 85 00 00 00"), assembler.assemble("call [0x85]"));
        assertArrayEquals(byteArray("2E 50 00 00 00"), assembler.assemble("call 0x50"));
    }

    private byte[] byteArray(String hexString) {
        String[] parts = hexString.split("\\s+");
        byte[] result = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            int value = Integer.parseInt(parts[i], 16);
            if (value > 255) {
                throw new IllegalArgumentException("Cannot represent [" + parts[i] + "] as byte");
            }
            result[i] = (byte) value;
        }
        return result;
    }

    private Map<String, String> createMap(String... values) {
        if ((values.length % 2) != 0) {
            throw new IllegalArgumentException("Arguments should be list of pairs, but found " + values.length + " elements");
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            String key = values[i];
            String value = values[i + 1];
            result.put(key, value);
        }
        return result;
    }

    private Map<String, String> parseString(String patternString, String value) throws Exception {
        Pattern pattern = Pattern.compile(patternString);
        Matcher m = pattern.matcher(value);
        if (!m.matches()) {
            fail("Pattern did not match string [" + value + "]");
        }

        List<String> namedGroups = StringParser.getNamedGroupsFromPattern(pattern);
        Map<String, String> result = new HashMap<>();
        for (String group : namedGroups) {
            String groupValue = m.group(group);
            if (groupValue != null) {
                result.put(group, groupValue);
            }
        }
        return result;
    }

}
