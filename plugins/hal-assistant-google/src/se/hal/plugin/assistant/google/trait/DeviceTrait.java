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
import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;

import java.util.HashMap;

/**
 * https://developers.google.com/assistant/smarthome/traits
 */
public abstract class DeviceTrait {

    /**
     * @return the API specific ID of this trait
     */
    abstract String getId();

    public abstract HashMap<String, Object> generateSyncResponse(HalDeviceConfig config);

    public abstract HashMap<String, Object> generateQueryResponse(HalDeviceData data);

    public void execute(HalAbstractDevice device, ExecuteRequest.Inputs.Payload.Commands.Execution execution) {}
}
