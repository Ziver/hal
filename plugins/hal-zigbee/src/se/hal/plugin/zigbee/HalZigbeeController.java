package se.hal.plugin.zigbee;


import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.ZigBeeProfileType;
import com.zsmartsystems.zigbee.ZigBeeStatus;
import com.zsmartsystems.zigbee.app.basic.ZigBeeBasicServerExtension;
import com.zsmartsystems.zigbee.app.discovery.ZigBeeDiscoveryExtension;
import com.zsmartsystems.zigbee.app.iasclient.ZigBeeIasCieExtension;
import com.zsmartsystems.zigbee.app.otaserver.ZigBeeOtaUpgradeExtension;
import com.zsmartsystems.zigbee.dongle.cc2531.ZigBeeDongleTiCc2531;
import com.zsmartsystems.zigbee.dongle.conbee.ZigBeeDongleConBee;
import com.zsmartsystems.zigbee.dongle.xbee.ZigBeeDongleXBee;
import com.zsmartsystems.zigbee.security.ZigBeeKey;
import com.zsmartsystems.zigbee.serialization.DefaultDeserializer;
import com.zsmartsystems.zigbee.serialization.DefaultSerializer;
import com.zsmartsystems.zigbee.transport.*;
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
    protected ZigBeeNetworkManager networkManager;

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

        // ----------------------------
        // Initialize Transport Network
        // ----------------------------

        ZigBeeTransportTransmit dongle = getDongle("CC2531", serialPort);
        networkManager = new ZigBeeNetworkManager(dongle);
        networkManager.setNetworkDataStore(dataStore);
        networkManager.setSerializer(DefaultSerializer.class, DefaultDeserializer.class);

        // Initialize Network

        logger.info("Initializing ZigBee Network...");
        ZigBeeStatus initResponse = networkManager.initialize();
        logger.info("ZigBee Network initialization finished with: " + initResponse);

        // ------------------------
        // Startup Network
        // ------------------------

        networkManager.setDefaultProfileId(ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.getKey());

        TransportConfig transportOptions = new TransportConfig();
        transportOptions.addOption(TransportConfigOption.RADIO_TX_POWER, 3);
        transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_JOIN_MODE, TrustCentreJoinMode.TC_JOIN_SECURE);
        transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_LINK_KEY, new ZigBeeKey(new int[] {
                0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39 }));
        dongle.updateTransportConfig(transportOptions);

        // Register extensions

        networkManager.addExtension(new ZigBeeOtaUpgradeExtension());
        networkManager.addExtension(new ZigBeeBasicServerExtension());
        networkManager.addExtension(new ZigBeeDiscoveryExtension());

        // Startup Network

        logger.info("Starting up ZigBee Network...");
        ZigBeeStatus startResponse = networkManager.startup(false);
        logger.info("ZigBee Network startup finished with: " + startResponse);

        // -----------
        // Other stuff
        // -----------

        /*if (dongle instanceof ZigBeeDongleTiCc2531) {
            ZigBeeDongleTiCc2531 tiDongle = (ZigBeeDongleTiCc2531) dongle;
            tiDongle.setLedMode(1, false);
            tiDongle.setLedMode(2, false);
        }*/
    }

    private static ZigBeeTransportTransmit getDongle(String name, ZigBeePort serialPort) {
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
