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

package se.hal.plugin.assistant.google.data;

/**
 * Enum for https://developers.google.com/assistant/smarthome/traits
 */
public enum SmartHomeDeviceTrait {
    AppSelector("action.devices.traits.AppSelector"),
    ArmDisarm("action.devices.traits.ArmDisarm"),
    Brightness("action.devices.traits.Brightness"),
    CameraStream("action.devices.traits.CameraStream"),
    Channel("action.devices.traits.Channel"),
    ColorSetting("action.devices.traits.ColorSetting"),
    Cook("action.devices.traits.Cook"),
    Dispense("action.devices.traits.Dispense"),
    Dock("action.devices.traits.Dock"),
    EnergyStorage("action.devices.traits.EnergyStorage"),
    FanSpeed("action.devices.traits.FanSpeed"),
    Fill("action.devices.traits.Fill"),
    HumiditySetting("action.devices.traits.HumiditySetting"),
    InputSelector("action.devices.traits.InputSelector"),
    LightEffects("action.devices.traits.LightEffects"),
    Locator("action.devices.traits.Locator"),
    LockUnlock("action.devices.traits.LockUnlock"),
    MediaState("action.devices.traits.MediaState"),
    Modes("action.devices.traits.Modes"),
    NetworkControl("action.devices.traits.NetworkControl"),
    OnOff("action.devices.traits.OnOff"),
    OpenClose("action.devices.traits.OpenClose"),
    Reboot("action.devices.traits.Reboot"),
    Rotation("action.devices.traits.Rotation"),
    RunCycle("action.devices.traits.RunCycle"),
    SensorState("action.devices.traits.SensorState"),
    Scene("action.devices.traits.Scene"),
    SoftwareUpdate("action.devices.traits.SoftwareUpdate"),
    StartStop("action.devices.traits.StartStop"),
    StatusReport("action.devices.traits.StatusReport"),
    TemperatureControl("action.devices.traits.TemperatureControl"),
    TemperatureSetting("action.devices.traits.TemperatureSetting"),
    Timer("action.devices.traits.Timer"),
    Toggles("action.devices.traits.Toggles"),
    TransportControl("action.devices.traits.TransportControl"),
    Volume("action.devices.traits.Volume");



    private final String typeId;


    SmartHomeDeviceTrait(String typeId) {
        this.typeId = typeId;
    }


    public String getString() {
        return typeId;
    }
}
