package se.hal.intf;

/**
 * Controller interface for handling event based devices.
 */
public interface HalEventController extends HalAbstractController {

    /**
     * @param   eventConfig  the event configuration to target when sending
     * @param   eventData    the data to send
     */
    void send(HalEventConfig eventConfig, HalEventData eventData);
}