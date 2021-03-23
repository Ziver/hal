package se.hal;

import org.junit.Test;
import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.struct.devicedata.OnOffEventData;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EventControllerManagerTest {

    private EventControllerManager manager = new EventControllerManager();


    @Test
    public void addAvailableEventDevice(){
        assertEquals(0, manager.getAvailableDeviceConfigs().size());

        manager.addAvailableDevice(TestEvent1.class);
        assertEquals(1, manager.getAvailableDeviceConfigs().size());
        assertTrue(manager.getAvailableDeviceConfigs().contains(TestEvent1.class));

        manager.addAvailableDevice(TestEvent2.class);
        assertEquals(2, manager.getAvailableDeviceConfigs().size());
        assertTrue(manager.getAvailableDeviceConfigs().contains(TestEvent1.class));
        assertTrue(manager.getAvailableDeviceConfigs().contains(TestEvent2.class));

        // Add duplicate Event
        manager.addAvailableDevice(TestEvent1.class);
        assertEquals("No duplicate check",2, manager.getAvailableDeviceConfigs().size());
    }


    @Test
    public void registerUnavailableEvent(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableDeviceConfigs());

        Event Event = new Event();
        Event.setDeviceConfig(new TestEvent1());
        manager.register(Event);
        assertEquals("No Event registered", Collections.EMPTY_LIST, manager.getRegisteredDevices());
    }


    @Test
    public void registerOneEvent() {
        Event Event1 = registerEvent(new TestEvent1());
        assertEquals(1, manager.getRegisteredDevices().size());
        assertTrue(manager.getRegisteredDevices().contains(Event1));
    }
    public void registerTwoEvents(){
        Event Event1 = registerEvent(new TestEvent1());
        Event Event2 = registerEvent(new TestEvent2());
        assertEquals(2, manager.getRegisteredDevices().size());
        assertTrue(manager.getRegisteredDevices().contains(Event1));
        assertTrue(manager.getRegisteredDevices().contains(Event2));
    }


    @Test
    public void deregisterEvent(){
        Event Event1 = registerEvent(new TestEvent1());
        manager.deregister(Event1);
        assertEquals(Collections.EMPTY_LIST, manager.getRegisteredDevices());
    }


    // TODO: TC for reportReceived


    //////////////////////////////////////////////////////////
    private Event registerEvent(HalEventConfig config){
        Event Event = new Event();
        Event.setDeviceConfig(config);
        manager.addAvailableDevice(config.getClass());
        manager.register(Event);
        return Event;
    }

    public static class TestEvent1 implements HalEventConfig {

        @Override
        public Class<? extends HalEventController> getDeviceControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalEventData> getDeviceDataClass() {
            return OnOffEventData.class;
        }

        @Override
        public boolean equals(Object obj) {
            return this.equals(obj);
        }
    }

    public static class TestEvent2 implements HalEventConfig {

        @Override
        public Class<? extends HalEventController> getDeviceControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalEventData> getDeviceDataClass() {
            return OnOffEventData.class;
        }

        @Override
        public boolean equals(Object obj) {
            return this.equals(obj);
        }
    }

    public static class TestController implements HalEventController {
        int size;

        @Override
        public void initialize() { }

        @Override
        public void register(HalDeviceConfig event) {
            size++;
        }

        @Override
        public void deregister(HalDeviceConfig event) {
            size--;
        }

        @Override
        public void send(HalEventConfig eventConfig, HalEventData eventData) {

        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public void setListener(HalDeviceReportListener listener) { }

        @Override
        public void close() { }
    }
}