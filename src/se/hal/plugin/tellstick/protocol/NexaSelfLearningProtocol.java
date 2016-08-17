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
import se.hal.plugin.tellstick.TellstickGroupProtocol;
import se.hal.plugin.tellstick.TellstickProtocol;
import zutil.ByteUtil;
import zutil.parser.binary.BinaryStruct;
import zutil.parser.binary.BinaryStructInputStream;
import zutil.parser.binary.BinaryStructOutputStream;
import zutil.ui.Configurator;

import java.io.IOException;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearningProtocol extends TellstickProtocol {



    public NexaSelfLearningProtocol() {
        super("arctech", "selflearning");
    }

    @Override
    public String encode(){
        try {
            // T[t0][t1][t2][t3][length][d1]..[dn]+
            StringBuilder enc = new StringBuilder(90); // Tellstick supports max 74 bytes
            enc.append(new char[]{'T', 127, 255, 24, 0});
            enc.append((char)0); // length

            enc.append((char)0b0000_1001); // preamble
            int length = 4;
            byte[] data = BinaryStructOutputStream.serialize(this);
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
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void decode(byte[] data){
        // Data positions
        // house  = 0xFFFFFFC0
        // group  = 0x00000020
        // method = 0x00000010
        // unit   = 0x0000000F
        //              ----------------h------------ g m --u-
        // 0x2CE81990 - 00101100_11101000_00011001_10 0 1 0000 - ON
        // 0x2CE81980 - 00101100_11101000_00011001_10 0 0 0000 - OFF

        BinaryStructInputStream.read(this, data);
    }


}
