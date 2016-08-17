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

package se.hal.plugin.tellstick.device;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.plugin.tellstick.TellstickGroupProtocol;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.TellstickSerialComm;
import zutil.ByteUtil;
import zutil.parser.binary.BinaryStruct;
import zutil.parser.binary.BinaryStructInputStream;
import zutil.parser.binary.BinaryStructOutputStream;
import zutil.ui.Configurator;

import java.io.IOException;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearning implements HalEventConfig,TellstickGroupProtocol,BinaryStruct {

    @BinaryField(index=1, length=26)
    @Configurator.Configurable("House code")
    private int house = 0;

    @BinaryField(index=2, length=1)
    @Configurator.Configurable("Group code")
    private boolean group = false;

    @BinaryField(index=3, length=1)
    private boolean enable = false;

    @BinaryField(index=4, length=4)
    @Configurator.Configurable("Unit code")
    private int unit = 0;


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



    public String toString(){
        return "house:"+house+
                ", group:"+group+
                ", unit:"+unit+
                ", method:"+enable;
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
    public boolean equalsGroup(TellstickGroupProtocol obj) {
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


    public Class<? extends HalEventController> getEventController() {
        return TellstickSerialComm.class;
    }
}
