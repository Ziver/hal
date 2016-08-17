package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public class TemperatureSensorData extends HalSensorData {

    private double temperature;


    /**
     * @param   data     the temperature to set in degrees C
     */
    public void setData(double data){
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
