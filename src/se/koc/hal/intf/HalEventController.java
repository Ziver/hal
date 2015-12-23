package se.koc.hal.intf;

import se.koc.hal.struct.Event;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalEventController {
    /**
     * Will register an event type to be handled by this controller
     */
    public void register(Event event);

    /**
     * Deregisters an event from this controller, the controller
     * will no longer handle that type of event
     */
    public void deregister(Event event);

    /**
     * @param   event  transmit this event if possible.
     */
    public void send(Event event);

    /**
     * @return the number of registered objects
     */
    public int size();

    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    public void close();
}