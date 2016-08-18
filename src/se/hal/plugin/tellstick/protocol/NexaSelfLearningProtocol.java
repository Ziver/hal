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

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.device.NexaSelfLearning;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.ByteUtil;
import zutil.log.LogUtil;
import zutil.parser.binary.BinaryStruct;
import zutil.parser.binary.BinaryStructInputStream;
import zutil.parser.binary.BinaryStructOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearningProtocol extends TellstickProtocol {
    private static final Logger logger = LogUtil.getLogger();
    public static final String PROTOCOL = "arctech";
    public static final String MODEL = "selflearning";


    private static class NexaSLTransmissionStruct implements BinaryStruct{
        @BinaryField(index=1, length=26)
        int house = 0;

        @BinaryField(index=2, length=1)
        boolean group = false;

        @BinaryField(index=3, length=1)
        boolean enable = false;

        @BinaryField(index=4, length=4)
        int unit = 0;
    }


    public NexaSelfLearningProtocol() {
        super(PROTOCOL, MODEL);
    }

    @Override
    public String encode(HalEventConfig deviceConfig, HalEventData deviceData){
        if ( ! (deviceConfig instanceof NexaSelfLearning)){
            logger.severe("Device config is not instance of NexaSelfLearning: "+deviceConfig.getClass());
            return null;
        }
        if ( ! (deviceData instanceof SwitchEventData)){
            logger.severe("Device data is not instance of SwitchEventData: "+deviceData.getClass());
            return null;
        }
        NexaSLTransmissionStruct struct = new NexaSLTransmissionStruct();
        struct.house = ((NexaSelfLearning) deviceConfig).getHouse();
        struct.group = ((NexaSelfLearning) deviceConfig).getGroup();
        struct.enable = ((SwitchEventData) deviceData).isOn();
        struct.unit = ((NexaSelfLearning) deviceConfig).getUnit();

        try {
            // T[t0][t1][t2][t3][length][d1]..[dn]+
            StringBuilder enc = new StringBuilder(90); // Tellstick supports max 74 bytes
            enc.append(new char[]{'T', 127, 255, 24, 0});
            enc.append((char)0); // length

            enc.append((char)0b0000_1001); // preamble
            int length = 4;
            byte[] data = BinaryStructOutputStream.serialize(struct);
            for (byte b : data){
                for (int i=7; i>=0; --i){
                    if (ByteUtil.getBits(b, i, 1) == 0)
                        enc.append((char) 0b1010_1000); // 0b1010_1000
                    else // 1
                        enc.append((char) 0b1000_1010); // 0b1000_1010
                    length += 4;
                }
            }
            enc.append((char)0b0000_0000); // postemble
            length += 2;
            enc.setCharAt(5, (char)length); // Set calculated length

            enc.append("+");
            return enc.toString();

        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }

        return null;
    }

    @Override
    public List<TellstickDecodedEntry> decode(byte[] data){
        // Data positions
        // house  = 0xFFFFFFC0
        // group  = 0x00000020
        // method = 0x00000010
        // unit   = 0x0000000F
        //              ----------------h------------ g m --u-
        // 0x2CE81990 - 00101100_11101000_00011001_10 0 1 0000 - ON
        // 0x2CE81980 - 00101100_11101000_00011001_10 0 0 0000 - OFF

        NexaSLTransmissionStruct struct = new NexaSLTransmissionStruct();
        BinaryStructInputStream.read(struct, data);

        ArrayList<TellstickDecodedEntry> list = new ArrayList<>();
        list.add(new TellstickDecodedEntry(
                new NexaSelfLearning(struct.house, struct.group, struct.unit),
                new SwitchEventData(struct.enable)
        ));
        return list;
    }


}
