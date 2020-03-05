package com.lox01.processor;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Dmitry
 */
public class LoxProcTest {

    @Test
    public void testFlagSetGet() {
        LoxProcessor processor=new LoxProcessor();
        processor.reset();
        Assert.assertEquals(0, processor.getFlagByte());
        processor.zeroFlag=1;
        Assert.assertEquals(0b1, processor.getFlagByte());
        processor.carryFlag=1;
        Assert.assertEquals(0b11, processor.getFlagByte());
        processor.overflowFlag=1;
        Assert.assertEquals(0b111, processor.getFlagByte());
        processor.negativeFlag=1;
        Assert.assertEquals(0b1111, processor.getFlagByte());
        processor.interruptEnableFlag=1;
        Assert.assertEquals(0b11111, processor.getFlagByte());
        
        processor.setFlagFromByte((byte)0);
        Assert.assertEquals(0b0, processor.getFlagByte());
        processor.setFlagFromByte((byte)0b1010);
        Assert.assertEquals(0b1010, processor.getFlagByte());
    }

}
