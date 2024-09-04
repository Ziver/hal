package se.hal.plugin.mqtt.detector;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import zutil.ObjectUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TasmotaMqttDetector implements HalMqttDetector {


    @Override
    public List<HalMqttDeviceConfig> parseTopic(String topic, byte[] data) {
        if (ObjectUtil.isEmpty(topic) ||
                ! topic.startsWith("zigbee2mqtt/") ||
                topic.startsWith("zigbee2mqtt/bridge/"))
            return Collections.emptyList();

        List<HalMqttDeviceConfig> detectedDeviceConfigs = new ArrayList<>();
        return detectedDeviceConfigs;
    }

}
