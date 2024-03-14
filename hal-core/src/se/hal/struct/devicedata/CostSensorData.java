package se.hal.struct.devicedata;

import se.hal.intf.HalSensorData;


public class CostSensorData extends HalSensorData {

    private double cost;


    public CostSensorData(){}
    public CostSensorData(double cost, long timestamp){
        this.cost = cost;
        super.setTimestamp(timestamp);
    }


    @Override
    public String toString(){
        return cost + "";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    @Override
    public double getData() {
        return cost;
    }

    @Override
    public void setData(double cost) {
        this.cost = cost;
    }
}
