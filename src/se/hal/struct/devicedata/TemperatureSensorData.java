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
     * @param   data     the temperature to set in degrees C
     */
    public void setTemperature(double data){
        this.temperature = data;
    }

    /**
     * @return temperature in degrees C
     */
    @Override
    public double getData() {
        return temperature;
    }

}
