package se.hal.intf;

/**
 * Listener to be called by the {@link HalSensorController} to report that sensor data has been received.
 */
public interface HalSensorReportListener {

    void reportReceived(HalSensorConfig s, HalSensorData d);

}