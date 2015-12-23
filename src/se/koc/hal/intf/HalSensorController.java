package se.koc.hal.intf;

import se.koc.hal.struct.Sensor;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalSensorController {

    /**
     * Will register a sensor type to be handled by this controller
     */
    public void register(Sensor sensor);

    /**
     * Deregisters a sensor from this controller, the controller
     * will no longer handle that type of sensor
     */
    public void deregister(Sensor sensor);

    /**
     * @return the number of registered objects
     */
    public int size();

    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    public void close();
}
