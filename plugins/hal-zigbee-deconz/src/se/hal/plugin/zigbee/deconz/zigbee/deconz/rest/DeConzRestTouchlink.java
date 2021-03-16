package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * The touchlink endpoint allows to communicate with near by located devices.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/touchlink/
 */
public interface DeConzRestTouchlink {

    /**
     * Starts scanning on all channels for devices which are located close to the gateway. The whole scan process will take about 10 seconds.
     * <p>Note: While scanning is in progress further API requests which require network access aren’t allowed.
     */
    @WSRequestType(POST)
    @WSPath("/api/{{requestApiKey}}/touchlink/scan")
    void startDeviceScan(String requestApiKey);

    /**
     * Returns the results of a touchlink scan.
     */
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/touchlink/scan")
    void getScanResult(String requestApiKey);

    /**
     * Puts a device into identify mode for example a light will blink a few times.
     * <p>Note: touchlinkId must be one of the indentifiers which are returned in the scan result.
     */
    @WSRequestType(POST)
    @WSPath("/api/{{requestApiKey}}/touchlink/{{touchlinkId}}/identify")
    void identifyDevice(String requestApiKey, int touchlinkId);

    /**
     * Send a reset to factory new request to a device.
     * <p>Note: touchlinkId must be one of the indentifiers which are returned in the scan result.
     */
    @WSRequestType(POST)
    @WSPath("/api/{{requestApiKey}}/touchlink/{{touchlinkId}}/reset")
    void resetDevice(String requestApiKey, int touchlinkId);
}
