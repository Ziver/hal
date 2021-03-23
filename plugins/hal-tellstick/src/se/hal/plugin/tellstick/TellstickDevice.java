package se.hal.plugin.tellstick;

import se.hal.intf.HalDeviceConfig;

/**
 * This interface represents a device configuration and links it to a protocol.
 *
 * Created by Ziver on 2016-08-18.
 */
public interface TellstickDevice extends HalDeviceConfig {

    String getProtocolName(); // TODO: could be implemented in a better way
    String getModelName();    // TODO: could be implemented in a better way
}
