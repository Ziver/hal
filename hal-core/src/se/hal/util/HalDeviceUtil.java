package se.hal.util;

import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalDeviceConfig;

import java.util.List;

/**
 * A class containing utility methods for HalDevice objects.
 */
public class HalDeviceUtil {

    /**
     * Method will search a list for a device matching the given configuration object.
     *
     * @param deviceConfig   the configuration object to identify the device by.
     * @param list          the list to search through.
     * @return a HalAbstractDevice object matching the configuration object, null if no device was found.
     */
    public static <D extends HalAbstractDevice> D findDevice(HalDeviceConfig deviceConfig, List<D> list){
        for (int i=0; i<list.size(); ++i) { // Don't use foreach for concurrency reasons
            D e = list.get(i);
            if (deviceConfig.equals(e.getDeviceConfig())) {
                return e;
            }
        }
        return null;
    }
}
