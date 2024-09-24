package se.hal.plugin.mqtt.device;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalSensorConfig;
import se.hal.plugin.mqtt.HalMqttController;
import zutil.ui.conf.Configurator;

import java.util.Objects;

public abstract class HalMqttEventConfig extends HalMqttDeviceConfig implements HalEventConfig {

    @Configurator.Configurable(value = "MQTT Write Topic Name")
    private String writeTopicName;


    public HalMqttEventConfig() {}
    public HalMqttEventConfig(String topic) {
        super(topic);
    }
    public HalMqttEventConfig(String topic, String jsonPath) {
        super(topic, jsonPath);
    }


    public String getWriteTopicName() {
        return writeTopicName;
    }

    public void setWriteTopicName(String writeTopicName) {
        this.writeTopicName = writeTopicName;
    }

    // --------------------------
    // Java Methods
    // --------------------------

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HalMqttEventConfig)) return false;
        if (!super.equals(o)) return false;

        HalMqttEventConfig that = (HalMqttEventConfig) o;
        return Objects.equals(writeTopicName, that.writeTopicName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(writeTopicName);
        return result;
    }
}
