package se.hal.intf;


/**
 * A listener interface that will be called when the
 * Event or Sensor that it is registered to receives a report
 *
 * @param   <C>     is the device configuration class
 * @param   <D>     is the device data class
 */
public interface HalDeviceReportListener<C extends HalDeviceConfig, D extends HalDeviceData> {

    void reportReceived(C deviceConfig, D deviceData);
}
