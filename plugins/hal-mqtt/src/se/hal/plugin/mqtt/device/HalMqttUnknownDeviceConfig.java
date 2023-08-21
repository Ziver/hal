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

/**
 * Represents a unknown device data type
 */
public class HalMqttUnknownDeviceConfig extends HalMqttDeviceConfig {


    public HalMqttUnknownDeviceConfig(String topic) {
        setTopic(topic);
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
        return null;
    }

    @Override
    public HalDeviceData getDeviceData(byte[] data) {
        return null;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }
}
