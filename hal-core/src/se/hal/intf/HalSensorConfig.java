package se.hal.intf;

/**
 * Interface representing sensor type specific configuration data.
 */
public interface HalSensorConfig extends HalDeviceConfig {
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
}
