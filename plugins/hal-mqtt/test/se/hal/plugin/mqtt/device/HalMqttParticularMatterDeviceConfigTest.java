package se.hal.plugin.mqtt.device;

import org.junit.Test;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.ParticulateMatterSensorData;
import zutil.converter.Converter;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static se.hal.test.HalAssert.assertEqualsIgnoreTimestamp;

public class HalMqttParticularMatterDeviceConfigTest {

    @Test
    public void getDeviceDataRaw() {
        HalMqttParticularMatterDeviceConfig config = new HalMqttParticularMatterDeviceConfig();

        assertEqualsIgnoreTimestamp(null, config.getDeviceData(null));
        assertEqualsIgnoreTimestamp(null, config.getDeviceData("".getBytes(StandardCharsets.UTF_8)));

        assertEqualsIgnoreTimestamp(new ParticulateMatterSensorData(0, 0), config.getDeviceData(Converter.toBytes(0)));
        assertEqualsIgnoreTimestamp(new ParticulateMatterSensorData(10, 0), config.getDeviceData(Converter.toBytes(10)));
    }
}