package se.hal.intf;

/**
 * Listener to be called by the {@link HalEventController} to report that a event has been received.
 */
public interface HalEventReportListener {

    void reportReceived(HalEventConfig e, HalEventData d);

}