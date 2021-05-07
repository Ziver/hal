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
import se.hal.plugin.tellstick.TellstickDevice;
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.protocol.NexaSelfLearningProtocol;
import se.hal.struct.devicedata.DimmerEventData;
import zutil.ui.conf.Configurator;

/**
 * Created by Ziver on 2015-02-18.
 */
public class NexaSelfLearningDimmer implements TellstickDevice, HalEventConfig {

    @Configurator.Configurable("House code")
    private int house = 0;

    @Configurator.Configurable("Unit code")
    private int unit = 0;


    public NexaSelfLearningDimmer() { }
    public NexaSelfLearningDimmer(int house, int unit) {
        this.house = house;
        this.unit = unit;
    }


    public int getHouse() {
        return house;
    }
    public void setHouse(int house) {
        this.house = house;
    }
    public int getUnit() {
        return unit;
    }
    public void setUnit(int unit) {
        this.unit = unit;
    }


    @Override
    public String getProtocolName() { return NexaSelfLearningProtocol.PROTOCOL; }
    @Override
    public String getModelName() { return NexaSelfLearningProtocol.MODEL; }


    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return TellstickSerialComm.class;
    }
    @Override
    public Class<? extends HalEventData> getDeviceDataClass() {
        return DimmerEventData.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NexaSelfLearningDimmer)
            return ((NexaSelfLearningDimmer) obj).house == house &&
                    ((NexaSelfLearningDimmer)obj).unit == unit;
        return false;
    }
    @Override
    public String toString() {
        return "house:" + house +
                ", unit:" + unit;
    }
}
