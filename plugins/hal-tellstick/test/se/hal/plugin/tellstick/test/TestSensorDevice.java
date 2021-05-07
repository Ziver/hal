package se.hal.plugin.tellstick.test;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickDevice;

public class TestSensorDevice implements HalSensorConfig, TellstickDevice {
    public int testData;

    @Override
    public String getProtocolName() {
        return "test-prot";
    }

    @Override
    public String getModelName() {
        return "test-model";
    }

    @Override
    public long getDataInterval() {
        return 0;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return null;
    }

    @Override
    public Class<? extends HalSensorController> getDeviceControllerClass() {
        return null;
    }

    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return testData == ((TestSensorDevice) obj).testData;
    }

}
