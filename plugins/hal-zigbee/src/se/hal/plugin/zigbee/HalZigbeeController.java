package se.hal.plugin.zigbee;

import org.bubblecloud.zigbee.v3.SerialPort;
import org.bubblecloud.zigbee.v3.ZigBeeApiDongleImpl;
import org.bubblecloud.zigbee.v3.ZigBeeDevice;
import org.bubblecloud.zigbee.v3.ZigBeeDongleTiCc2531Impl;
import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.struct.AbstractDevice;
import zutil.log.LogUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class HalZigbeeController implements HalSensorController, HalEventController, HalAutoScannableController {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_ZIGBEE_PORT = "zigbee.com_port";
    private static final String CONFIG_ZIGBEE_PANID = "zigbee.pan_id";

    private SerialPort port;
    private ZigBeeApiDongleImpl zigbeeApi;

    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;
    private List<AbstractDevice> registeredDevices;


    public HalZigbeeController() {}


    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZIGBEE_PORT) &&
                HalContext.containsProperty(CONFIG_ZIGBEE_PANID);
    }
    @Override
    public void initialize() {
        initialize(
                HalContext.getStringProperty(CONFIG_ZIGBEE_PORT),
                HalContext.getIntegerProperty(CONFIG_ZIGBEE_PANID));
    }
    public void initialize(String comPort, int panId) {
        byte[] networkKey = null; // Default network key
        port = new SerialPortJSC(comPort);
        zigbeeApi = new ZigBeeApiDongleImpl(
                new ZigBeeDongleTiCc2531Impl(port, -6480, 11, networkKey, false),
                false);

        zigbeeApi.startup();

        for (ZigBeeDevice device : zigbeeApi.getDevices()) {
            System.out.println("Device: " + device.getLabel());
        }
    }

    @Override
    public void close() {
        logger.info("Shutting down Zigbee port.");

        zigbeeApi.shutdown();
        port.close();
    }


    // --------------------------
    // Hal Overrides
    // --------------------------

    @Override
    public void register(HalEventConfig event) {

    }
    @Override
    public void register(HalSensorConfig sensor) {

    }

    @Override
    public void deregister(HalEventConfig event) {
        registeredDevices.remove(event);
    }
    @Override
    public void deregister(HalSensorConfig sensor) {
        registeredDevices.remove(sensor);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {

    }

    @Override
    public void setListener(HalEventReportListener listener) {
        eventListener = listener;
    }
    @Override
    public void setListener(HalSensorReportListener listener) {
        sensorListener = listener;
    }
}
