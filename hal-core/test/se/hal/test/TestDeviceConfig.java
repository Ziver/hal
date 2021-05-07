package se.hal.test;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;

/**
 * A dymmy device config class
 */
public class TestDeviceConfig implements HalSensorConfig, HalEventConfig {

    private String id;


    public TestDeviceConfig(String id) {
        this.id = id;
    }


    @Override
    public long getDataInterval() {
        return 60 * 1000; // 1 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getDeviceControllerClass() {
        return null;
    }

    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return TemperatureSensorData.class;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestDeviceConfig)
            return id.equals(((TestDeviceConfig) obj).id);
        return false;
    }
}
