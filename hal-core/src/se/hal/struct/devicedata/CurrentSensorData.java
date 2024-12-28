package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class CurrentSensorData extends HalSensorData {

    private double current;


    public CurrentSensorData() { }
    public CurrentSensorData(double current, long timestamp) {
        this.current = current;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        if (current < 0.1)
            return current / 100 + " mA";
        return current + " A";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    /**
     * @return number representing Volts measured
     */
    @Override
    public double getData() {
        return current;
    }

    @Override
    public void setData(double current){
        this.current = current;
    }
}
