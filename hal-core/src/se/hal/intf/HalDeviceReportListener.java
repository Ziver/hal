package se.hal.intf;

import se.hal.struct.AbstractDevice;

/**
 * A listener interface that will be called when the
 * Event or Sensor that it is registered to receives a report
 *
 * @param   <T>     is the device type
 */
public interface HalDeviceReportListener<T extends AbstractDevice> {

    void receivedReport(T device);
}
