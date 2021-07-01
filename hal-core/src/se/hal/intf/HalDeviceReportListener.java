package se.hal.intf;


/**
 * A listener interface that will be called when the
 * Event or Sensor that it is registered to receives a report
 */
public interface HalDeviceReportListener {

    void reportReceived(HalDeviceConfig deviceConfig, HalDeviceData deviceData);
}
