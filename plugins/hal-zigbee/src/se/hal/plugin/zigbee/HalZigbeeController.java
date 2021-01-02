package se.hal.plugin.zigbee;


import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.ZigBeeStatus;
import com.zsmartsystems.zigbee.dongle.cc2531.ZigBeeDongleTiCc2531;
import com.zsmartsystems.zigbee.dongle.conbee.ZigBeeDongleConBee;
import com.zsmartsystems.zigbee.dongle.xbee.ZigBeeDongleXBee;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.transport.ZigBeeTransportTransmit;
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

    private ZigBeePort serialPort;
    private ZigBeeDataStore dataStore;
    private ZigBeeNetworkManager networkManager;

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
        initialize(
                HalContext.getStringProperty(CONFIG_ZIGBEE_PORT));
    }
    public void initialize(String comPort) {
        serialPort = new ZigBeeJSerialCommPort(comPort);
        dataStore = new ZigBeeDataStore();

        ZigBeeTransportTransmit dongle = getDongle("CC2531");
        ZigBeeNetworkManager networkManager = new ZigBeeNetworkManager(dongle);
        networkManager.setNetworkDataStore(dataStore);

        ZigBeeStatus initResponse = networkManager.initialize();
        System.out.println("NetworkManager.initialize() returned " + initResponse);

        System.out.println("PAN ID          = " + networkManager.getZigBeePanId());
        System.out.println("Extended PAN ID = " + networkManager.getZigBeeExtendedPanId());
        System.out.println("Channel         = " + networkManager.getZigBeeChannel());

        if (dongle instanceof ZigBeeDongleTiCc2531) {
            ZigBeeDongleTiCc2531 tiDongle = (ZigBeeDongleTiCc2531) dongle;
            tiDongle.setLedMode(1, false);
            tiDongle.setLedMode(2, false);
        }
    }

    private ZigBeeTransportTransmit getDongle(String name) {
        switch (name) {
        case "CC2531":
            return new ZigBeeDongleTiCc2531(serialPort);
        case "XBEE":
            return new ZigBeeDongleXBee(serialPort);
        case "CONBEE":
            return new ZigBeeDongleConBee(serialPort);
        default:
            logger.severe("Unknown ZigBee dongle: " + name);
            return null;
        }
    }

    @Override
    public void close() {
        logger.info("Shutting down Zigbee port.");

        networkManager.shutdown();
        serialPort.close();
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
