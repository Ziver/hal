package se.hal.intf;

/**
 * Interface representing sensor type specific configuration data.
 */
public interface HalSensorConfig {
    enum AggregationMethod{
        SUM,
        AVERAGE
    }


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
    Class<? extends HalSensorController> getSensorControllerClass();

    /**
     * @return the class that should be instantiated and used for data received from this sensor
     */
    Class<? extends HalSensorData> getSensorDataClass();

    /**
     * NOTE: it should only static or unique data for the sensor type.
     * This method is used to associate reported data with registered sensors
     */
    boolean equals(Object obj);
}
