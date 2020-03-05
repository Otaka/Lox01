package com.lox01.processor;

import com.lox01.memmanager.MemoryController;

/**
 * @author Dmitry
 */
public class LoxProcessor {

    private MemoryController memoryController;

    private int[] registers = new int[8];//R1,R2,R3,R4,RG,SP,PC,ZERO_REG
    private static final int R1 = 0;
    private static final int R2 = 1;
    private static final int R3 = 2;
    private static final int R4 = 3;
    private static final int RG = 4;
    private static final int SP = 5;
    private static final int PC = 6;
    private static final int ZERO_REG = 7;

    //flag register
    int zeroFlag = 0;       //bit 0
    int carryFlag = 0;      //bit 1
    int overflowFlag = 0;   //bit 2
    int negativeFlag = 0;   //bit 3
    int interruptEnableFlag = 0;  //bit 4

    private int interruptRequest = 0;
    private boolean run;

    private int internalRegister1;
    private int internalRegister2;
    private int internalIndexRegister;

    public void executeLoop() {
        while (run) {
            int opcode = memoryController.getMem8(registers[PC]++);
            switch (opcode) {
                case 1://JMP REG
                {
                    readRegPointers();
                    registers[PC] = registers[internalRegister1];
                    break;
                }
                case 2://JMP offset32
                {
                    int value = memoryController.getMem32(registers[PC]);
                    registers[PC] += (4 + value);
                    break;
                }
                case 3://JZ offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (zeroFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 4://JNZ offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (zeroFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 5://JG offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (zeroFlag == 0 && negativeFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 6://JGE offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (negativeFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 7://JL offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (negativeFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 8://JLE offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (zeroFlag == 1 || negativeFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 9://JGU offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (carryFlag == 0 || zeroFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 10://JGEU offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (carryFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 11://JLU offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (carryFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 12://JLEU offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (carryFlag == 1 || zeroFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 13://JO offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (overflowFlag == 1) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 14://JNO offset16
                {
                    int offset = memoryController.getMem16(registers[PC]);
                    registers[PC] += 2;
                    if (overflowFlag == 0) {
                        registers[PC] += offset;
                    }
                    break;
                }
                case 20://ADD REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & (long) 0xFFFFFFFF + y & (long) 0xFFFFFFFF);
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 21://ADDC REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & ((long) 0xFFFFFFFF) + y & (long) 0xFFFFFFFF) + carryFlag;
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 22://SUB REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & ((long) 0xFFFFFFFF) - y & (long) 0xFFFFFFFF);
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 23://SUBC REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & ((long) 0xFFFFFFFF) - y & (long) 0xFFFFFFFF) - ((carryFlag == 0) ? 1 : 0);
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 24://CMP REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & ((long) 0xFFFFFFFF) - y & (long) 0xFFFFFFFF);
                    updateNZ(result);
                    updateOC(x, y, result);
                    break;
                }
                case 25://MUL REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = x * y;
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 26://MULU REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (x & (long) 0xFFFFFFFF) * (y & (long) 0xFFFFFFFF);
                    updateNZ(result);
                    updateOC(x, y, result);
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 27://DIV REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = x / y;
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 28://DIVU REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    long result = (long) (x & ((long) 0xFFFFFFFF) / y & (long) 0xFFFFFFFF);
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 29://REM REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x % y;
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 30://AND REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x & y;
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 31://OR REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x | y;
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 32://XOR REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x ^ y;
                    updateNZ(result);
                    carryFlag = 0;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 33://SHR REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x >>> y;
                    updateNZ(result);
                    carryFlag = (x >> (y - 1)) & 0x1;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 34://SAR REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x >> y;
                    updateNZ(result);
                    carryFlag = (x >> (y - 1)) & 0x1;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 35://SHL REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    int result = x << y;
                    updateNZ(result);
                    carryFlag = (x >> (y + 1)) & 0x1;
                    overflowFlag = 0;
                    registers[internalRegister1] = (int) result;
                    break;
                }
                case 36://ROL REG,REG
                {
                    readRegPointers();
                    int result = Integer.rotateLeft(registers[internalRegister1], registers[internalRegister2]);
                    registers[internalRegister1] = result;
                    updateNZ(result);
                    overflowFlag = 0;
                    carryFlag = 0;
                    break;
                }
                case 37://ROR REG,REG
                {
                    readRegPointers();
                    int result = Integer.rotateRight(registers[internalRegister1], registers[internalRegister2]);
                    registers[internalRegister1] = result;
                    updateNZ(result);
                    overflowFlag = 0;
                    carryFlag = 0;
                    break;
                }
                case 38://addf REG,REG
                {
                    readRegPointers();
                    float result = Float.intBitsToFloat(registers[internalRegister1]) + Float.intBitsToFloat(registers[internalRegister2]);
                    registers[internalRegister1] = Float.floatToIntBits(result);
                    setFlags((result < 0 ? 1 : 0), 0, 0, 0);
                    break;
                }
                case 39://subf REG,REG
                {
                    readRegPointers();
                    float result = Float.intBitsToFloat(registers[internalRegister1]) - Float.intBitsToFloat(registers[internalRegister2]);
                    registers[internalRegister1] = Float.floatToIntBits(result);
                    setFlags((result < 0 ? 1 : 0), 0, 0, 0);
                    break;
                }
                case 40://cmpf REG,REG
                {
                    readRegPointers();
                    float x = Float.intBitsToFloat(registers[internalRegister1]);
                    float y = Float.intBitsToFloat(registers[internalRegister2]);

                    setFlags(x < y ? 1 : 0, ((x == y) ? 1 : 0), 0, 0);
                    break;
                }
                case 41://mulf REG,REG
                {
                    readRegPointers();
                    float result = Float.intBitsToFloat(registers[internalRegister1]) * Float.intBitsToFloat(registers[internalRegister2]);
                    registers[internalRegister1] = Float.floatToIntBits(result);
                    setFlags((result < 0 ? 1 : 0), 0, 0, 0);
                    break;
                }
                case 42://mulf REG,REG
                {
                    readRegPointers();
                    int x = registers[internalRegister1];
                    int y = registers[internalRegister2];
                    float result = Float.intBitsToFloat(x) / Float.intBitsToFloat(y);
                    registers[internalRegister1] = Float.floatToIntBits(result);
                    setFlags((result < 0 ? 1 : 0), 0, 0, 0);
                    break;
                }
                case 43://clearf FLAG
                {
                    readRegPointers();
                    setFlagValue(internalRegister1, 0);
                    break;
                }
                case 44://setf FLAG
                {
                    readRegPointers();
                    setFlagValue(internalRegister1, 1);
                    break;
                }
                case 45://not REG
                {
                    readRegPointers();
                    int value = registers[internalRegister1];
                    value = ~value;
                    registers[internalRegister1] = value;
                    updateNZ(value);
                    overflowFlag = 0;
                    carryFlag = 0;
                    break;
                }
                case 46://calla POS32
                {
                    int newPosition = readMem32PC();
                    int oldPc = registers[PC];
                    pushValue(oldPc);
                    registers[PC] = newPosition;
                    break;
                }
                case 47://call offset32
                {
                    int newPosition = readMem32PC();
                    int oldPc = registers[PC];
                    pushValue(oldPc);
                    registers[PC] = oldPc + newPosition;
                    break;
                }
                case 48://call REG
                {
                    readRegPointers();
                    int newPosition = registers[internalRegister1];
                    pushValue(registers[PC]);
                    registers[PC] = newPosition;
                    break;
                }
                case 49://ret
                {
                    int value = popValue();
                    registers[PC] = value;
                    break;
                }
                case 50://push reg
                {
                    readRegPointers();
                    if (internalRegister1 > 7) {
                        break;
                    }
                    pushValue(registers[internalRegister1]);
                    break;
                }
                case 51://pop reg
                {
                    readRegPointers();
                    if (internalRegister1 > 7) {
                        break;
                    }
                    registers[internalRegister1] = popValue();
                    break;
                }
                case 52://pushf
                {
                    pushValue(getFlagByte());
                    break;
                }
                case 53://popf
                {
                    setFlagFromByte(popByteValue());
                    break;
                }
                case 54://nop
                {
                    //do nothing
                    break;
                }
                case 55://interrupt
                {
                    int interruptIndex = readMem8PC();
                    executeInterrupt(interruptIndex);
                }
                case 60://REG IMMEDIATE
                {
                    readRegPointers();
                    int value = readMem32PC();
                    registers[internalRegister1] = value;
                    break;
                }
                case 61://REG REG
                {
                    readRegPointers();
                    registers[internalRegister1] = registers[internalRegister2];
                    break;
                }
                case 62://MEM8 REG, REG:[REG*1]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister];
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 63://MEM8 REG, REG:[REG*2]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 2);
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 64://MEM8 REG, REG:[REG*4]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 4);
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 65://MEM8 REG, REG:[REG*1+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] + offset;
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 66://MEM8 REG, REG:[REG*2+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 67://MEM8 REG, REG:[REG*4+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4 + offset;
                    registers[internalRegister1] = memoryController.getMem8(address);
                    break;
                }
                case 68://MEM8 REG:[REG*1],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }
                case 69://MEM8 REG:[REG*2],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }
                case 70://MEM8 REG:[REG*4],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }
                case 71://MEM8 REG:[REG*1+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1 + offset;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }
                case 72://MEM8 REG:[REG*2+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }
                case 73://MEM8 REG:[REG*4+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem8(address, (byte) registers[internalRegister1]);
                    break;
                }

                case 74://MEM16 REG, REG:[REG*1]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister];
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 75://MEM16 REG, REG:[REG*2]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 2);
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 76://MEM16 REG, REG:[REG*4]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 4);
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 77://MEM16 REG, REG:[REG*1+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] + offset;
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 78://MEM16 REG, REG:[REG*2+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 79://MEM16 REG, REG:[REG*4+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4 + offset;
                    registers[internalRegister1] = memoryController.getMem16(address);
                    break;
                }
                case 80://MEM16 REG:[REG*1],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }
                case 81://MEM16 REG:[REG*2],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }
                case 82://MEM16 REG:[REG*4],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }
                case 83://MEM16 REG:[REG*1+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1 + offset;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }
                case 84://MEM16 REG:[REG*2+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }
                case 85://MEM16 REG:[REG*4+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem16(address, (short) registers[internalRegister1]);
                    break;
                }

                case 86://MEM32 REG, REG:[REG*1]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister];
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 87://MEM32 REG, REG:[REG*2]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 2);
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 88://MEM32 REG, REG:[REG*4]
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + (registers[internalIndexRegister] * 4);
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 89://MEM32 REG, REG:[REG*1+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] + offset;
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 90://MEM32 REG, REG:[REG*2+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 91://MEM32 REG, REG:[REG*4+offset32]
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4 + offset;
                    registers[internalRegister1] = memoryController.getMem32(address);
                    break;
                }
                case 92://MEM32 REG:[REG*1],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
                case 93://MEM32 REG:[REG*2],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
                case 94://MEM32 REG:[REG*4],REG
                {
                    readRegPointers();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 4;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
                case 95://MEM32 REG:[REG*1+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 1 + offset;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
                case 96://MEM32 REG:[REG*2+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
                case 97://MEM32 REG:[REG*4+offset32],REG
                {
                    readRegPointers();
                    int offset = readMem32PC();
                    int address = registers[internalRegister2] + registers[internalIndexRegister] * 2 + offset;
                    memoryController.setMem32(address, registers[internalRegister1]);
                    break;
                }
            }

            if (interruptRequest > 0) {
                int tInterruptRequest = interruptRequest;
                interruptRequest = 0;
                if (interruptEnableFlag == 1) {
                    executeInterrupt(tInterruptRequest);
                }
            }
        }
    }

    private void pushValue(int value) {
        registers[SP] += 4;
        memoryController.setMem32(registers[SP], value);
        setFlags(0, 0, 0, 0);
    }

    private void pushValue(byte value) {
        registers[SP] += 1;
        memoryController.setMem8(registers[SP], value);
        setFlags(0, 0, 0, 0);
    }

    private int popValue() {
        int value = memoryController.getMem32(registers[SP]);
        registers[SP] -= 4;
        setFlags(0, 0, 0, 0);
        return value;
    }

    private int popByteValue() {
        int value = memoryController.getMem8(registers[SP]);
        registers[SP] -= 1;
        setFlags(0, 0, 0, 0);
        return value;
    }

    private void setFlags(int n, int z, int o, int c) {
        negativeFlag = n;
        zeroFlag = z;
        overflowFlag = o;
        carryFlag = c;
    }

    private void setFlagValue(int flagIndex, int value) {
        switch (flagIndex) {
            case 0:
                zeroFlag = value;
                break;
            case 1:
                carryFlag = value;
                break;
            case 2:
                overflowFlag = value;
                break;
            case 3:
                negativeFlag = value;
                break;
            case 4:
                interruptEnableFlag = value;
                break;
        }
    }

    private void updateOC(long x, long y, long result) {
        carryFlag = (result > 0xFFFFFFFF) == false ? 0 : 1;
        overflowFlag = (~(x ^ y) & (x ^ result) & 0x80000000) == 0 ? 0 : 1;
    }

    private void updateNZ(long value) {
        negativeFlag = (value & 0x80000000) == 0 ? 0 : 1;
        zeroFlag = (value == 0) ? 1 : 0;
    }

    private void readRegPointers() {
        int regInfo = memoryController.getMem8(registers[PC]++);
        internalRegister1 = regInfo & 0b111;
        internalRegister2 = (regInfo >> 3) & 0b111;
        internalIndexRegister = (regInfo >> 6) & 0b11;
    }

    private int readMem32PC() {
        int val = memoryController.getMem32(registers[PC]);
        registers[PC] += 4;
        return val;
    }

    private int readMem8PC() {
        int val = memoryController.getMem8(registers[PC]);
        registers[PC]++;
        return val;
    }

    public void setMemoryController(MemoryController memoryController) {
        this.memoryController = memoryController;
    }

    public void reset() {
        registers[PC] = 0x1000;
        run = true;
        setFlagFromByte((byte) 0);
    }

    void setFlagFromByte(int value) {
        zeroFlag = value & 0b1;
        carryFlag = (value & 0b10) >> 1;
        overflowFlag = (value & 0b100) >> 2;
        negativeFlag = (value & 0b1000) >> 3;
        interruptEnableFlag = (value & 0b10000) >> 4;
    }

    byte getFlagByte() {
        return (byte) (zeroFlag | carryFlag << 1 | overflowFlag << 2 | negativeFlag << 3 | interruptEnableFlag << 4);
    }

    private void executeInterrupt(int interruptNumber) {
    }
}
