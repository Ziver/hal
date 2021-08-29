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


import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.struct.devicedata.TemperatureSensorData;

import java.util.HashMap;


public class TemperatureControlTrait extends DeviceTrait {

    @Override
    String getId() {
        return "action.devices.traits.TemperatureControl";
    }

    @Override
    public HashMap<String, Object> generateSyncResponse(HalDeviceConfig config) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("temperatureRange", new HashMap<String, Object>() {{
            put("minThresholdCelsius", -20);
            put("maxThresholdCelsius", 60);
        }});
        //response.put("temperatureStepCelsius", 0.5);
        response.put("temperatureUnitForUX", "C");
        //response.put("commandOnlyTemperatureControl", false);
        response.put("queryOnlyTemperatureControl", true);
        return response;
    }

    @Override
    public HashMap<String, Object> generateQueryResponse(HalDeviceData data) {
        HashMap<String, Object> response = new HashMap<>();

        if (data instanceof TemperatureSensorData) {
            //response.put("temperatureSetpointCelsius", data.getData());
            response.put("temperatureAmbientCelsius", data.getData());
        }

        return response;
    }
}
