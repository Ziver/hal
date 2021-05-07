package se.hal.plugin.tellstick;

import org.junit.Before;
import org.junit.Test;
import se.hal.intf.HalDeviceReportListener;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.plugin.tellstick.test.TestEventDevice;
import se.hal.plugin.tellstick.test.TestProtocol;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TellstickSerialCommEventTest {

    @Before
    public void init() {
        TellstickParser.registerProtocol(TestProtocol.class);
    }

    // ----------------------------------------------------
    // Non crashing TC
    // ----------------------------------------------------

    @Test
    public void startup() {
        TellstickSerialComm tellstick = new TellstickSerialComm();
        tellstick.handleLine("+V2");
    }

    @Test
    public void unregisteredListener() {
        TellstickSerialComm tellstick = new TellstickSerialComm();
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:1234;");
    }

    // ----------------------------------------------------
    // Normal TCs
    // ----------------------------------------------------

    @Test
    public void receiveUnregisteredEvent() {
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalEventConfig> list = new ArrayList<>();
        tellstick.setListener(new HalDeviceReportListener<HalEventConfig,HalEventData>() {
            @Override
            public void reportReceived(HalEventConfig e, HalEventData d) {
                list.add(e);
            }
        });
        // Execution
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:6345;");
        assertEquals("Events first transmission", 0, list.size());
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:6345;");
        assertEquals("Events Second transmission", 1, list.size());
    }

    @Test
    public void receiveEvent() {
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalEventConfig> list = new ArrayList<>();
        tellstick.setListener(new HalDeviceReportListener<HalEventConfig,HalEventData>() {
            @Override
            public void reportReceived(HalEventConfig e, HalEventData d) {
                list.add(e);
            }
        });
        // Execution
        TestEventDevice event = new TestEventDevice();
        event.testData = 0xAAAA;
        tellstick.register(event);
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:AAAA;");
        // Verification
        assertEquals("Nr of received events", 1, list.size());
        assertEquals("Data", event.testData, ((TestEventDevice)list.get(0)).testData);
    }

}
