package se.hal.plugin.raspberry;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.ui.Configurator;

public class RPiTemperatureSensor implements HalSensorConfig {

    @Configurator.Configurable("1-Wire Address")
    private String w1Address;


    public RPiTemperatureSensor() { }
    public RPiTemperatureSensor(String w1Address) {
        this.w1Address = w1Address;
    }


    public String get1WAddress() {
        return w1Address;
    }


    @Override
    public long getDataInterval() {
        return 10*60*1000; // 10 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getDeviceControllerClass() {
        return RPiController.class;
    }

    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return TemperatureSensorData.class;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof RPiTemperatureSensor && w1Address != null)
            return this.get1WAddress().equals(((RPiTemperatureSensor) obj).w1Address);
        return false;
    }
}
