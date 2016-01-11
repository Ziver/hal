package se.hal.struct;

import se.hal.intf.HalSensor;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface TemperatureSensor extends HalSensor {

    double getTemperature();

    double getHumidity();
}
