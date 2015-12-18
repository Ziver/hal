/*
 * Copyright (c) 2015 Ziver
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.koc.hal.plugin.tellstick.protocols;

import zutil.converters.Converter;

import static org.junit.Assert.*;

public class NexaSelfLearningTest {

    @org.junit.Test
    public void testEncode() throws Exception {
        NexaSelfLearning nexa = new NexaSelfLearning();
        nexa.setHouse(11772006);
        nexa.setUnit(3);
        nexa.setEnable(true);

        assertArrayEquals(
                new char[]{
                        84, 127, 255, 24, 1, 132, 154, 138, 136, 170,
                        136, 168, 170, 138, 136, 168, 168, 170, 136, 170,
                        138, 138, 138, 138, 138, 136, 168, 170, 138, 136,
                        168, 170, 138, 136, 170, 138, 136, 168, 170, 43},
                nexa.encode().toCharArray()
        );
    }


    @org.junit.Test
    public void decode_ON() throws Exception {
        NexaSelfLearning nexa = new NexaSelfLearning();
        nexa.decode(Converter.hexToByte("0x2CE81990"));

        assertEquals("House Code", 11772006, nexa.getHouse());
        assertEquals("Unit Code", 1, nexa.getUnit());
        assertTrue("Enabled", nexa.isEnabled());
    }
    @org.junit.Test
    public void decode_OFF() throws Exception {
        NexaSelfLearning nexa = new NexaSelfLearning();
        nexa.decode(Converter.hexToByte("0x2CE81980"));

        assertEquals("House Code", 11772006, nexa.getHouse());
        assertEquals("Unit Code", 1, nexa.getUnit());
        assertFalse("Enabled", nexa.isEnabled());
    }
}