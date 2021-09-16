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
    default long getDataInterval() {
        return 60 * 60 * 1000; // 1 hour
    }

    /**
     * @return which aggregation method that should be used to aggregate the reported data.
     */
    AggregationMethod getAggregationMethod();
}
