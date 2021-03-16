package se.hal.intf;

/**
 * Interface representing a generic device configuration data.
 */
public interface HalDeviceConfig {

    Class<? extends HalAbstractController> getDeviceControllerClass();

    /**
     * @return the class that should be instantiated and used for data received from this event
     */
    Class<? extends HalDeviceData> getDeviceDataClass();

    /**
     * This method needs to be implemented.
     * NOTE: it should not compare data and timestamp, only static or unique data for the event type.
     */
    boolean equals(Object obj);

}
