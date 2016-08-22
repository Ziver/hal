package se.hal.intf;

/**
 * Interface representing one report from an event
 *
 * Created by Ziver on 2016-08-17.
 */
public abstract class HalDeviceData {

    private long timestamp = -1;


    public long getTimestamp(){
        return timestamp;
    }
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }


    /**
     * @return serialized event data converted to double that will be saved in DB.
     */
    public abstract double getData();

    public abstract void setData(double data);
}
