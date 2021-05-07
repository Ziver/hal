package se.hal.plugin.tellstick;

import org.junit.Before;
import org.junit.Test;
import se.hal.intf.HalDeviceReportListener;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.test.TestProtocol;
import se.hal.plugin.tellstick.test.TestSensorDevice;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TellstickSerialCommSensorTest {

    @Before
    public void init(){
        TellstickParser.registerProtocol(TestProtocol.class);
    }


    // ----------------------------------------------------
    // Normal TCs
    // ----------------------------------------------------

    @Test
    public void receiveUnregisteredSensor() {
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalSensorConfig> list = new ArrayList<>();
        tellstick.setListener(new HalDeviceReportListener<HalSensorConfig,HalSensorData>() {
            @Override
            public void reportReceived(HalSensorConfig e, HalSensorData d) {
                list.add(e);
            }
        });
        // Execution
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:15;");
        assertEquals("Sensors first transmission", 0, list.size());
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:15;");
        assertEquals("Sensors Second transmission", 1, list.size());
    }

    @Test
    public void receiveSensor() {
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalSensorConfig> list = new ArrayList<>();
        tellstick.setListener(new HalDeviceReportListener<HalSensorConfig,HalSensorData>() {
            @Override
            public void reportReceived(HalSensorConfig e, HalSensorData d) {
                list.add(e);
            }
        });
        // Execution
        TestSensorDevice sensor = new TestSensorDevice();
        sensor.testData = 0xAA;
        tellstick.register(sensor);
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:AA;");
        // Verification
        assertEquals("Nr of received sensors", 1, list.size());
        assertEquals("Data", sensor.testData, ((TestSensorDevice)list.get(0)).testData);
    }

}
