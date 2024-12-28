package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class VoltageSensorData extends HalSensorData {

    private double voltage;


    public VoltageSensorData() { }
    public VoltageSensorData(double voltage, long timestamp) {
        this.voltage = voltage;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return voltage+" V";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    /**
     * @return number representing Volts measured
     */
    @Override
    public double getData() {
        return voltage;
    }

    @Override
    public void setData(double voltage){
        this.voltage = voltage;
    }
}
