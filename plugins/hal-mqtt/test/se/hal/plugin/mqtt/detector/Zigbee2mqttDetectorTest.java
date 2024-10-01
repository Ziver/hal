package se.hal.plugin.mqtt.detector;

import org.junit.Test;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttOnOffEventConfig;
import se.hal.plugin.mqtt.device.HalMqttParticularMatterDeviceConfig;
import se.hal.test.MockHalDeviceReportListener;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;


public class Zigbee2mqttDetectorTest {

    @Test
    public void ignoredTopics() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        List<HalMqttDeviceConfig> devices = detector.parseTopic("", new byte[]{});
        assertEquals(0, devices.size());

        devices = detector.parseTopic("invalid/topic", new byte[]{});
        assertEquals(0, devices.size());

        devices = detector.parseTopic("zigbee2mqtt/Kitchen Plant Light", "{\"power\":10.48,\"state\":\"ON\"}".getBytes(StandardCharsets.UTF_8));
        HalMqttOnOffEventConfig actualOnOff = new HalMqttOnOffEventConfig("zigbee2mqtt/Kitchen Plant Light", "$.state");
        actualOnOff.setWriteTopicName("zigbee2mqtt/Kitchen Plant Light/set");
        assertEquals(1, devices.size());
        assertEquals(actualOnOff, devices.get(0));

    }
/*
    @Test
    public void parseTemperature() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        assertEquals(
                Collections.emptyList(),
                detector.parseTopic(
                        "zigbee2mqtt/Kitchen air quality",
                        "{\"temperature\":26}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parseHumidity() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        assertEquals(
                Collections.emptyList(),
                detector.parseTopic(
                        "zigbee2mqtt/Kitchen air quality",
                        "{\"humidity\":51}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void parseParticularMatter() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        assertEquals(
                Collections.list(new HalMqttParticularMatterDeviceConfig()),
                detector.parseTopic(
                        "zigbee2mqtt/Kitchen air quality",
                        "{\"pm25\":1}".getBytes(StandardCharsets.UTF_8)));
    }
*/
}