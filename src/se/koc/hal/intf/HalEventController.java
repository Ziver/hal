package se.koc.hal.intf;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalEventController {
    /**
     * Will register an event type to be handled by this controller
     */
    public void register(HalEvent event);

    /**
     * Deregisters an event from this controller, the controller
     * will no longer handle that type of event
     */
    public void deregister(HalEvent event);

    /**
     * @param   event  transmit this event if possible.
     */
    public void send(HalEvent event); // TODO: where to put data?

    /**
     * @return the number of registered objects
     */
    public int size();

    /**
     * Set a listener that will receive all reports from the the registered Events
     */
    void setListener(HalEventReportListener listener);


    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    public void close();
}