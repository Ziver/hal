package se.hal.plugin.mqtt.device;

import se.hal.intf.HalDeviceData;
import se.hal.struct.devicedata.OnOffEventData;
import zutil.ObjectUtil;
import zutil.parser.DataNode;
import zutil.parser.DataNodePath;
import zutil.parser.json.JSONParser;
import zutil.ui.conf.Configurator;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HalMqttOnOffEventConfig extends HalMqttEventConfig {

    @Configurator.Configurable(value = "Match value for state ON")
    private String valueOnString = "ON";
    @Configurator.Configurable(value = "Match value for state OFF")
    private String valueOffString = "OFF";


    public HalMqttOnOffEventConfig() {}
    public HalMqttOnOffEventConfig(String topic) {
        super(topic);
    }
    public HalMqttOnOffEventConfig(String topic, String jsonPath) {
        super(topic, jsonPath);
    }



    public String getValueOnString() {
        return valueOnString;
    }

    public void setValueOnString(String valueOnString) {
        this.valueOnString = valueOnString;
    }

    public String getValueOffString() {
        return valueOffString;
    }

    public void setValueOffString(String valueOffString) {
        this.valueOffString = valueOffString;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return OnOffEventData.class;
    }

    @Override
    public HalDeviceData getDeviceData(byte[] data) {
        if(ObjectUtil.isEmpty(data)) {
            return null;
        }
        String dataStr = new String(data, StandardCharsets.UTF_8);

        if (!ObjectUtil.isEmpty(getJsonPath())) {
            DataNode json = JSONParser.read(dataStr);
            DataNode deviceDataValue = DataNodePath.search(getJsonPath(), json);

            if (ObjectUtil.isEmpty(deviceDataValue)) {
                return null;
            }

            dataStr = deviceDataValue.getString().toUpperCase();
        }

        if (valueOnString.equalsIgnoreCase(dataStr)) {
            return new OnOffEventData(true, System.currentTimeMillis());
        } else if (valueOffString.equalsIgnoreCase(dataStr)) {
            return new OnOffEventData(false, System.currentTimeMillis());
        } else {
            return null;
        }
    }

    @Override
    public byte[] getMqttPublishPayload(HalDeviceData data) {
        if (data instanceof OnOffEventData) {
            if (((OnOffEventData) data).isOn()) {
                return valueOnString.getBytes(StandardCharsets.UTF_8);
            } else {
                return valueOffString.getBytes(StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}
