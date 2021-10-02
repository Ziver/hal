package se.hal.intf;

/**
 * Interface representing event type specific configuration data.
 */
public interface HalEventConfig extends HalDeviceConfig {

    /**
     * @return true if this event device is only a reporting data and do not accept writing/changing the data from Hal.
     */
    default boolean isReadOnly() {
        return false;
    }
}
