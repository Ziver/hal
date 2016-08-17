package se.hal.intf;

/**
 * Interface representing one data report from a sensor.
 *
 * Created by Ziver on 2016-08-17.
 */
public abstract class HalSensorData {

    private long timestamp = -1;


    public long getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }


    /**
     * @return serialized sensor data converted to double that will be saved in DB.
     */
    public abstract double getData();
}
