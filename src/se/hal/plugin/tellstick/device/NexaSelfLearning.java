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
import se.hal.intf.HalEventData;
import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickDevice;
import se.hal.plugin.tellstick.TellstickDeviceGroup;
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.protocol.NexaSelfLearningProtocol;
import se.hal.struct.devicedata.SwitchEventData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.parser.binary.BinaryStruct;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearning implements HalEventConfig,TellstickDevice,TellstickDeviceGroup {

    @Configurator.Configurable("House code")
    private int house = 0;

    @Configurator.Configurable("Group code")
    private boolean group = false;

    @Configurator.Configurable("Unit code")
    private int unit = 0;


    public NexaSelfLearning() { }
    public NexaSelfLearning(int house, boolean group, int unit) {
        this.house = house;
        this.group = group;
        this.unit = unit;
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



    public String toString(){
        return "house:"+house+
                ", group:"+group+
                ", unit:"+unit;
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
    public boolean equalsGroup(TellstickDeviceGroup obj) {
        if(obj instanceof NexaSelfLearning)
            return ((NexaSelfLearning) obj).house == house &&
                    (((NexaSelfLearning) obj).group || group );
        return false;
    }


    @Override
    public Class<? extends HalEventController> getEventControllerClass() {
        return TellstickSerialComm.class;
    }

    @Override
    public Class<? extends HalEventData> getEventDataClass() {
        return SwitchEventData.class;
    }

    @Override
    public String getProtocolName() { return NexaSelfLearningProtocol.PROTOCOL; }
    @Override
    public String getModelName() { return NexaSelfLearningProtocol.MODEL; }
}
