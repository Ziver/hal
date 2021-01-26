package se.hal;

import org.junit.Test;
import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.struct.devicedata.OnOffEventData;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EventControllerManagerTest {

    private ControllerManager manager = new ControllerManager();


    @Test
    public void addAvailableEvent(){
        assertEquals(0, manager.getAvailableEvents().size());

        manager.addAvailableEvent(TestEvent1.class);
        assertEquals(1, manager.getAvailableEvents().size());
        assertTrue(manager.getAvailableEvents().contains(TestEvent1.class));

        manager.addAvailableEvent(TestEvent2.class);
        assertEquals(2, manager.getAvailableEvents().size());
        assertTrue(manager.getAvailableEvents().contains(TestEvent1.class));
        assertTrue(manager.getAvailableEvents().contains(TestEvent2.class));

        // Add duplicate Event
        manager.addAvailableEvent(TestEvent1.class);
        assertEquals("No duplicate check",2, manager.getAvailableEvents().size());
    }


    @Test
    public void registerUnavailableEvent(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableEvents());

        Event Event = new Event();
        Event.setDeviceConfig(new TestEvent1());
        manager.register(Event);
        assertEquals("No Event registered", Collections.EMPTY_LIST, manager.getRegisteredEvents());
    }


    @Test
    public void registerOneEvent() {
        Event Event1 = registerEvent(new TestEvent1());
        assertEquals(1, manager.getRegisteredEvents().size());
        assertTrue(manager.getRegisteredEvents().contains(Event1));
    }
    public void registerTwoEvents(){
        Event Event1 = registerEvent(new TestEvent1());
        Event Event2 = registerEvent(new TestEvent2());
        assertEquals(2, manager.getRegisteredEvents().size());
        assertTrue(manager.getRegisteredEvents().contains(Event1));
        assertTrue(manager.getRegisteredEvents().contains(Event2));
    }


    @Test
    public void deregisterEvent(){
        Event Event1 = registerEvent(new TestEvent1());
        manager.deregister(Event1);
        assertEquals(Collections.EMPTY_LIST, manager.getRegisteredEvents());
    }


    // TODO: TC for reportReceived


    //////////////////////////////////////////////////////////
    private Event registerEvent(HalEventConfig config){
        Event Event = new Event();
        Event.setDeviceConfig(config);
        manager.addAvailableEvent(config.getClass());
        manager.register(Event);
        return Event;
    }

    public static class TestEvent1 implements HalEventConfig {

        @Override
        public Class<? extends HalEventController> getEventControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalEventData> getEventDataClass() {
            return OnOffEventData.class;
        }
    }

    public static class TestEvent2 implements HalEventConfig {

        @Override
        public Class<? extends HalEventController> getEventControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalEventData> getEventDataClass() {
            return OnOffEventData.class;
        }
    }

    public static class TestController implements HalEventController {
        int size;

        @Override
        public void initialize() throws Exception { }

        @Override
        public void register(HalEventConfig event) {
            size++;
        }

        @Override
        public void deregister(HalEventConfig event) {
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
        public void setListener(HalEventReportListener listener) { }

        @Override
        public void close() { }
    }
}