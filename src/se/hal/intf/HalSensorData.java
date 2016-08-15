package se.hal.intf;

/**
 * Created by Ziver on 2015-12-23.
 */
public interface HalSensorData {
    enum AggregationMethod{
        SUM,
        AVERAGE
    }


    long getTimestamp();

    double getData();


    /**
     * @return the intended data reporting interval in milliseconds.
     */
    long getDataInterval();

    /**
     * @return which aggregation method that should be used to aggregate the reported data.
     */
    AggregationMethod getAggregationMethod();


    /**
     * @return the Controller class where SensorData should be registered on
     */
    Class<? extends HalSensorController> getSensorController();

    /**
     * NOTE: it should only static or unique data for the sensor type.
     * This method is used to associate reported data with registered sensors
     */
    boolean equals(Object obj);
}
