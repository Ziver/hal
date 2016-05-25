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

package se.hal.plugin.tellstick.protocols;

import zutil.ByteUtil;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class NexaSelfLearningTest {

    @org.junit.Test
    public void testEncode() throws Exception {
        NexaSelfLearning nexa = new NexaSelfLearning();
        nexa.setHouse(11_772_006);
        nexa.setUnit(0);
        nexa.turnOn();

        byte[] expected = Converter.toBytes(new char[]{
                        84, // T
                        127, 255, 24, 0, // timings
                        134, // length

                        0xF9, // preamble
                        168, 168, 138, 168, 138, 138, 168, 168, 138, 138,
                        138, 168, 138, 168, 168, 168, 168, 168, 168, 138,
                        138, 168, 168, 138, 138, 168, 168, 138, 168, 168,
                        168, 168,
                        0x00, // postemble

                        43}); // +
        byte[] actual = nexa.encode().getBytes(StandardCharsets.ISO_8859_1);

        System.out.println("Expected: "+Converter.toHexString(expected).toUpperCase());
        System.out.println("Actual  : "+Converter.toHexString(actual).toUpperCase());
        assertArrayEquals(expected, actual);
    }


    @org.junit.Test
    public void decode_ON() throws Exception {
        NexaSelfLearning nexa = decode("0x2CE81990");

        assertEquals("House Code", 11772006, nexa.getHouse());
        assertEquals("Unit Code", 0, nexa.getUnit());
        assertTrue("Enabled", nexa.isOn());
    }

    @org.junit.Test
    public void decode_OFF() throws Exception {
        NexaSelfLearning nexa = decode("0x2CE81980");

        assertEquals("House Code", 11772006, nexa.getHouse());
        assertEquals("Unit Code", 0, nexa.getUnit());
        assertFalse("Enabled", nexa.isOn());
    }

    private NexaSelfLearning decode(String data){
        NexaSelfLearning nexa = new NexaSelfLearning();
        nexa.decode(Converter.hexToByte(data));
        return nexa;
    }
}