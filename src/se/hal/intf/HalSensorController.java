package se.hal.intf;

public interface HalSensorController {

    /**
     * The framework might create dummy objects so any type of
     * resource initialization should be handled in this method
     * and not in the constructor.
     */
    void initialize() throws Exception;

    /**
     * Will register a sensor type to be handled by this controller
     */
    void register(HalSensorConfig sensor);

    /**
     * Deregisters a sensor from this controller, the controller
     * will no longer handle that type of sensor
     */
    void deregister(HalSensorConfig sensor);

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
