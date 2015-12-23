package se.koc.hal.intf;

/**
 * Created by Ziver on 2015-12-23.
 */
public interface HalSensor {
    enum AggregationMethod{
        SUM,
        AVERAGE
    }


    public AggregationMethod getAggregationMethod();

    public Class<? extends HalSensorController> getController();


}
