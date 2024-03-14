package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class PriceSensorData extends HalSensorData {

    private double price;


    public PriceSensorData(){}
    public PriceSensorData(double price, long timestamp){
        this.price = price;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return price + "";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    @Override
    public double getData() {
        return price;
    }

    @Override
    public void setData(double price) {
        this.price = price;
    }
}
