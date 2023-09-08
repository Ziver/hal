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


import com.google.actions.api.smarthome.ExecuteRequest;
import se.hal.EventControllerManager;
import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.struct.Event;
import se.hal.struct.devicedata.ColorEventData;
import se.hal.struct.devicedata.OnOffEventData;
import zutil.converter.Converter;

import java.util.HashMap;


/**
 * https://developers.home.google.com/cloud-to-cloud/traits/colorsetting
 */
public class ColorSetting extends DeviceTrait {

    @Override
    String getId() {
        return "action.devices.traits.ColorSetting";
    }

    @Override
    public HashMap<String, Object> generateSyncResponse(HalDeviceConfig config) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("commandOnlyColorSetting", false);
        response.put("colorModel", "rgb"); // rgb or hsv
        //response.put("colorTemperatureRange", new HashMap<String, Object>() {{
        //    put("temperatureMinK", 2000);
        //    put("temperatureMaxK", 9000);
        //}});
        return response;
    }

    @Override
    public HashMap<String, Object> generateQueryResponse(HalDeviceData data) {
        HashMap<String, Object> colorState = new HashMap<>();

        //colorState.put("temperatureK", data.getData());

        if (data instanceof ColorEventData) {
            colorState.put("spectrumRgb", ((ColorEventData) data).getData());
        }

        //colorState.put("spectrumHsv", new HashMap<String, Object>() {{
        //    put("hue", ((ColorEventData) data).getHex());
        //    put("saturation", ((ColorEventData) data).getSaturation());
        //    put("value", ((ColorEventData) data).getLightness());
        //}});

        HashMap<String, Object> response = new HashMap<>();
        response.put("color", colorState);
        return response;
    }

    @Override
    public void execute(HalAbstractDevice device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {
        if ("action.devices.commands.ColorAbsolute".equals(execution.command)) {
            byte[] rgb = Converter.toBytes((int) execution.getParams().get("spectrumRGB"));
            ColorEventData eventData = new ColorEventData(rgb[2], rgb[1], rgb[0], System.currentTimeMillis());

            EventControllerManager.getInstance().send((Event) device, eventData);
        }
    }
}
