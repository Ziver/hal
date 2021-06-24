package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class PressureSensorData extends HalSensorData {

    private double pressure;


    public PressureSensorData(){}
    public PressureSensorData(double pressure, long timestamp){
        super.setTimestamp(timestamp);
        this.pressure = pressure;
    }


    @Override
    public String toString(){
        return pressure + " hPa";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    /**
     * @return pressure in degrees hPa
     */
    @Override
    public double getData() {
        return pressure;
    }

    /**
     * @param   pressure     the temperature to set in degrees hPa
     */
    @Override
    public void setData(double pressure) {
        this.pressure = pressure;
    }
}
