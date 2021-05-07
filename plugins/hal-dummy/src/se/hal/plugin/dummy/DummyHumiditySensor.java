package se.hal.plugin.dummy;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.HumiditySensorData;


public class DummyHumiditySensor implements DummyDevice, HalSensorConfig {


    @Override
    public HalDeviceData generateData() {
        return new HumiditySensorData(
                (int) (Math.random() * 100),
                System.currentTimeMillis()
        );
    }


    @Override
    public long getDataInterval() {
        return 60*1000; // 1 min
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
        return HumiditySensorData.class;
    }

    @Override
    public boolean equals(Object obj) {
        return this.equals(obj);
    }
}
