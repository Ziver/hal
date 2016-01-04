package se.koc.hal.struct;

import se.koc.hal.intf.HalSensor;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface TemperatureSensor extends HalSensor {

    double getTemperature();

    double getHumidity();
}
