package se.hal.struct.devicedata;


import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public class HumiditySensorData extends HalSensorData {

    private double humidity;


    public HumiditySensorData() { }
    public HumiditySensorData(double humidity) {
        this.humidity = humidity;
    }


    @Override
    public double getData() {
        return humidity;
    }
    @Override
    public void setData(double humidity) {
        this.humidity = humidity;
    }
}
