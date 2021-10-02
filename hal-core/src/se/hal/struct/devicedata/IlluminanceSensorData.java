package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class IlluminanceSensorData extends HalSensorData {

    private double lux;


    public IlluminanceSensorData(){}
    public IlluminanceSensorData(double lux, long timestamp){
        this.lux =  lux;
        this.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return lux + " lux";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    /**
     * @return the light intensity in lux
     */
    @Override
    public double getData() {
        return lux;
    }

    /**
     * @param   data     set the light intensity in lux
     */
    @Override
    public void setData(double data) {
        this.lux = data;
    }
}
