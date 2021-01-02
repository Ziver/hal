package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

public class LightSensorData extends HalSensorData {

    private double lux;


    public LightSensorData(){}
    public LightSensorData(double lux, long timestamp){
        this.lux =  lux;
        this.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return lux+" lux";
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
     * @param   lux     set the light intensity in lux
     */
    @Override
    public void setData(double lux) {
        this.lux = lux;
    }
}
