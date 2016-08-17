package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public class PowerConsumptionSensorData extends HalSensorData {

    private double wattHours;


    public void setConsumption(double wattHours){
        this.wattHours = wattHours;
    }

    /**
     * @return int representing Watt/Hour
     */
    @Override
    public double getData() {
        return wattHours;
    }
}
