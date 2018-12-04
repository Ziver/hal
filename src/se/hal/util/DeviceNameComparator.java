package se.hal.util;

import se.hal.struct.AbstractDevice;

import java.util.Comparator;

/**
 * A comparator that compares on the device name.
 */
public class DeviceNameComparator implements Comparator<AbstractDevice> {
    private static DeviceNameComparator instance;

    @Override
    public int compare(AbstractDevice device1, AbstractDevice device2) {
        return device1.getName().compareTo(device2.getName());
    }

    public static DeviceNameComparator getInstance() {
        if (instance == null)
            instance = new DeviceNameComparator();
        return instance;
    }
}
