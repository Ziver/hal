package se.hal.plugin.tellstick;

import org.junit.Before;
import org.junit.Test;
import se.hal.intf.*;
import se.hal.struct.devicedata.DimmerEventData;
import zutil.converter.Converter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommEventTest {

    @Before
    public void init(){
        TellstickParser.registerProtocol(TestEvent.class);
    }


    //############# Non crashing TC

    @Test
    public void startup(){
        TellstickSerialComm tellstick = new TellstickSerialComm();
        tellstick.handleLine("+V2");
    }

    @Test
    public void unregisteredListener(){
        TellstickSerialComm tellstick = new TellstickSerialComm();
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:1234;");
    }


    //############ Normal TCs

    @Test
    public void receiveUnregisteredEvent(){
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
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:2345;");
        assertEquals("Events first transmission", 0, list.size());
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:2345;");
        assertEquals("Events Second transmission", 1, list.size());
    }

    @Test
    public void receiveEvent(){
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
        TestEvent event = new TestEvent();
        event.testData = 0xAAAA;
        tellstick.register(event);
        tellstick.handleLine("+Wclass:sensor;protocol:test-prot;model:test-model;data:AAAA;");
        // Verification
        assertEquals("Nr of received events", 1, list.size());
        assertEquals("Data", event.testData, ((TestEvent)list.get(0)).testData);
    }




    private static class TestEvent extends TellstickProtocol implements HalEventConfig,TellstickDevice {
        public int testData;

        public TestEvent(){
            super("test-prot", "test-model");
        }

        @Override
        public List<TellstickDecodedEntry> decode(byte[] data) {
            testData = Converter.toInt(data);

            ArrayList<TellstickDecodedEntry> list = new ArrayList<>();
            list.add(new TellstickDecodedEntry(
                    this, new DimmerEventData(testData, System.currentTimeMillis())
            ));
            return list;
        }


        @Override
        public Class<? extends HalEventController> getDeviceControllerClass() { return null; }
        @Override
        public Class<? extends HalEventData> getDeviceDataClass() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {return testData == ((TestEvent)obj).testData;}
    }
}
