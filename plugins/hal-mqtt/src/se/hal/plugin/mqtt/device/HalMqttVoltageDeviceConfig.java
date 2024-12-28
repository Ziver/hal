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
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import se.hal.struct.devicedata.VoltageSensorData;
import zutil.ObjectUtil;
import zutil.converter.Converter;
import zutil.parser.DataNode;
import zutil.parser.DataNodePath;
import zutil.parser.json.JSONParser;

import java.nio.charset.StandardCharsets;

public class HalMqttVoltageDeviceConfig extends HalMqttSensorConfig {

    public HalMqttVoltageDeviceConfig() {}
    public HalMqttVoltageDeviceConfig(String topic) {
        super(topic);
    }
    public HalMqttVoltageDeviceConfig(String topic, String jsonPath) {
        super(topic, jsonPath);
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return VoltageSensorData.class;
    }

    @Override
    public VoltageSensorData getDeviceData(byte[] data) {
        if(ObjectUtil.isEmpty(data)) {
            return null;
        }

        if (!ObjectUtil.isEmpty(getJsonPath())) {
            String dataStr = new String(data, StandardCharsets.UTF_8);
            DataNode json = JSONParser.read(dataStr);
            DataNode deviceDataValue = DataNodePath.search(getJsonPath(), json);

            if (deviceDataValue != null) {
                return new VoltageSensorData(deviceDataValue.getDouble(), System.currentTimeMillis());
            } else {
                return null;
            }
        }
        return new VoltageSensorData(Converter.toInt(data), System.currentTimeMillis());
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }
}
