/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
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

package se.hal.plugin.assistant.google.trait;

import se.hal.intf.HalAbstractDevice;
import se.hal.struct.Sensor;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * A factory class that will associate traits to Hal devices
 */
public class DeviceTraitFactory {
    private static final Logger logger = LogUtil.getLogger();

    private DeviceTraitFactory() {}


    /**
     * @param device    the device to get supported DeviceTraits for.
     * @return an array of DeviceTrait objects that are able to handle the given device type or empty array if there is no supported DeviceTrait available.
     */
    public static DeviceTrait[] getTraits(HalAbstractDevice device) {
        if (device == null || device.getDeviceConfig() == null)
            return new DeviceTrait[0];

        switch (device.getDeviceConfig().getDeviceDataClass().getName()) {
            case "se.hal.struct.devicedata.DimmerEventData":
            case "se.hal.struct.devicedata.OnOffEventData":
                return new DeviceTrait[]{new OnOffTrait()};

            case "se.hal.struct.devicedata.OpenClosedEventData":
                return new DeviceTrait[]{new OpenCloseTrait()};

            case "se.hal.struct.devicedata.PowerConsumptionSensorData":
            case "se.hal.struct.devicedata.LightSensorData":
                return new DeviceTrait[]{new SensorStateTrait()};

            case "se.hal.struct.devicedata.HumiditySensorData":
                return new DeviceTrait[]{new SensorStateTrait(), new HumiditySettingTrait()};

            case "se.hal.struct.devicedata.TemperatureSensorData":
                return new DeviceTrait[]{new SensorStateTrait(), new TemperatureControlTrait()};

            default:
                logger.warning("Unknown device data class (" + device.getDeviceConfig().getDeviceDataClass() + ") provided by device config: " + device.getDeviceConfig().getClass());
                return new DeviceTrait[0];
        }
    }

    /**
     * @param traits
     * @return a list integers of trait IDs matching the traits given.
     */
    public static List<String> getTraitIds(DeviceTrait[] traits) {
        List<String> list = new ArrayList<>(traits.length);

        for (DeviceTrait trait : traits) {
            list.add(trait.getId());
        }

        return list;
    }
}
