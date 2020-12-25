package se.hal.plugin.zigbee;

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

    private static final String CONFIG_ZIGBEE_PORT = "";

    private SerialPort port;
    private ZigBeeApiDongleImpl zigbeeApi;

    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;
    private List<AbstractDevice> registeredDevices;


    public HalZigbeeController() {}


    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZIGBEE_PORT);
    }
    @Override
    public void initialize() {
        initialize(HalContext.getStringProperty(CONFIG_ZIGBEE_PORT));
    }
    public void initialize(String comPort) {
        byte[] networkKey = null; // Default network key
        port = new SerialPortImpl(comPort);
        zigbeeApi = new ZigBeeApiDongleImpl(
                new ZigBeeDongleTiCc2531Impl(port, 4951, 11, networkKey, false),
                false);

        zigbeeApi.startup();

        ZigBeeDevice device = zigbeeApi.getZigBeeDevices().get(3);

        zigbeeApi.on(device);
        Thread.sleep(1000);
        zigbeeApi.color(device, 1.0, 0.0, 0.0, 1.0);
        Thread.sleep(1000);
        zigbeeApi.color(device, 0.0, 1.0, 0.0, 1.0);
        Thread.sleep(1000);
        zigbeeApi.color(device, 0.0, 0.0, 1.0, 1.0);
        Thread.sleep(1000);
        zigbeeApi.off(device);
    }

    @Override
    public void close() {
        logger.info("Shutting down Zigbee port...");

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
