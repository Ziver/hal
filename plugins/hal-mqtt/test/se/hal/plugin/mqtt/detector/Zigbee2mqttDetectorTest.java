package se.hal.plugin.mqtt.detector;

import org.junit.Test;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
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