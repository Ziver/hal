package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class ParticulateMatterSensorData extends HalSensorData {

    private double particulateMatter;


    public ParticulateMatterSensorData(){}
    public ParticulateMatterSensorData(double particulateMatter, long timestamp){
        this.particulateMatter = particulateMatter;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return particulateMatter + " µg/m3";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    /**
     * @return the particulate matter
     */
    @Override
    public double getData() {
        return particulateMatter;
    }

    /**
     * @param   particulateMatter     the particulate matter to set
     */
    @Override
    public void setData(double particulateMatter) {
        this.particulateMatter = particulateMatter;
    }
}
