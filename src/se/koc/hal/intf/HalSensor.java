package se.koc.hal.intf;

import zutil.parser.DataNode;

/**
 * Created by Ziver on 2015-12-23.
 */
public interface HalSensor {
    enum AggregationMethod{
        SUM,
        AVERAGE
    }


    long getTimestamp();

    double getData();

    AggregationMethod getAggregationMethod();

    Class<? extends HalSensorController> getSensorController();

    /**
     * This method needs to be implemented.
     * NOTE: it should not compare data and timestamp, only static or unique data for the event type.
     */
    boolean equals(Object obj);
}
