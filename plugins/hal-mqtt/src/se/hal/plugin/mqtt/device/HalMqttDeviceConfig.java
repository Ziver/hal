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
import se.hal.intf.HalEventData;
import se.hal.intf.HalSensorConfig;
import se.hal.plugin.mqtt.HalMqttController;
import zutil.ui.conf.Configurator;

public abstract class HalMqttDeviceConfig implements HalSensorConfig {
    @Configurator.Configurable(value = "MQTT Topic")
    private String topic;
    @Configurator.Configurable(value = "JSON Path", description = "If the value of the topic is a JSON then this parameter can be used to specify the path to the e.g. temperature value." +
            "<br>THe parameter uses the JSON-Path syntax where it always starts with $ and object fields can be accessed by .<key> and a array element by [index]")
    private String jsonPath;


    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getJsonPath() {
        return jsonPath;
    }
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return HalMqttController.class;
    }

    /**
     * @param data the data published to the MQTT topic.
     * @return a new data object instance containing the device data based on the input data.
     */
    public abstract HalDeviceData getDeviceData(byte[] data);

    // --------------------------
    // Java Methods
    // --------------------------

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HalMqttDeviceConfig)
            return topic.equals(((HalMqttDeviceConfig) obj).topic) && jsonPath.equals(((HalMqttDeviceConfig) obj).jsonPath);
        return false;
    }

    @Override
    public String toString() {
        return "Topic: " + topic + ", JSON Path: " + jsonPath;
    }
}
