package se.hal.plugin.mqtt.detector;

import org.junit.Test;
import se.hal.plugin.mqtt.device.HalMqttHumidityDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttParticularMatterDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttTemperatureDeviceConfig;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.ParticulateMatterSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;
import se.hal.test.MockHalDeviceReportListener;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;


public class GenericMqttDetectorTest {

    @Test
    public void ignoredTopics() {
        MockHalDeviceReportListener listener = new MockHalDeviceReportListener();
        GenericMqttDetector detector = new GenericMqttDetector();
        detector.addListener(listener);


        listener.reset();
        detector.parseTopic("", new byte[]{});
        assertEquals(0, listener.getNumberOfReports());

        listener.reset();
        detector.parseTopic("invalid/topic", new byte[]{});
        assertEquals(0, listener.getNumberOfReports());
    }

    @Test
    public void parseTemperature() {
        MockHalDeviceReportListener listener = new MockHalDeviceReportListener();
        GenericMqttDetector detector = new GenericMqttDetector();
        detector.addListener(listener);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/temperature",
                Converter.toBytes(26));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttTemperatureDeviceConfig("zigbee2mqtt/Kitchen air quality/temperature"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new TemperatureSensorData(26, 0),
                listener.getReport(0).data);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"temperature\": 26}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttTemperatureDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.temperature"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new TemperatureSensorData(26, 0),
                listener.getReport(0).data);
    }

    @Test
    public void parseHumidity() {
        MockHalDeviceReportListener listener = new MockHalDeviceReportListener();
        GenericMqttDetector detector = new GenericMqttDetector();
        detector.addListener(listener);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/humidity",
                Converter.toBytes(51));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttHumidityDeviceConfig("zigbee2mqtt/Kitchen air quality/humidity"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new HumiditySensorData(51, 0),
                listener.getReport(0).data);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"humidity\": 51}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttHumidityDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.humidity"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new HumiditySensorData(51, 0),
                listener.getReport(0).data);
    }

    @Test
    public void parseParticularMatter() {
        MockHalDeviceReportListener listener = new MockHalDeviceReportListener();
        GenericMqttDetector detector = new GenericMqttDetector();
        detector.addListener(listener);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality/pm25",
                Converter.toBytes(1));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttParticularMatterDeviceConfig("zigbee2mqtt/Kitchen air quality/pm25"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new ParticulateMatterSensorData(1, 0),
                listener.getReport(0).data);


        listener.reset();
        detector.parseTopic(
                "zigbee2mqtt/Kitchen air quality",
                "{\"pm25\": 1}".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, listener.getNumberOfReports());
        assertEquals(
                new HalMqttParticularMatterDeviceConfig("zigbee2mqtt/Kitchen air quality", "$.pm25"),
                listener.getReport(0).config);
        listener.getReport(0).data.setTimestamp(0);
        assertEquals(
                new ParticulateMatterSensorData(1, 0),
                listener.getReport(0).data);
    }

}