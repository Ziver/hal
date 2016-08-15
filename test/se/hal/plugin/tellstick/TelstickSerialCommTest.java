package se.hal.plugin.tellstick;

import org.junit.Before;
import org.junit.Test;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalEventReportListener;
import se.hal.intf.HalSensorData;
import se.hal.intf.HalSensorReportListener;
import se.hal.plugin.tellstick.protocols.Oregon0x1A2D;
import zutil.converter.Converter;
import zutil.db.DBConnection;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;
import zutil.struct.MutableInt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommTest {

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
    public void unregisteredEvent(){
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalEventData> list = new ArrayList<>();
        tellstick.setListener(new HalEventReportListener() {
            @Override
            public void reportReceived(HalEventData e) {
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
    public void event(){
        // Setup
        TellstickSerialComm tellstick = new TellstickSerialComm();
        final ArrayList<HalEventData> list = new ArrayList<>();
        tellstick.setListener(new HalEventReportListener() {
            @Override
            public void reportReceived(HalEventData e) {
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



    private static class TestEvent extends TellstickProtocol implements HalEventData{
        public int testData;

        public TestEvent(){
            super("test-prot", "test-model");
        }

        @Override
        public void decode(byte[] data) {
            testData = Converter.toInt(data);
        }


        @Override
        public String encode() {return null;}
        @Override
        public double getData() {return 0;}
        @Override
        public boolean equals(Object obj) {return testData == ((TestEvent)obj).testData;}
    }
}
