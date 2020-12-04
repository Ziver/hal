package se.hal.plugin.dummy;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;


public class DummyTemperatureSensor implements HalSensorConfig {


    public DummyTemperatureSensor() { }


    @Override
    public long getDataInterval() {
        return 60*1000; // 1 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getSensorControllerClass() {
        return DummyController.class;
    }

    @Override
    public Class<? extends HalSensorData> getSensorDataClass() {
        return TemperatureSensorData.class;
    }

}
