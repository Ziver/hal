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
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.cmd.TellstickCmd;
import se.hal.plugin.tellstick.cmd.TellstickCmdExtendedSend;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.device.NexaSelfLearning;
import se.hal.plugin.tellstick.device.NexaSelfLearningDimmer;
import se.hal.struct.devicedata.DimmerEventData;
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
 * @see <a href="https://github.com/telldus/telldus/blob/master/telldus-core/service/ProtocolNexa.cpp">ProtocolNexa.cpp Tellstick Reference</a>
 */
public class NexaSelfLearningProtocol extends TellstickProtocol {
    private static final Logger logger = LogUtil.getLogger();
    public static final String PROTOCOL = "arctech";
    public static final String MODEL = "selflearning";


    private static class NexaSLTransmissionStruct implements BinaryStruct{
        @BinaryField(index=10, length=26)
        int house = 0;

        @BinaryField(index=20, length=1)
        boolean group = false;

        @BinaryField(index = 30, length = 1)
        boolean enable = false;

        @BinaryField(index=40, length=4)
        int unit = 0;
    }
    private static class NexaSLTransmissionDimmerStruct extends NexaSLTransmissionStruct {
        @BinaryField(index = 50, length = 4)
        int dimLevel = 0;
    }



    public NexaSelfLearningProtocol() {
        super(PROTOCOL, MODEL);
    }

    @Override
    public TellstickCmd encode(HalEventConfig deviceConfig, HalEventData deviceData){
        if ( ! (deviceConfig instanceof NexaSelfLearning || deviceConfig instanceof NexaSelfLearningDimmer)){
            logger.severe("Device config is not instance of NexaSelfLearning: "+deviceConfig.getClass());
            return null;
        }
        if ( ! (deviceData instanceof SwitchEventData || deviceData instanceof DimmerEventData)){
            logger.severe("Device data is not an instance of SwitchEventData or DimmerEventData: "+deviceData.getClass());
            return null;
        }

        // Create transmission struct
        NexaSLTransmissionStruct struct;
        if (deviceData instanceof DimmerEventData) {
            struct = new NexaSLTransmissionDimmerStruct();
            struct.house = ((NexaSelfLearningDimmer) deviceConfig).getHouse();
            struct.unit = ((NexaSelfLearningDimmer) deviceConfig).getUnit();
            ((NexaSLTransmissionDimmerStruct)struct).dimLevel = (int)(deviceData.getData()*16);
        }
        else {
            struct = new NexaSLTransmissionStruct();
            struct.house = ((NexaSelfLearning) deviceConfig).getHouse();
            struct.group = ((NexaSelfLearning) deviceConfig).getGroup();
            struct.unit = ((NexaSelfLearning) deviceConfig).getUnit();
            struct.enable = ((SwitchEventData) deviceData).isOn();
        }


        // Generate transmission string
        try {
            TellstickCmdExtendedSend cmd = new TellstickCmdExtendedSend();
            cmd.setPulls0Timing(1270);
            cmd.setPulls1Timing(2550);
            cmd.setPulls2Timing(240);

            cmd.addPulls2().addPulls1(); // preamble
            byte[] data = BinaryStructOutputStream.serialize(struct);
            for (byte b : data){
                for (int i=7; i>=0; --i){
                    if (ByteUtil.getBits(b, i, 1) == 0) // 0
                        cmd.addPulls2().addPulls2().addPulls2().addPulls0(); // 0b1010_1000
                    else // 1
                        cmd.addPulls2().addPulls0().addPulls2().addPulls2(); // 0b1000_1010
                }
            }
            //cmd.addPulls2().addPulls2(); // postemble?
            return cmd;

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
/*        for (NexaSelfLearningDimmer device : TellstickSerialComm.getInstance().getRegisteredDevices(NexaSelfLearningDimmer.class)) {
            if (device.getHouse() == struct.house && device.getUnit() == struct.unit){
                NexaSLTransmissionDimmerStruct dimmerStruct = new NexaSLTransmissionDimmerStruct();
                BinaryStructInputStream.read(dimmerStruct, data);

                list.add(new TellstickDecodedEntry(
                        new NexaSelfLearningDimmer(dimmerStruct.house, dimmerStruct.unit),
                        new DimmerEventData(dimmerStruct.dimLevel)
                ));
            }
        }
        if (list.isEmpty()) { // No dimmer stored, we assume it is a switch event then...
*/
            list.add(new TellstickDecodedEntry(
                    new NexaSelfLearning(struct.house, struct.group, struct.unit),
                    new SwitchEventData(struct.enable)
            ));
/*        }*/
        return list;
    }


}
