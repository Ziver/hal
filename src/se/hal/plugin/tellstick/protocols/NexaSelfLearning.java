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

import se.hal.plugin.tellstick.TellstickGroupProtocol;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.struct.SwitchEventData;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearning extends TellstickProtocol implements SwitchEventData,TellstickGroupProtocol {

    @Configurator.Configurable("House code")
    private int house = 0;
    @Configurator.Configurable("Group code")
    private boolean group = false;
    @Configurator.Configurable("Unit code")
    private int unit = 0;

    private boolean enable = false;


    public NexaSelfLearning() {
        super("arctech", "selflearning");
    }


    public String encode(){
        // Binary 0 => "01"
        // Binary 1 => "10"
        StringBuilder enc = new StringBuilder();
        enc.append(new char[]{'T', 127, 255, 24, 1});

        enc.append((char)132);

        // House
        StringBuilder m = new StringBuilder();
        for (int i = 25; i >= 0; --i) {
            m.append( (house & (1 << i)) == 0 ? "01" : "10" );
        }
        // Group
        if (group)
            m.append("10");
        else
            m.append("01");

        // On or OFF
        if (enable)
            m.append("10");
        else
            m.append("01");

        // Unit
        for (int i = 3; i >= 0; --i) {
            m.append( (unit & (1 << i)) == 0 ? "01" : "10" );
        }

        // The number of data is odd add this to make it even
        m.append("0");
        //01011001101001011010100110010101010101101001011010 01 01 1001011010 0

        char code = 9;  // b1001, startcode
        for (int i = 0; i < m.length(); ++i) {
            code <<= 4;
            if (m.charAt(i) == '1') {
                code |= 0x08;  // b1000
            } else {
                code |= 0x0A;  // b1010
            }
            if (i % 2 == 0) {
                enc.append(code);
                code = 0x00;
            }
        }

        enc.append("+");
        return enc.toString();
    }

    public void decode(byte[] data){
        // Data positions
        // house  = 0xFFFFFFC0
        // group  = 0x00000020
        // method = 0x00000010
        // unit   = 0x0000000F
        //              ----------------h------------ g m --u-
        // 0x2CE81990 - 00101100_11101000_00011001_10 0 1 0000 - ON
        // 0x2CE81980 - 00101100_11101000_00011001_10 0 0 0000 - OFF

        house = 0;
        house |= (data[3] & 0xFF) << 18;
        house |= (data[2] & 0xFF) << 10;
        house |= (data[1] & 0xFF) << 2;
        house |= (data[0] & 0xC0) >>> 6;

        int tmpGroup = data[0] & 0x20; // >>> 5
        group = tmpGroup != 0;

        enable = (data[0] & 0x10) != 0;

        unit = data[0] & 0x0F;
    }


    public int getHouse() {
        return house;
    }
    public void setHouse(int house) {
        this.house = house;
    }
    public boolean getGroup() {
        return group;
    }
    public void setGroup(boolean group) {
        this.group = group;
    }
    public int getUnit() {
        return unit;
    }
    public void setUnit(int unit) {
        this.unit = unit;
    }

    public boolean isOn() {
        return enable;
    }
    public void turnOn() {
        enable = true;
    }
    public void turnOff() {
        enable = false;
    }



    public String toString(){
        return "protocol:arctech;model:selflearning;" +
                "house:"+house+
                ";group:"+group+
                ";unit:"+unit+
                ";method:"+enable;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof NexaSelfLearning)
            return ((NexaSelfLearning) obj).house == house &&
                    ((NexaSelfLearning) obj).group == group &&
                    ((NexaSelfLearning)obj).unit == unit;
        return false;
    }
    @Override
    public boolean equalsGroup(TellstickProtocol obj) {
        if(obj instanceof NexaSelfLearning)
            return ((NexaSelfLearning) obj).house == house &&
                    (((NexaSelfLearning) obj).group || group );
        return false;
    }
    @Override
    public void copyGroupData(TellstickGroupProtocol group) {
        if(group instanceof NexaSelfLearning)
            this.enable = ((NexaSelfLearning) group).enable;
    }

    @Override
    public double getData() {
        return (enable ? 1 : 0);
    }
}