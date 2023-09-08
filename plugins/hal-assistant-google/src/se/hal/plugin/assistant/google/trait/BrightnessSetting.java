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
import se.hal.struct.devicedata.LevelEventData;
import zutil.converter.Converter;

import java.util.HashMap;


/**
 * https://developers.home.google.com/cloud-to-cloud/traits/brightness
 */
public class BrightnessSetting extends DeviceTrait {

    @Override
    String getId() {
        return "action.devices.traits.Brightness";
    }

    @Override
    public HashMap<String, Object> generateSyncResponse(HalDeviceConfig config) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("commandOnlyBrightness", false);
        return response;
    }

    @Override
    public HashMap<String, Object> generateQueryResponse(HalDeviceData data) {
        HashMap<String, Object> response = new HashMap<>();

        if (data instanceof LevelEventData) {
            response.put("brightness", ((LevelEventData) data).getData() * 100);
        }

        return response;
    }

    @Override
    public void execute(HalAbstractDevice device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {
        if ("action.devices.commands.BrightnessAbsolute".equals(execution.command)) {
            LevelEventData eventData = new LevelEventData((int) execution.getParams().get("brightness"), System.currentTimeMillis());

            EventControllerManager.getInstance().send((Event) device, eventData);
        } else if ("action.devices.commands.BrightnessRelative".equals(execution.command) && device.getDeviceData() != null) {
            LevelEventData currentLevel = (LevelEventData) device.getDeviceData();
            int newPercentage = currentLevel.getPercent();

            if (execution.getParams().containsKey("brightnessRelativePercent")) {
                newPercentage += (int) execution.getParams().get("brightnessRelativePercent");
            } else if (execution.getParams().containsKey("brightnessRelativeWeight")) {
                newPercentage += 10 * (int) execution.getParams().get("brightnessRelativeWeight");
            }

            LevelEventData eventData = new LevelEventData(newPercentage, System.currentTimeMillis());
            EventControllerManager.getInstance().send((Event) device, eventData);
        }
    }
}
