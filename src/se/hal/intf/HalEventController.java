package se.hal.intf;

public interface HalEventController extends HalAbstractController {

    /**
     * Will register an event type to be handled by this controller
     */
    void register(HalEventConfig eventConfig);

    /**
     * Deregisters an event from this controller, the controller
     * will no longer handle that type of event
     */
    void deregister(HalEventConfig eventConfig);

    /**
     * @param   eventConfig  the event configuration to target when sending
     * @param   eventData    the data to send
     */
    void send(HalEventConfig eventConfig, HalEventData eventData);

    /**
     * Set a listener that will receive all reports from the the registered Events
     */
    void setListener(HalEventReportListener listener);
}