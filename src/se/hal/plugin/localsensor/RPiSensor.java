package se.hal.plugin.localsensor;

import se.hal.intf.HalSensorController;
import se.hal.struct.PowerConsumptionSensorData;

/**
 * Created by ezivkoc on 2016-01-14.
 */
public class RPiSensor implements PowerConsumptionSensorData {

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public double getData() {
        return 0;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }

    @Override
    public Class<? extends HalSensorController> getSensorController() {
        return RPiController.class;
    }

    public boolean equals(Object obj){
        return obj == this;
    }
}
