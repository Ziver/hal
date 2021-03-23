package se.hal.plugin.dummy;

import se.hal.intf.*;
import se.hal.struct.devicedata.TemperatureSensorData;


public class DummyTemperatureSensor implements DummyDevice, HalSensorConfig {


    @Override
    public HalDeviceData generateData() {
        return new TemperatureSensorData(
                (int) (Math.random() * 30),
                System.currentTimeMillis()
        );
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
        return DummyController.class;
    }

    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return TemperatureSensorData.class;
    }

    @Override
    public boolean equals(Object obj) {
        return this.equals(obj);
    }
}
