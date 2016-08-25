package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public class TemperatureSensorData extends HalSensorData {

    private double temperature;


    public TemperatureSensorData(){}
    public TemperatureSensorData(double temperature){
        this.temperature =  temperature;
    }
    public TemperatureSensorData(long timestamp, double temperature){
        this(temperature);
        super.setTimestamp(timestamp);
    }


    /**
     * @return temperature in degrees C
     */
    @Override
    public double getData() {
        return temperature;
    }
    /**
     * @param   temperature     the temperature to set in degrees C
     */
    @Override
    public void setData(double temperature) {
        this.temperature = temperature;
    }


    @Override
    public String toString(){
        return temperature+" \\u00b0C";
    }
}
