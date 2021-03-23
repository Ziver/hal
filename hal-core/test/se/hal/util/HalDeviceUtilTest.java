package se.hal.util;

import junit.framework.TestCase;
import org.junit.Test;
import se.hal.struct.Event;
import se.hal.test.TestDeviceConfig;

import java.util.ArrayList;
import java.util.List;

public class HalDeviceUtilTest extends TestCase {

    @Test
    public void testFindDevice() {
        List<Event> eventList = new ArrayList<>();

        Event device = new Event();
        device.setDeviceConfig(new TestDeviceConfig("not test"));
        eventList.add(device);

        Event device2 = new Event();
        device2.setDeviceConfig(new TestDeviceConfig("not test"));
        eventList.add(device2);

        Event expectedDevice = new Event();
        expectedDevice.setDeviceConfig(new TestDeviceConfig("test"));
        eventList.add(expectedDevice);

        assertEquals(expectedDevice, HalDeviceUtil.findDevice(new TestDeviceConfig("test"), eventList));
    }
}