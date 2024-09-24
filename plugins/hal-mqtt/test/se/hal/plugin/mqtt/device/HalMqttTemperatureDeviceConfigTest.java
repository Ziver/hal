package se.hal.plugin.mqtt.device;

import org.junit.Test;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static se.hal.test.HalAssert.assertEqualsIgnoreTimestamp;

public class HalMqttTemperatureDeviceConfigTest {

    @Test
    public void getDeviceDataRaw() {
        HalMqttTemperatureDeviceConfig config = new HalMqttTemperatureDeviceConfig();

        assertEqualsIgnoreTimestamp(null, config.getDeviceData(null));
        assertEqualsIgnoreTimestamp(null, config.getDeviceData("".getBytes(StandardCharsets.UTF_8)));

        assertEqualsIgnoreTimestamp(new TemperatureSensorData(0, 0), config.getDeviceData(Converter.toBytes(0)));
        assertEqualsIgnoreTimestamp(new TemperatureSensorData(10, 0), config.getDeviceData(Converter.toBytes(10)));
    }
}