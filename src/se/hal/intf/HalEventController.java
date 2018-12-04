package se.hal.intf;

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
    void register(HalEventConfig event);

    /**
     * Deregisters an event from this controller, the controller
     * will no longer handle that type of event
     */
    void deregister(HalEventConfig event);

    /**
     * @param   eventConfig  the event configuration to target when sending
     * @param   eventData    the data to send
     */
    void send(HalEventConfig eventConfig, HalEventData eventData);

    /**
     * @return the number of registered objects
     */
    int size();

    /**
     * Set a listener that will receive all reports from the the registered Events
     */
    void setListener(HalEventReportListener listener);


    /**
     * Close any resources associated with this controller.
     * This method could be called multiple times, first time
     * should be handled as normal any subsequent calls should be ignored.
     */
    void close();
}