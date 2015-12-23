package se.koc.hal.struct;

import se.koc.hal.intf.HalSensor;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface TemperatureSensor extends HalSensor {

    public double getTemperature();

    public double getHumidity();
}
