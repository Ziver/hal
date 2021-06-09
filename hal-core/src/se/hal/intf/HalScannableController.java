package se.hal.intf;

/**
 * Controllers that implement this interface support manual scanning of devices.
 */
public interface HalScannableController {

    /**
     * Calling this method will start the scanning or pairing
     * mode of the controller to find new devices.
     */
    void startScan();

    /**
     * @return a boolean indication if a scan is still ongoing.
     */
    boolean isScanning();

    /**
     * Function that can be used by template to identify a scannable controller.
     *
     * @return always true.
     */
    default boolean isScannable() {
        return true;
    }
}
