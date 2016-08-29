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

package se.hal.plugin.tellstick.protocol;

import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.TellstickProtocol.TellstickDecodedEntry;
import se.hal.plugin.tellstick.device.NexaSelfLearning;
import se.hal.plugin.tellstick.device.NexaSelfLearningDimmer;
import se.hal.struct.devicedata.DimmerEventData;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class NexaSelfLearningTest {

    @org.junit.Test
    public void testSwitchEncode() throws Exception {
        NexaSelfLearning nexaDevice = new NexaSelfLearning();
        nexaDevice.setHouse(11_772_006);
        nexaDevice.setUnit(3);
        SwitchEventData nexaData = new SwitchEventData(true);

        byte[] expected = Converter.toBytes(new char[]{
                        84, // T
                        127, 255, 24, 0, // timings
                        130, // length

                        154, 138, 136, 170, 136, 168, 170, 138, 136, 168,
                        168, 170, 136, 170, 138, 138, 138, 138, 138, 136,
                        168, 170, 138, 136, 168, 170, 138, 136, 170, 138,
                        136, 168, 160,

                        43}); // +
        NexaSelfLearningProtocol nexaProt = new NexaSelfLearningProtocol();
        byte[] actual = nexaProt.encode(nexaDevice, nexaData).getTransmissionString()
                .getBytes(StandardCharsets.ISO_8859_1);

        System.out.println("Expected: "+Converter.toHexString(expected).toUpperCase());
        System.out.println("Actual  : "+Converter.toHexString(actual).toUpperCase());
        assertArrayEquals(expected, actual);
    }

    @org.junit.Test
    public void testDimmerEncode() throws Exception {
        NexaSelfLearningDimmer nexaDevice = new NexaSelfLearningDimmer();
        nexaDevice.setHouse(11_772_006);
        nexaDevice.setUnit(3);
        DimmerEventData nexaData = new DimmerEventData(0.5);

        byte[] expected = Converter.toBytes(new char[]{
                84, // T
                127, 255, 24, 0, // timings
                162, // length, 32 extra timings

                154, 138, 136, 170, 136, 168, 170, 138, 136, 168,
                168, 170, 136, 170, 138, 138, 138, 138, 138, 136,
                168, 170, 138, 136, 168, 170, 138,
                138, 138, 138, 136, 168,
                168, 170, 138, 138, 138, 138, 138, 138, 128, // Dimer value

                43}); // +
        NexaSelfLearningProtocol nexaProt = new NexaSelfLearningProtocol();
        byte[] actual = nexaProt.encode(nexaDevice, nexaData).getTransmissionString()
                .getBytes(StandardCharsets.ISO_8859_1);

        System.out.println("Expected: "+Converter.toHexString(expected).toUpperCase());
        System.out.println("Actual  : "+Converter.toHexString(actual).toUpperCase());
        assertArrayEquals(expected, actual);
    }


    @org.junit.Test
    public void decode_ON() throws Exception {
        TellstickDecodedEntry nexa = decode("0x2CE81990");

        assertEquals("House Code", 11772006, ((NexaSelfLearning)nexa.getDevice()).getHouse());
        assertEquals("Unit Code", 0, ((NexaSelfLearning)nexa.getDevice()).getUnit());
        assertTrue("Enabled", ((SwitchEventData)nexa.getData()).isOn());
    }

    @org.junit.Test
    public void decode_OFF() throws Exception {
        TellstickDecodedEntry nexa = decode("0x2CE81980");

        assertEquals("House Code", 11772006, ((NexaSelfLearning)nexa.getDevice()).getHouse());
        assertEquals("Unit Code", 0, ((NexaSelfLearning)nexa.getDevice()).getUnit());
        assertFalse("Enabled", ((SwitchEventData)nexa.getData()).isOn());
    }

    private TellstickDecodedEntry decode(String data){
        NexaSelfLearningProtocol nexaProt = new NexaSelfLearningProtocol();
        List<TellstickDecodedEntry> list = nexaProt.decode(Converter.hexToByte(data));
        return list.get(0);
    }
}