package se.hal.intf;

public interface HalSensorReportListener {

    void reportReceived(HalSensorConfig s, HalSensorData d);

}