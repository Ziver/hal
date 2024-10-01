package se.hal.plugin.mqtt.detector;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttOnOffEventConfig;
import zutil.ObjectUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * https://www.zigbee2mqtt.io/guide/usage/mqtt_topics_and_messages.html
 */
public class Zigbee2mqttDetector implements HalMqttDetector {

    @Override
    public List<HalMqttDeviceConfig> parseTopic(String topic, byte[] data) {
        if (ObjectUtil.isEmpty(topic) ||
                ! topic.startsWith("zigbee2mqtt/") ||
                topic.endsWith("/set") ||
                topic.startsWith("zigbee2mqtt/bridge/"))
            return Collections.emptyList();

        List<HalMqttDeviceConfig> detectedDeviceConfigs = new ArrayList<>();
        DataNode json = JSONParser.read(new String(data, StandardCharsets.UTF_8));

        if (json.getString("state") != null) {
            HalMqttOnOffEventConfig event = new HalMqttOnOffEventConfig(topic, "$.state");
            event.setWriteTopicName(topic + "/set/state");
            event.setValueOnString("ON");
            event.setValueOffString("OFF");
            detectedDeviceConfigs.add(event);
        }

        return detectedDeviceConfigs;
    }
}
