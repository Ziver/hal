package se.hal.plugin.mqtt.detector;

import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttHumidityDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttParticularMatterDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttTemperatureDeviceConfig;
import zutil.ObjectUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This detector will handle any MQTT topics and messages that have a generic structure.
 * Supported formats:
 * <u>
 *     <li>Topics ending with: temperature, humidity, ppm</li>
 *     <li>Topics where the payload is a JSON object containing the fields: temperature, humidity, ppm</li>
 * </u>
 */
public class GenericMqttDetector implements HalMqttDetector {

    @Override
    public List<HalMqttDeviceConfig> parseTopic(String topic, byte[] data) {
        if (ObjectUtil.isEmpty(topic))
            return Collections.emptyList();

        List<HalMqttDeviceConfig> detectedDeviceConfigs = new ArrayList<>();
        String[] topicSections = topic.split("/");

        // Check if the topic ends with specific keywords

        if (topicSections.length > 1) {
            String keyword = topicSections[topicSections.length - 1];
            HalMqttDeviceConfig config = null;

            switch (keyword) {
                case "temperature":
                    config = new HalMqttTemperatureDeviceConfig(topic);
                    break;
                case "humidity":
                    config = new HalMqttHumidityDeviceConfig(topic);
                    break;
                case "pm25":
                    config = new HalMqttParticularMatterDeviceConfig(topic);
                    break;
            }

            if (config != null) {
                detectedDeviceConfigs.add(config);
                return null;
            }
        }

        // Check if the payload is in JSON format

        DataNode jsonPayload = JSONParser.read(new String(data, StandardCharsets.UTF_8));
        if (jsonPayload != null) {
            if (jsonPayload.get("temperature") != null) {
                HalMqttDeviceConfig config = new HalMqttTemperatureDeviceConfig(topic, "$.temperature");
                detectedDeviceConfigs.add(config);
            }
            if (jsonPayload.get("humidity") != null) {
                HalMqttDeviceConfig config = new HalMqttHumidityDeviceConfig(topic, "$.humidity");
                detectedDeviceConfigs.add(config);
            }
            if (jsonPayload.get("pm25") != null) {
                HalMqttDeviceConfig config = new HalMqttParticularMatterDeviceConfig(topic, "$.pm25");
                detectedDeviceConfigs.add(config);
            }
        }

        return detectedDeviceConfigs;
    }
}
