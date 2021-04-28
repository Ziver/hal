package se.hal.plugin.zigbee;


import com.zsmartsystems.zigbee.*;
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
import com.zsmartsystems.zigbee.zcl.clusters.*;
import com.zsmartsystems.zigbee.zdo.command.NetworkAddressRequest;
import com.zsmartsystems.zigbee.zdo.field.NodeDescriptor;
import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.intf.HalAbstractDevice;
import zutil.log.LogUtil;

import java.nio.channels.Channel;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class HalZigbeeController implements HalSensorController,
        HalEventController,
        HalAutoScannableController,
        ZigBeeAnnounceListener,
        ZigBeeNetworkNodeListener {

    private static final Logger logger = LogUtil.getLogger();

    public static final String ZIGBEE_DONGLE_CC2531 = "CC2531";
    public static final String ZIGBEE_DONGLE_CONBEE = "CONBEE";
    public static final String ZIGBEE_DONGLE_XBEE   = "XBEE";

    private static final String CONFIG_ZIGBEE_PORT = "zigbee.com_port";
    private static final String CONFIG_ZIGBEE_DONGLE = "zigbee.dongle";

    private ZigBeePort serialPort;
    private ZigBeeDataStore dataStore;
    protected ZigBeeNetworkManager networkManager;

    private HalDeviceReportListener deviceListener;
    private List<HalAbstractDevice> registeredDevices;


    public HalZigbeeController() {}

    // --------------------------
    // Lifecycle Methods
    // --------------------------

    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZIGBEE_PORT) &&
                HalContext.containsProperty(CONFIG_ZIGBEE_DONGLE);
    }
    @Override
    public void initialize() {
        initialize(HalContext.getStringProperty(CONFIG_ZIGBEE_PORT), HalContext.getStringProperty(CONFIG_ZIGBEE_DONGLE));
    }
    public void initialize(String comPort, String dongleName) {
        serialPort = new ZigBeeJSerialCommPort(comPort, ZigBeeJSerialCommPort.DEFAULT_BAUD_RATE, ZigBeePort.FlowControl.FLOWCONTROL_OUT_RTSCTS);
        dataStore = new ZigBeeDataStore();
        TransportConfig transportOptions = new TransportConfig();

        // ----------------------------
        // Initialize Transport Network
        // ----------------------------

        ZigBeeTransportTransmit dongle = getDongle(dongleName, serialPort, transportOptions);
        networkManager = new ZigBeeNetworkManager(dongle);
        networkManager.setNetworkDataStore(dataStore);
        networkManager.setSerializer(DefaultSerializer.class, DefaultDeserializer.class);
        networkManager.addAnnounceListener(this);
        networkManager.addNetworkNodeListener(this);

        // Initialize Network

        logger.info("Initializing ZigBee Network...");
        ZigBeeStatus initResponse = networkManager.initialize();
        logger.info("ZigBee Network initialization finished with: " + initResponse);

        // ------------------------
        // Startup Network
        // ------------------------

        networkManager.setDefaultProfileId(ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.getKey());
        networkManager.setZigBeeLinkKey(new ZigBeeKey(new int[] { // Add the default ZigBeeAlliance09 HA link key
                0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39 }));
        networkManager.setZigBeeNetworkKey(new ZigBeeKey("552FAAF9B5F49E75F1ADDA12215C2CA1")); // ZigBeeKey.createRandom();
        networkManager.setZigBeeChannel(ZigBeeChannel.create(11));
        networkManager.setZigBeePanId(65534); // (int) Math.floor((Math.random() * 65534));
        networkManager.setZigBeeExtendedPanId(new ExtendedPanId("00124B001CCE1B5F")); // ExtendedPanId.createRandom();

        //transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_JOIN_MODE, TrustCentreJoinMode.TC_JOIN_INSECURE); // TC_JOIN_SECURE
        dongle.updateTransportConfig(transportOptions);

        // Register extensions

        networkManager.addExtension(new ZigBeeOtaUpgradeExtension());
        networkManager.addExtension(new ZigBeeDiscoveryExtension());
        networkManager.addExtension(new ZigBeeIasCieExtension());

        networkManager.addSupportedClientCluster(ZclBasicCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclIdentifyCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclGroupsCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclScenesCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclPollControlCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclOnOffCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclLevelControlCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclColorControlCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclPressureMeasurementCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclTemperatureMeasurementCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclThermostatCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclRelativeHumidityMeasurementCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclWindowCoveringCluster.CLUSTER_ID);
        networkManager.addSupportedClientCluster(ZclBinaryInputBasicCluster.CLUSTER_ID);

        networkManager.addSupportedServerCluster(ZclBasicCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclIdentifyCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclGroupsCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclScenesCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclPollControlCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclOnOffCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclLevelControlCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclColorControlCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclPressureMeasurementCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclWindowCoveringCluster.CLUSTER_ID);
        networkManager.addSupportedServerCluster(ZclBinaryInputBasicCluster.CLUSTER_ID);

        // Startup Network

        logger.info("Starting up ZigBee Network...");
        ZigBeeStatus startResponse = networkManager.startup(false);
        logger.info("ZigBee Network startup finished with: " + startResponse);
    }

    private static ZigBeeTransportTransmit getDongle(String name, ZigBeePort serialPort, TransportConfig transportOptions) {
        switch (name) {
        case ZIGBEE_DONGLE_CC2531:
            HashSet<Integer> clusters = new HashSet<>();
            clusters.add(ZclIasZoneCluster.CLUSTER_ID);

            transportOptions.addOption(TransportConfigOption.SUPPORTED_OUTPUT_CLUSTERS, clusters);
            transportOptions.addOption(TransportConfigOption.RADIO_TX_POWER, 3);

            return new ZigBeeDongleTiCc2531(serialPort);
        case ZIGBEE_DONGLE_CONBEE:
            return new ZigBeeDongleConBee(serialPort);
        case ZIGBEE_DONGLE_XBEE:
            return new ZigBeeDongleXBee(serialPort);
        default:
            logger.severe("Unknown ZigBee dongle: " + name);
            return null;
        }
    }

    @Override
    public void close() {
        logger.info("Shutting down Zigbee network.");

        networkManager.shutdown();
        serialPort.close();
    }

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public void deviceStatusUpdate(ZigBeeNodeStatus deviceStatus, Integer networkAddress, IeeeAddress ieeeAddress) {
        System.out.println(deviceStatus.name() + " status updated.");
    }

    @Override
    public void announceUnknownDevice(Integer networkAddress) {
        System.out.println("Unknown device: " + networkAddress);
    }

    @Override
    public void nodeAdded(final ZigBeeNode node) {
        System.out.println("nodeAdded: " + node);

        // If this is the coordinator (NWK address 0), ignore this device
        if (node.getLogicalType() == NodeDescriptor.LogicalType.COORDINATOR || node.getNetworkAddress() == 0) {
            System.out.println(node.getIeeeAddress() + ": is a coordinator, skipping.");
            return;
        }

        if (!node.isDiscovered()) {
            System.out.println(node.getIeeeAddress() + ": Node discovery not complete");
            return;
        }

        // Perform the device properties discovery.

        System.out.println(node.getIeeeAddress() + ": " +
                "Manufacturer=" + node.getNodeDescriptor().getManufacturerCode());
    }

    @Override
    public void nodeUpdated(final ZigBeeNode node) {
        System.out.println("nodeUpdated: " + node);
    }

    @Override
    public void nodeRemoved(final ZigBeeNode node) {
        System.out.println("nodeRemoved: " + node);
    }

    // --------------------------
    // Hal Overrides
    // --------------------------

    @Override
    public void register(HalDeviceConfig deviceConfig) {

    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {

    }

    @Override
    public void setListener(HalDeviceReportListener listener) {
        deviceListener = listener;
    }
}
