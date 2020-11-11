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
 * Enum for https://developers.google.com/assistant/smarthome/types
 */
public enum SmartHomeDeviceType {
    AC_UNIT("action.devices.types.AC_UNIT"),
    AIRCOOLER("action.devices.types.AIRCOOLER"),
    AIRFRESHENER("action.devices.types.AIRFRESHENER"),
    AIRPURIFIER("action.devices.types.AIRPURIFIER"),
    AUDIO_VIDEO_RECEIVER("action.devices.types.AUDIO_VIDEO_RECEIVER"),
    AWNING("action.devices.types.AWNING"),
    BATHTUB("action.devices.types.BATHTUB"),
    BED("action.devices.types.BED"),
    BLENDER("action.devices.types.BLENDER"),
    BLINDS("action.devices.types.BLINDS"),
    BOILER("action.devices.types.BOILER"),
    CAMERA("action.devices.types.CAMERA"),
    CHARGER("action.devices.types.CHARGER"),
    CLOSET("action.devices.types.CLOSET"),
    COFFEE_MAKER("action.devices.types.COFFEE_MAKER"),
    COOKTOP("action.devices.types.COOKTOP"),
    CURTAIN("action.devices.types.CURTAIN"),
    DEHUMIDIFIER("action.devices.types.DEHUMIDIFIER"),
    DEHYDRATOR("action.devices.types.DEHYDRATOR"),
    DISHWASHERv("action.devices.types.DISHWASHER"),
    DOOR("action.devices.types.DOOR"),
    DRAWER("action.devices.types.DRAWER"),
    DRYER("action.devices.types.DRYER"),
    FAN("action.devices.types.FAN"),
    FAUCET("action.devices.types.FAUCET"),
    FIREPLACE("action.devices.types.FIREPLACE"),
    FREEZER("action.devices.types.FREEZER"),
    FRYER("action.devices.types.FRYER"),
    GARAGE("action.devices.types.GARAGE"),
    GATE("action.devices.types.GATE"),
    GRILL("action.devices.types.GRILL"),
    HEATER("action.devices.types.HEATER"),
    HOOD("action.devices.types.HOOD"),
    HUMIDIFIER("action.devices.types.HUMIDIFIER"),
    KETTLE("action.devices.types.KETTLE"),
    LIGHT("action.devices.types.LIGHT"),
    LOCK("action.devices.types.LOCK"),
    REMOTECONTROL("action.devices.types.REMOTECONTROL"),
    MOP("action.devices.types.MOP"),
    MOWER("action.devices.types.MOWER"),
    MICROWAVE("action.devices.types.MICROWAVE"),
    MULTICOOKER("action.devices.types.MULTICOOKER"),
    NETWORK("action.devices.types.NETWORK"),
    OUTLET("action.devices.types.OUTLET"),
    OVEN("action.devices.types.OVEN"),
    PERGOLA("action.devices.types.PERGOLA"),
    PETFEEDER("action.devices.types.PETFEEDER"),
    PRESSURECOOKER("action.devices.types.PRESSURECOOKER"),
    RADIATOR("action.devices.types.RADIATOR"),
    REFRIGERATOR("action.devices.types.REFRIGERATOR"),
    ROUTER("action.devices.types.ROUTER"),
    SCENE("action.devices.types.SCENE"),
    SENSOR("action.devices.types.SENSOR"),
    SECURITYSYSTEM("action.devices.types.SECURITYSYSTEM"),
    SETTOP("action.devices.types.SETTOP"),
    SHUTTER("action.devices.types.SHUTTER"),
    SHOWER("action.devices.types.SHOWER"),
    SMOKE_DETECTOR("action.devices.types.SMOKE_DETECTOR"),
    SOUSVIDE("action.devices.types.SOUSVIDE"),
    SPEAKER("action.devices.types.SPEAKER"),
    STREAMING_BOX("action.devices.types.STREAMING_BOX"),
    STREAMING_STICK("action.devices.types.STREAMING_STICK"),
    STREAMING_SOUNDBAR("action.devices.types.STREAMING_SOUNDBAR"),
    SOUNDBAR("action.devices.types.SOUNDBAR"),
    SPRINKLER("action.devices.types.SPRINKLER"),
    STANDMIXER("action.devices.types.STANDMIXER"),
    SWITCH("action.devices.types.SWITCH"),
    TV("action.devices.types.TV"),
    THERMOSTAT("action.devices.types.THERMOSTAT"),
    VACUUM("action.devices.types.VACUUM"),
    VALVE("action.devices.types.VALVE"),
    WASHER("action.devices.types.WASHER"),
    WATERHEATER("action.devices.types.WATERHEATER"),
    WATERPURIFIER("action.devices.types.WATERPURIFIER"),
    WATERSOFTENER("action.devices.types.WATERSOFTENER"),
    WINDOW("action.devices.types.WINDOW"),
    YOGURTMAKER("action.devices.types.YOGURTMAKER");


    private final String typeId;


    private SmartHomeDeviceType(String typeId) {
        this.typeId = typeId;
    }


    public String getString() {
        return typeId;
    }
}
