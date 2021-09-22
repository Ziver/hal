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
import se.hal.plugin.zigbee.db.ZigBeeHalDataStore;
import se.hal.plugin.zigbee.device.*;
import zutil.Timer;
import zutil.log.LogUtil;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller that will connect to a Zigbee USB coordinator.
 */
public class ZigbeeController implements HalSensorController,
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

    private static final String CONFIG_ZIGBEE_DONGLE          = "hal_zigbee.dongle";
    private static final String CONFIG_ZIGBEE_PORT            = "hal_zigbee.com_port";
    private static final String CONFIG_ZIGBEE_NETWORK_CHANNEL = "hal_zigbee.network.channel";
    private static final String CONFIG_ZIGBEE_NETWORK_PANID   = "hal_zigbee.network.panid";
    private static final String CONFIG_ZIGBEE_NETWORK_EPANID  = "hal_zigbee.network.epanid";
    private static final String CONFIG_ZIGBEE_NETWORK_NWKKEY  = "hal_zigbee.network.nwkey";
    private static final String CONFIG_ZIGBEE_NETWORK_LINKKEY = "hal_zigbee.network.linkkey";

    private ZigBeePort serialPort;
    protected ZigBeeNetworkManager networkManager;

    private Timer permitJoinTimer;
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private List<ZigbeeHalDeviceConfig> registeredDevices = new ArrayList<>();


    public ZigbeeController() {}

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
        TransportConfig transportOptions = new TransportConfig();

        // ----------------------------
        // Initialize Transport Network
        // ----------------------------

        ZigBeeTransportTransmit dongle = getDongle(dongleName, serialPort, transportOptions);
        networkManager = new ZigBeeNetworkManager(dongle);
        networkManager.setNetworkDataStore(new ZigBeeHalDataStore(HalContext.getDB()));
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

        // Prepare defaults

        if (!HalContext.containsProperty(CONFIG_ZIGBEE_NETWORK_CHANNEL))
            HalContext.setProperty(CONFIG_ZIGBEE_NETWORK_CHANNEL, "11");
        if (!HalContext.containsProperty(CONFIG_ZIGBEE_NETWORK_PANID))
            HalContext.setProperty(CONFIG_ZIGBEE_NETWORK_PANID, "" + (int) Math.floor((Math.random() * 65534)));
        if (!HalContext.containsProperty(CONFIG_ZIGBEE_NETWORK_EPANID))
            HalContext.setProperty(CONFIG_ZIGBEE_NETWORK_EPANID, "" + ExtendedPanId.createRandom());
        if (!HalContext.containsProperty(CONFIG_ZIGBEE_NETWORK_NWKKEY))
            HalContext.setProperty(CONFIG_ZIGBEE_NETWORK_NWKKEY, "" + ZigBeeKey.createRandom());
        if (!HalContext.containsProperty(CONFIG_ZIGBEE_NETWORK_LINKKEY))
            HalContext.setProperty(CONFIG_ZIGBEE_NETWORK_LINKKEY, "" + new ZigBeeKey(new int[] {
                    0x5A, 0x69, 0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39 })); // Add the default ZigBeeAlliance09 HA link key

        // Configure network

        networkManager.setZigBeeChannel(ZigBeeChannel.create(HalContext.getIntegerProperty(CONFIG_ZIGBEE_NETWORK_CHANNEL, 11)));
        networkManager.setZigBeePanId(HalContext.getIntegerProperty(CONFIG_ZIGBEE_NETWORK_PANID));
        networkManager.setZigBeeExtendedPanId(new ExtendedPanId(HalContext.getStringProperty(CONFIG_ZIGBEE_NETWORK_EPANID)));
        networkManager.setZigBeeNetworkKey(new ZigBeeKey(HalContext.getStringProperty(CONFIG_ZIGBEE_NETWORK_NWKKEY)));
        networkManager.setZigBeeLinkKey(new ZigBeeKey(HalContext.getStringProperty(CONFIG_ZIGBEE_NETWORK_LINKKEY)));
        networkManager.setDefaultProfileId(ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.getKey());
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
    // Getters
    // ------------------------------------------

    public ZigBeeChannel getZigbeeChannel() {
        return networkManager.getZigBeeChannel();
    }
    public int getZigbeePanId() {
        return networkManager.getZigBeePanId();
    }
    public ExtendedPanId getZigbeeExtendedPanId() {
        return networkManager.getZigBeeExtendedPanId();
    }

    public ZigBeeNode getNode(IeeeAddress address) {
        return networkManager.getNode(address);
    }
    public Set<ZigBeeNode> getNodes() {
        return networkManager.getNodes();
    }
    public List<ZigbeeHalDeviceConfig> getRegisteredDevices() {
        return registeredDevices;
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

        for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
            deviceAdded(endpoint);
        }
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
            ZclCluster cluster = endpoint.getInputCluster(inputClusterId);
            ZigbeeHalDeviceConfig config = createDeviceConfig(inputClusterId);

            // Read basic attributes
            if (cluster instanceof ZclBasicCluster) {
                try {
                    cluster.readAttributes(Arrays.asList(
                            ZclBasicCluster.ATTR_MANUFACTURERNAME,
                            ZclBasicCluster.ATTR_MODELIDENTIFIER,
                            ZclBasicCluster.ATTR_HWVERSION,
                            ZclBasicCluster.ATTR_APPLICATIONVERSION,
                            ZclBasicCluster.ATTR_STACKVERSION,
                            ZclBasicCluster.ATTR_ZCLVERSION,
                            ZclBasicCluster.ATTR_DATECODE
                    )).get();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Was unable to read basic device information.", e);
                }
            }
            // Handle specific node attributes
            else if (cluster != null && config != null) {
                config.setZigbeeNodeAddress(endpoint.getIeeeAddress());
                config.initialize(cluster);

                cluster.addAttributeListener(new ZclAttributeListener() {
                    @Override
                    public void attributeUpdated(ZclAttribute attribute, Object value) {
                        logger.finer("[Node: " + endpoint.getIeeeAddress() + ", Endpoint: " + endpoint.getEndpointId() + ", Cluster: " +  attribute.getCluster().getId() + "] Attribute " + config.getClass().getSimpleName() + " updated: id=" + attribute.getId() + ", attribute_name=" + attribute.getName() + ", value=" + attribute.getLastValue());

                        HalDeviceData data = config.getDeviceData(attribute);
                        if (data != null) {
                            for (HalDeviceReportListener deviceListener : deviceListeners) {
                                deviceListener.reportReceived(config, data);
                            }
                        }
                    }
                });

                // Notify listener that a device is online
                for (HalDeviceReportListener deviceListener : deviceListeners) {
                    deviceListener.reportReceived(config, null);
                }
            } else {
                logger.finest("[Node: " + endpoint.getIeeeAddress() + ", Endpoint: " + endpoint.getEndpointId() + "] Cluster ID '" + inputClusterId + "' is not supported.");
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
            ((ZigbeeHalEventDeviceConfig) eventConfig).sendZigbeeCommand(this, eventData);
        }
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }

    @Override
    public void startScan() {
        logger.info("Starting Zigbee pairing process.");

        networkManager.permitJoin(120);
        permitJoinTimer = new Timer(120_000).start();
    }

    @Override
    public boolean isScanning() {
        return permitJoinTimer != null && !permitJoinTimer.hasTimedOut();
    }
}
