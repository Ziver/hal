package se.hal.struct.devicedata;


import se.hal.intf.HalSensorData;

public class HumiditySensorData extends HalSensorData {

    private double humidity;


    public HumiditySensorData() { }
    public HumiditySensorData(double humidity, long timestamp) {
        this.humidity = humidity;
        this.setTimestamp(timestamp);
    }


    @Override
    public double getData() {
        return humidity;
    }
    @Override
    public void setData(double humidity) {
        this.humidity = humidity;
    }

    @Override
    public String toString(){
        return humidity+"%";
    }
}
