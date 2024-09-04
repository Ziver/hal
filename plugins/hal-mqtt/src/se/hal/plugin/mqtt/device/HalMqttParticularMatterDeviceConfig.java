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

package se.hal.plugin.mqtt.device;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventController;
import se.hal.plugin.mqtt.HalMqttController;
import se.hal.struct.devicedata.ParticulateMatterSensorData;
import zutil.ObjectUtil;
import zutil.converter.Converter;
import zutil.parser.DataNode;
import zutil.parser.DataNodePath;
import zutil.parser.json.JSONParser;

import java.nio.charset.StandardCharsets;

public class HalMqttParticularMatterDeviceConfig extends HalMqttDeviceConfig {

    public HalMqttParticularMatterDeviceConfig() {}
    public HalMqttParticularMatterDeviceConfig(String topic) {
        super(topic);
    }
    public HalMqttParticularMatterDeviceConfig(String topic, String jsonPath) {
        super(topic, jsonPath);
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
        return ParticulateMatterSensorData.class;
    }

    @Override
    public ParticulateMatterSensorData getDeviceData(byte[] data) {
        if (!ObjectUtil.isEmpty(getJsonPath())) {
            String dataStr = new String(data, StandardCharsets.UTF_8);
            DataNode json = JSONParser.read(dataStr);
            DataNode deviceDataValue = DataNodePath.search(getJsonPath(), json);

            if (deviceDataValue != null)
                return new ParticulateMatterSensorData(deviceDataValue.getDouble(), System.currentTimeMillis());
            else
                return null;
        }
        return new ParticulateMatterSensorData(Converter.toInt(data), System.currentTimeMillis());
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }
}
