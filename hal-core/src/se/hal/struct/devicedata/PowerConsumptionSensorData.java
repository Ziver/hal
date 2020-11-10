package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

public class PowerConsumptionSensorData extends HalSensorData {

    private double wattHours;



    public PowerConsumptionSensorData() { }
    public PowerConsumptionSensorData(double wattHours, long timestamp) {
        this.wattHours = wattHours;
        super.setTimestamp(timestamp);
    }


    /**
     * @return int representing Watt/Hour
     */
    @Override
    public double getData() {
        return wattHours;
    }
    @Override
    public void setData(double wattHours){
        this.wattHours = wattHours;
    }

    @Override
    public String toString(){
        return wattHours+" Wh";
    }
}
