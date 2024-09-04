package se.hal.plugin.mqtt.detector;

import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;

import java.util.List;

/**
 * This interface defines the interface required by a MQTT Device detector.
 * The purpose of the implementing classes is to simplify creation of devices based on
 * MQTT by automatically detecting and parsing devices from reported topics.
 */
public interface HalMqttDetector {

    List<HalMqttDeviceConfig> parseTopic(String topic, byte[] data);
}
