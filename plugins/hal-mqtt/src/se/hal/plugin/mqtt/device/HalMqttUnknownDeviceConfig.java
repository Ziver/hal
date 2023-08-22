/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Ziver Koc
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

package se.hal.plugin.mqtt.device;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventController;
import se.hal.plugin.mqtt.HalMqttController;
import se.hal.struct.devicedata.TemperatureSensorData;

import java.nio.charset.StandardCharsets;

/**
 * Represents an unknown device data type
 */
public class HalMqttUnknownDeviceConfig extends HalMqttDeviceConfig {

    /** Save data so it can be provided to user for analysis, the data will not be used in any other way */
    transient String data;


    public HalMqttUnknownDeviceConfig(String topic, byte[] data) {
        setTopic(topic);
        this.data = new String(data, StandardCharsets.UTF_8);
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return HalMqttController.class;
    }

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return TemperatureSensorData.class; // Just return any data class just so we do not get so many error logs
    }

    @Override
    public HalDeviceData getDeviceData(byte[] data) {
        return null;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public String toString() {
        return "Topic: " + getTopic() + ", Data: " + data;
    }
}
