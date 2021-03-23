package se.hal.intf;

/**
 * Interface representing a generic device configuration data.
 */
public interface HalDeviceConfig {

    /**
     * @return the controller class that is responsible to track this configuration
     */
    Class<? extends HalAbstractController> getDeviceControllerClass();

    /**
     * @return the class that should be instantiated and used for data received from this event
     */
    Class<? extends HalDeviceData> getDeviceDataClass();

    /**
     * This method is required to be implemented.
     * This method compares two configuration objects static or unique configuration. it should not compare data and timestamp type dynamic values.
     *
     * @param obj   is the target object to compare to.
     * @return true if the configuration of the two objects are same, false if the objects are not of same type or configuration does not match.
     */
    boolean equals(Object obj);
}
