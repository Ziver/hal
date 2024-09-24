package se.hal.test;

import se.hal.intf.HalDeviceData;

import static org.junit.Assert.assertEquals;

/**
 * Class contains assertions that can be useful
 */
public class HalAssert {

    public static void assertEqualsIgnoreTimestamp(HalDeviceData expected, HalDeviceData actual) {
        if (expected != null) {
            expected.setTimestamp(0);
        }
        if (actual != null) {
            actual.setTimestamp(0);
        }

        assertEquals(expected, actual);
    }
}
