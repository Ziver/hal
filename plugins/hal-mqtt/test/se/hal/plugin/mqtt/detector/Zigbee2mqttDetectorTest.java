package se.hal.plugin.mqtt.detector;

import org.junit.Test;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttOnOffEventConfig;
import se.hal.plugin.mqtt.device.HalMqttParticularMatterDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttPowerConsumptionDeviceConfig;
import se.hal.test.MockHalDeviceReportListener;
import zutil.converter.Converter;

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

    @Test
    public void parseState() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        List<HalMqttDeviceConfig> devices = devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen Plant Light",
                "{\"invalid_power\":10.48,\"state\":\"ON\"}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, devices.size());

        HalMqttOnOffEventConfig actualOnOff = new HalMqttOnOffEventConfig("zigbee2mqtt/Kitchen Plant Light", "$.state");
        actualOnOff.setWriteTopicName("zigbee2mqtt/Kitchen Plant Light/set/state");
        assertEquals(actualOnOff, devices.get(0));
    }

    @Test
    public void parsePower() {
        Zigbee2mqttDetector detector = new Zigbee2mqttDetector();

        List<HalMqttDeviceConfig> devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"power\": 1}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttPowerConsumptionDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.power"),
                devices.get(0));
    }
}