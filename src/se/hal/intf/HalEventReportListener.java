package se.hal.intf;

public interface HalEventReportListener {

    void reportReceived(HalEventConfig e, HalEventData d);

}