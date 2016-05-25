package se.hal.intf;

/**
 * Created by Ziver on 2015-12-15.
 */
public interface HalEventController {

    /**
     * The framework might create dummy objects so any type of
     * resource initialization should be handled in this method
     * and not in the constructor.
     */
    void initialize() throws Exception;

    /**
     * Will register an event type to be handled by this controller
     */
    public void register(HalEventData event);

    /**
     * Deregisters an event from this controller, the controller
     * will no longer handle that type of event
     */
    public void deregister(HalEventData event);

    /**
     * @param   event  transmit this event if possible.
     */
    public void send(HalEventData event); // TODO: where to put data?

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