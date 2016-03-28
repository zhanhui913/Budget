package com.zhan.budget.Util;

import junit.framework.TestCase;

/**
 * Created by zhanyap on 2016-03-01.
 */
public class ColorsTest extends TestCase {

    public void testHex2Long() throws Exception {
        assertEquals(15L, Colors.hex2Long("F"));
        assertEquals(15L, Colors.hex2Long("0F"));
        assertEquals(255L, Colors.hex2Long("FF"));
        assertEquals(4095L, Colors.hex2Long("FFF"));
        assertEquals(65535L, Colors.hex2Long("FFFF"));
        assertEquals(1048575L, Colors.hex2Long("FFFFF"));
        assertEquals(16777215L, Colors.hex2Long("FFFFFF"));
        assertEquals(268435455L, Colors.hex2Long("FFFFFFF"));
        assertEquals(4294967295L, Colors.hex2Long("FFFFFFFF"));
    }

    public void testLong2Hex() throws Exception {
        assertEquals("FFFFFFFF", Colors.long2Hex(4294967295L));
        assertEquals("FFFFFFF", Colors.long2Hex(268435455L));
        assertEquals("FFFFFF", Colors.long2Hex(16777215L));
        assertEquals("FFFFF", Colors.long2Hex(1048575L));
        assertEquals("FFFF", Colors.long2Hex(65535L));
        assertEquals("FFF", Colors.long2Hex(4095L));
        assertEquals("FF", Colors.long2Hex(255L));
        assertEquals("F", Colors.long2Hex(15L));
    }

    public void testBoth() throws Exception{
        assertEquals("FF00F123", Colors.validateHex("123"));
    }
}