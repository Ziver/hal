package se.hal.plugin.zigbee;


import com.zsmartsystems.zigbee.*;
import com.zsmartsystems.zigbee.app.discovery.ZigBeeDiscoveryExtension;
import com.zsmartsystems.zigbee.app.iasclient.ZigBeeIasCieExtension;
import com.zsmartsystems.zigbee.app.otaserver.ZigBeeOtaUpgradeExtension;
import com.zsmartsystems.zigbee.dongle.cc2531.ZigBeeDongleTiCc2531;
import com.zsmartsystems.zigbee.dongle.conbee.ZigBeeDongleConBee;
import com.zsmartsystems.zigbee.dongle.xbee.ZigBeeDongleXBee;
import com.zsmartsystems.zigbee.security.ZigBeeKey;
import com.zsmartsystems.zigbee.serialization.DefaultDeserializer;
import com.zsmartsystems.zigbee.serialization.DefaultSerializer;
import com.zsmartsystems.zigbee.transport.TransportConfig;
import com.zsmartsystems.zigbee.transport.TransportConfigOption;
import com.zsmartsystems.zigbee.transport.ZigBeePort;
import com.zsmartsystems.zigbee.transport.ZigBeeTransportTransmit;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclAttributeListener;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.clusters.*;
import com.zsmartsystems.zigbee.zdo.field.NodeDescriptor;
import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.plugin.zigbee.device.*;
import zutil.Timer;
import zutil.log.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class HalZigbeeController implements HalSensorController,
        HalEventController,
        HalAutostartController,
        HalScannableController,
        ZigBeeAnnounceListener,
        ZigBeeNetworkNodeListener,
        ZigBeeNetworkEndpointListener {

    private static final Logger logger = LogUtil.getLogger();

    public static final String ZIGBEE_DONGLE_CC2531 = "CC2531";
    public static final String ZIGBEE_DONGLE_CONBEE = "CONBEE";
    public static final String ZIGBEE_DONGLE_XBEE   = "XBEE";

    private static final String CONFIG_ZIGBEE_PORT = "zigbee.com_port";
    private static final String CONFIG_ZIGBEE_DONGLE = "zigbee.dongle";

    private ZigBeePort serialPort;
    private ZigBeeDataStore dataStore;
    protected ZigBeeNetworkManager networkManager;

    private Timer permitJoinTimer;
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private List<ZigbeeHalDeviceConfig> registeredDevices = new ArrayList<>();


    public HalZigbeeController() {}

    // ------------------------------------------
    // Lifecycle Methods
    // ------------------------------------------

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

        // Register extensions

        ZigBeeDiscoveryExtension discoveryExtension = new ZigBeeDiscoveryExtension();
        discoveryExtension.setUpdatePeriod(86400); // in seconds, 24h

        networkManager.addExtension(discoveryExtension);
        networkManager.addExtension(new ZigBeeOtaUpgradeExtension());
        networkManager.addExtension(new ZigBeeIasCieExtension());

        // Register clusters

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

        // Configure network

        networkManager.setDefaultProfileId(ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.getKey());
        networkManager.setZigBeeNetworkKey(ZigBeeKey.createRandom());//new ZigBeeKey("552FAAF9B5F49E75F1ADDA12215C2CA1")); // ZigBeeKey.createRandom();
        networkManager.setZigBeeLinkKey(new ZigBeeKey(new int[] { // Add the default ZigBeeAlliance09 HA link key
                0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39 }));
        networkManager.setZigBeeChannel(ZigBeeChannel.create(11));
        networkManager.setZigBeePanId(65534); // (int) Math.floor((Math.random() * 65534));
        networkManager.setZigBeeExtendedPanId(new ExtendedPanId("00124B001CCE1B5F")); // ExtendedPanId.createRandom();

        //transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_JOIN_MODE, TrustCentreJoinMode.TC_JOIN_INSECURE); // TC_JOIN_SECURE
        dongle.updateTransportConfig(transportOptions);

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

    // ------------------------------------------
    // Zigbee Node Methods
    // ------------------------------------------

    @Override
    public void deviceStatusUpdate(ZigBeeNodeStatus deviceStatus, Integer networkAddress, IeeeAddress ieeeAddress) {
        logger.fine("New device connected to network: " + ieeeAddress + "(" + deviceStatus + ")");
    }

    @Override
    public void announceUnknownDevice(Integer networkAddress) {
        logger.fine("Unknown device connected to network: " + networkAddress);
    }

    @Override
    public void nodeAdded(final ZigBeeNode node) {
        nodeUpdated(node);
    }

    @Override
    public void nodeUpdated(final ZigBeeNode node) {
        // If this is the coordinator (NWK address 0), ignore this device
        if (node.getLogicalType() == NodeDescriptor.LogicalType.COORDINATOR || node.getNetworkAddress() == 0) {
            logger.fine("[Node: " + node.getIeeeAddress() + "]: Node is coordinator, ignoring registration.");
            return;
        }

        if (!node.isDiscovered()) {
            logger.fine("[Node: " + node.getIeeeAddress() + "]: Node discovery not complete, ignoring registration.");
            return;
        }

        // Perform the device properties discovery.

        node.removeNetworkEndpointListener(this);
        node.addNetworkEndpointListener(this);
        logger.fine("[Node: " + node.getIeeeAddress() + "]: Node has been registered: " +
                "Manufacturer=" + node.getNodeDescriptor().getManufacturerCode() +
                ", Type=" + node.getNodeDescriptor().getLogicalType());
    }

    @Override
    public void nodeRemoved(final ZigBeeNode node) {
        node.removeNetworkEndpointListener(this);
        logger.fine("[Node: " + node.getIeeeAddress() + "]: Node registration has been removed.");
    }

    // ------------------------------------------
    // Zigbee Endpoint Methods
    // ------------------------------------------

    @Override
    public void deviceAdded(ZigBeeEndpoint endpoint) {
        deviceUpdated(endpoint);
    }

    @Override
    public void deviceUpdated(ZigBeeEndpoint endpoint) {
        logger.fine("[Node: " + endpoint.getIeeeAddress() + ", Endpoint: " + endpoint.getEndpointId() + "]: Received a Zigbee endpoint update: " + endpoint);

        for (int inputClusterId : endpoint.getInputClusterIds()) {
            ZigbeeHalDeviceConfig config = createDeviceConfig(inputClusterId);

            if (config != null) {
                registerCluster(endpoint, config);
            } else {
                logger.finest("[Node: " + endpoint.getIeeeAddress() + "] Cluster ID '" + inputClusterId + "' is not supported.");
            }
        }
    }

    private ZigbeeHalDeviceConfig createDeviceConfig(int clusterId) {
        switch (clusterId) {
            case ZclRelativeHumidityMeasurementCluster.CLUSTER_ID: return new ZigbeeHumidityConfig();
            case ZclOnOffCluster.CLUSTER_ID:                       return new ZigbeeOnOffConfig();
            case ZclPressureMeasurementCluster.CLUSTER_ID:         return new ZigbeePressureConfig();
            case ZclTemperatureMeasurementCluster.CLUSTER_ID:      return new ZigbeeTemperatureConfig();
        }

        return null;
    }

    private void registerCluster(final ZigBeeEndpoint endpoint, ZigbeeHalDeviceConfig config) {
        final ZclCluster cluster = endpoint.getInputCluster(config.getZigbeeClusterId());
        if (cluster != null) {
            config.setZigbeeNodeAddress(endpoint.getIeeeAddress());

            cluster.addAttributeListener(new ZclAttributeListener() {
                @Override
                public void attributeUpdated(ZclAttribute attribute, Object value) {
                    for (HalDeviceReportListener deviceListener : deviceListeners) {
                        logger.finer("[Node: " + endpoint.getIeeeAddress() + ", Endpoint: " + endpoint.getEndpointId() + ", Cluster: " +  attribute.getCluster().getId() + "] Attribute updated: attribute_name=" + attribute.getName() + ", value=" + attribute.getLastValue());
                        deviceListener.reportReceived(config, config.getDeviceData(attribute));
                    }
                }
            });

            // // TODO: Notify listener that a device is online
            for (HalDeviceReportListener deviceListener : deviceListeners) {
                deviceListener.reportReceived(config, null);
            }
        }
    }

    @Override
    public void deviceRemoved(ZigBeeEndpoint endpoint) {
        logger.fine("[Node: " + endpoint.getIeeeAddress() + ", Endpoint: " + endpoint.getEndpointId() + "]: Endpoint removed: " + endpoint);
    }

    // ------------------------------------------
    // Hal Overrides
    // ------------------------------------------

    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof ZigbeeHalDeviceConfig && !registeredDevices.contains(deviceConfig)) {
            ZigbeeHalDeviceConfig zigbeeDevice = (ZigbeeHalDeviceConfig) deviceConfig;
            registeredDevices.add(zigbeeDevice);
        }
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
        if (eventConfig instanceof ZigbeeHalEventDeviceConfig) {
            ((ZigbeeHalEventDeviceConfig) eventConfig).sendZigbeeCommand(networkManager, eventData);
        }
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        deviceListeners.add(listener);
    }

    @Override
    public void startScan() {
        logger.info("Starting Zigbee pairing process.");

        networkManager.permitJoin(120);
        permitJoinTimer = new Timer(120_000);
        permitJoinTimer.start();
    }

    @Override
    public boolean isScanning() {
        return permitJoinTimer != null && !permitJoinTimer.hasTimedOut();
    }
}
