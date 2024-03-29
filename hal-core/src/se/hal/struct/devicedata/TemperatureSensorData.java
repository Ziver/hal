package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class TemperatureSensorData extends HalSensorData {

    private double temperature;


    public TemperatureSensorData(){}
    public TemperatureSensorData(double temperature, long timestamp){
        this.temperature = temperature;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return temperature + " C";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

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
}
