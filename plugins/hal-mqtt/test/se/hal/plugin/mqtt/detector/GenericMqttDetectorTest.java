package se.hal.plugin.mqtt.detector;

import org.junit.Test;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttHumidityDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttParticularMatterDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttTemperatureDeviceConfig;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.ParticulateMatterSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;
import se.hal.test.MockHalDeviceReportListener;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;


public class GenericMqttDetectorTest {

    @Test
    public void ignoredTopics() {
        GenericMqttDetector detector = new GenericMqttDetector();

        assertEquals(0, detector.parseTopic("", new byte[]{}).size());
        assertEquals(0, detector.parseTopic("invalid/topic", new byte[]{}).size());
    }

    @Test
    public void parseTemperature() {
        GenericMqttDetector detector = new GenericMqttDetector();

        List<HalMqttDeviceConfig> devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/temperature",
                Converter.toBytes(26));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttTemperatureDeviceConfig("zigbee2mqtt/Kitchen air quality/temperature"),
                devices.get(0));

        devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"temperature\": 26}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttTemperatureDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.temperature"),
                devices.get(0));
    }

    @Test
    public void parseHumidity() {
        GenericMqttDetector detector = new GenericMqttDetector();

        List<HalMqttDeviceConfig> devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/humidity",
                Converter.toBytes(51));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttHumidityDeviceConfig("zigbee2mqtt/Kitchen air quality/humidity"),
                devices.get(0));

        devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"humidity\": 51}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttHumidityDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.humidity"),
                devices.get(0));
    }

    @Test
    public void parseParticularMatter() {
        GenericMqttDetector detector = new GenericMqttDetector();

        List<HalMqttDeviceConfig> devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/pm25",
                Converter.toBytes(1));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttParticularMatterDeviceConfig("zigbee2mqtt/Kitchen air quality/pm25"),
                devices.get(0));

        devices = detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"pm25\": 1}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, devices.size());
        assertEquals(
                new HalMqttParticularMatterDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.pm25"),
                devices.get(0));
    }

}