package se.hal.struct;

import se.hal.intf.HalSensorData;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface TemperatureSensorData extends HalSensorData {

    double getTemperature();

    double getHumidity();
}
