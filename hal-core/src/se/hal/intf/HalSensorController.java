package se.hal.intf;

/**
 * Controller interface for handling Sensor devices.
 */
public interface HalSensorController extends HalAbstractController {

    /**
     * Will register a sensor type to be handled by this controller
     */
    void register(HalSensorConfig sensorConfig);

    /**
     * Deregisters a sensor from this controller, the controller
     * will no longer handle that type of sensor
     */
    void deregister(HalSensorConfig sensorConfig);

    /**
     * Set a listener that will receive all reports from the the registered Sensors
     */
    void setListener(HalSensorReportListener listener);
}
