package se.koc.hal.intf;

import se.koc.hal.struct.Sensor;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalSensorController {

    /**
     * Will register a sensor type to be handled by this controller
     */
    void register(HalSensor sensor);

    /**
     * Deregisters a sensor from this controller, the controller
     * will no longer handle that type of sensor
     */
    void deregister(HalSensor sensor);

    /**
     * @return the number of registered objects
     */
    int size();

    /**
     * Set a listener that will receive all reports from the the registered Sensors
     */
    void setListener(SensorReportListener listener);

    interface SensorReportListener{
        void reportReceived(HalSensor s);
    }


    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    void close();
}
