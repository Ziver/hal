package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public class LightSensorData extends HalSensorData {

    private double lux;


    public LightSensorData(){}
    public LightSensorData(double lux){
        this.lux =  lux;
    }


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
