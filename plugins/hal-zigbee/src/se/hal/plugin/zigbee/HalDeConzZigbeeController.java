package se.hal.plugin.zigbee;

import se.hal.HalContext;
import se.hal.intf.*;
import zutil.log.LogUtil;

import java.util.logging.Logger;

/**
 * Class will handle Zigbee devices through the deConz REST API and with devices supporting.
 *
 * <p>
 * Rest documentatiuon for deConz: https://dresden-elektronik.github.io/deconz-rest-doc/
 */
public class HalDeConzZigbeeController implements HalSensorController, HalEventController, HalAutoScannableController {
    private static final Logger logger = LogUtil.getLogger();

    public static final String CONFIG_ZIGBEE_REST_URL = "zigbee.rest_url";
    public static final String CONFIG_ZIGBEE_REST_PORT = "zigbee.rest_port";
    public static final String CONFIG_ZIGBEE_REST_USERNAME = "zigbee.rest_username";
    public static final String CONFIG_ZIGBEE_REST_PASSWORD = "zigbee.rest_password";
    public static final String CONFIG_ZIGBEE_COM_PORT = "zigbee.com_port";


    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZIGBEE_REST_URL);
    }

    @Override
    public void initialize() throws Exception {
        // connect to deconz
        // if username is set use that for basic auth
        // else try without username or fail with log message that username should be setup

        // Get API key
    }


    @Override
    public void setListener(HalEventReportListener listener) {

    }

    @Override
    public void register(HalEventConfig eventConfig) {

    }

    @Override
    public void deregister(HalEventConfig eventConfig) {

    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {

    }


    @Override
    public void register(HalSensorConfig sensorConfig) {

    }

    @Override
    public void deregister(HalSensorConfig sensorConfig) {

    }

    @Override
    public void setListener(HalSensorReportListener listener) {

    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public void close() {

    }
}
