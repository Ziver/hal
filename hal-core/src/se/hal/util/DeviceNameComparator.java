package se.hal.util;

import se.hal.intf.HalAbstractDevice;

import java.util.Comparator;

/**
 * A comparator that compares on the device name.
 */
public class DeviceNameComparator implements Comparator<HalAbstractDevice> {
    private static DeviceNameComparator instance;

    @Override
    public int compare(HalAbstractDevice device1, HalAbstractDevice device2) {
        return device1.getName().compareTo(device2.getName());
    }

    public static DeviceNameComparator getInstance() {
        if (instance == null)
            instance = new DeviceNameComparator();
        return instance;
    }
}
