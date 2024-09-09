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
import se.hal.intf.HalSensorConfig;
import se.hal.plugin.mqtt.HalMqttController;
import zutil.ui.conf.Configurator;

import java.util.Objects;

public abstract class HalMqttDeviceConfig implements HalSensorConfig {

    @Configurator.Configurable(value = "MQTT Topic Name")
    private String topicName;
    @Configurator.Configurable(value = "JSON Path", description = "If the value of the topic is a JSON then this parameter can be used to specify the path to the e.g. temperature value." +
            "<br>THe parameter uses the JSON-Path syntax where it always starts with $ and object fields can be accessed by .<key> and a array element by [index]")
    private String jsonPath;


    public HalMqttDeviceConfig() {}

    /**
     * @param topicName is the topic associated to this device
     */
    public HalMqttDeviceConfig(String topicName) {
        this.topicName = topicName;
    }

    /**
     * @param topicName is the topic associated to this device.
     * @param jsonPath  indicates that the payload is  of JSON format and the data should be extracted from this path.
     */
    public HalMqttDeviceConfig(String topicName, String jsonPath) {
        this.topicName = topicName;
        this.jsonPath = jsonPath;
    }


    public String getTopicName() {
        return topicName;
    }
    public void setTopicName(String topicName) {
        this.topicName = topicName;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HalMqttDeviceConfig that = (HalMqttDeviceConfig) o;

        if (!Objects.equals(topicName, that.topicName)) return false;
        return Objects.equals(jsonPath, that.jsonPath);
    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + (jsonPath != null ? jsonPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Topic: " + topicName + ", JSON Path: " + jsonPath;
    }
}
