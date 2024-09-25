package se.hal.plugin.mqtt.device;

import se.hal.intf.HalDeviceData;
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

    /**
     * Generates the payload that will be added to a MQTT publish message.
     *
     * @param data  the data that should be converted to a payload.
     * @return a byte array representing the given data, null if the conversion was not possible.
     */
    public abstract byte[] getMqttPublishPayload(HalDeviceData data);

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
