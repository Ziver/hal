package se.hal.plugin.tellstick;

import org.junit.Before;
import org.junit.Test;
import se.hal.intf.*;
import se.hal.struct.devicedata.SwitchEventData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.converter.Converter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommSensorTest {

    @Before
    public void init(){
        TellstickParser.registerProtocol(TestSensor.class);
    }
    

    //############ Normal TCs

    @Test
    public void receiveUnregisteredSensor(){
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalSensorConfig> list = new ArrayList<>();
        tellstick.setListener(new HalSensorReportListener() {
            @Override
            public void reportReceived(HalSensorConfig e, HalSensorData d) {
                list.add(e);
            }
        });
        // Execution
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:2345;");
        assertEquals("Sensors first transmission", 0, list.size());
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:2345;");
        assertEquals("Sensors Second transmission", 1, list.size());
    }

    @Test
    public void receiveSensor(){
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalSensorConfig> list = new ArrayList<>();
        tellstick.setListener(new HalSensorReportListener() {
            @Override
            public void reportReceived(HalSensorConfig e, HalSensorData d) {
                list.add(e);
            }
        });
        // Execution
        TestSensor sensor = new TestSensor();
        sensor.testData = 0xAAAA;
        tellstick.register(sensor);
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:AAAA;");
        // Verification
        assertEquals("Nr of received sensors", 1, list.size());
        assertEquals("Data", sensor.testData, ((TestSensor)list.get(0)).testData);
    }




    private static class TestSensor extends TellstickProtocol implements HalSensorConfig,TellstickDevice {
        public int testData;

        public TestSensor(){
            super("test-prot", "test-model");
        }

        @Override
        public List<TellstickDecodedEntry> decode(byte[] data) {
            testData = Converter.toInt(data);

            ArrayList<TellstickDecodedEntry> list = new ArrayList<>();
            list.add(new TellstickDecodedEntry(
                    this, new TemperatureSensorData(testData)
            ));
            return list;
        }


        @Override
        public boolean equals(Object obj) {return testData == ((TestSensor)obj).testData;}


        @Override
        public long getDataInterval() { return 0; }
        @Override
        public AggregationMethod getAggregationMethod() { return null; }
        @Override
        public Class<? extends HalSensorController> getSensorControllerClass() { return null; }
        @Override
        public Class<? extends HalSensorData> getSensorDataClass() {
            return null;
        }
    }
}
