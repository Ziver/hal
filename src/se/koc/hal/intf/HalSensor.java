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



    AggregationMethod getAggregationMethod();

    Class<? extends HalSensorController> getController();


}
