package se.hal.plugin.mqtt.device;

import org.junit.Test;
import se.hal.struct.devicedata.OnOffEventData;

import java.nio.charset.StandardCharsets;

import static se.hal.test.HalAssert.assertEqualsIgnoreTimestamp;

public class HalMqttOnOffEventConfigTest {
    private static final OnOffEventData ON_EVENT = new OnOffEventData(true, 0);
    private static final OnOffEventData OFF_EVENT = new OnOffEventData(false, 0);

    @Test
    public void getDeviceDataRaw() {
        HalMqttOnOffEventConfig config = new HalMqttOnOffEventConfig();

        assertEqualsIgnoreTimestamp(null, config.getDeviceData(null));
        assertEqualsIgnoreTimestamp(null, config.getDeviceData("".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(null, config.getDeviceData("unknown".getBytes(StandardCharsets.UTF_8)));

        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("OFF".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("off".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("ON".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("on".getBytes(StandardCharsets.UTF_8)));

        config.setValueOnString("online");
        config.setValueOffString("offline");

        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("OFFLINE".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("offline".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("ONLINE".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("online".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void getDeviceDataJson() {
        HalMqttOnOffEventConfig config = new HalMqttOnOffEventConfig();
        config.setJsonPath("$.state");

        assertEqualsIgnoreTimestamp(null, config.getDeviceData("{\"power\":10.48,\"state\":\"\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(null, config.getDeviceData("{\"power\":10.48,\"state\":\"unknown\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"OFF\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"off\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"ON\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"on\"}".getBytes(StandardCharsets.UTF_8)));

        config.setValueOnString("online");
        config.setValueOffString("offline");
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"OFFLINE\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(OFF_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"offline\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"ONLINE\"}".getBytes(StandardCharsets.UTF_8)));
        assertEqualsIgnoreTimestamp(ON_EVENT, config.getDeviceData("{\"power\":10.48,\"state\":\"online\"}".getBytes(StandardCharsets.UTF_8)));

    }
}