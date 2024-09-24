package se.hal.plugin.mqtt.device;

import se.hal.intf.HalEventController;
import se.hal.intf.HalSensorConfig;
import se.hal.plugin.mqtt.HalMqttController;

public abstract class HalMqttSensorConfig extends HalMqttDeviceConfig implements HalSensorConfig {

    public HalMqttSensorConfig() {}
    public HalMqttSensorConfig(String topic) {
        super(topic);
    }
    public HalMqttSensorConfig(String topic, String jsonPath) {
        super(topic, jsonPath);
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return HalMqttController.class;
    }
}
