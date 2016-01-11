package se.hal.intf;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalSensorController {

    void initialize() throws Exception;

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
    void setListener(HalSensorReportListener listener);


    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    void close();
}
