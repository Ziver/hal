package se.hal.plugin.zwave;

import org.zwave4j.*;
import se.hal.HalContext;
import se.hal.intf.*;
import zutil.log.LogUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 *
 * <p>Reference: http://www.openzwave.com/dev/namespaceOpenZWave.html
 *
 * @author Ziver Koc
 */
public class HalZWaveController implements HalSensorController, HalEventController, HalAutostartController, NotificationWatcher {
    private static final Logger logger = LogUtil.getLogger();

    public static final String CONFIG_ZWAVE_PORT     = "hal_zwave.com_port";
    public static final String CONFIG_ZWAVE_CFG_PATH = "hal_zwave.cfg_path";

    private String serialPort;
    private long homeId;

    private Options options;
    private Manager manager;

    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private List<HalAbstractDevice> registeredDevices;


    public HalZWaveController() {
        NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
    }


    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZWAVE_PORT) &&
                HalContext.containsProperty(CONFIG_ZWAVE_CFG_PATH);
    }
    @Override
    public void initialize() {
        initialize(HalContext.getStringProperty(CONFIG_ZWAVE_PORT),
                HalContext.getStringProperty(CONFIG_ZWAVE_CFG_PATH));
    }
    public void initialize(String comPort, String configDir) {
        options = Options.create(configDir, "", "");
        options.addOptionBool("ConsoleOutput", false);
        options.lock();

        logger.info("Creating OpenZWave Manager...");
        serialPort = comPort;
        manager = Manager.create();
        manager.addWatcher(this, null);

        logger.info("Connecting to com port... ("+ serialPort +")");
        manager.addDriver(serialPort);
    }

    @Override
    public void close() {
        logger.info("Shutting down OpenZWave Manager...");
        manager.removeWatcher(this, null);
        manager.removeDriver(serialPort);
        Manager.destroy();
        Options.destroy();
    }


    // --------------------------
    // OpenZWave Overrides
    // --------------------------

    @Override
    public void onNotification(Notification notification, Object context) {
        switch (notification.getType()) {
            case DRIVER_READY:
                homeId = notification.getHomeId();
                logger.info("Driver ready (Home ID: " + homeId + ").");
                break;
            case DRIVER_FAILED:
                logger.info("Driver failed.");
                break;
            case DRIVER_RESET:
                logger.info("Driver reset.");
                break;
            case AWAKE_NODES_QUERIED:
                logger.info("Controller is done initializing, All awake nodes queried.");
                manager.writeConfig(homeId);
                break;
            case ALL_NODES_QUERIED_SOME_DEAD:
                logger.info("Controller is done initializing, All nodes queried but some are dead.");
                manager.writeConfig(homeId);
                break;
            case ALL_NODES_QUERIED:
                logger.info("Controller is done initializing, All nodes queried.");
                manager.writeConfig(homeId);
                break;

            case POLLING_ENABLED:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Polling enabled.");
                break;
            case POLLING_DISABLED:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Polling disabled.");
                break;
            case NODE_NEW:
                logger.fine("[NodeID: " + notification.getNodeId() + "] New node detected.");
                break;
            case NODE_ADDED:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Node registered to controller.");
                break;
            case NODE_REMOVED:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Node unregistered from controller.");
                break;
            case ESSENTIAL_NODE_QUERIES_COMPLETE:
                logger.finest("[NodeID: " + notification.getNodeId() + "] Essential node queries complete.");
                break;
            case NODE_QUERIES_COMPLETE:
                logger.finest("[NodeID: " + notification.getNodeId() + "] Node queries complete.");
                break;
            case NODE_EVENT:
                System.out.println(String.format("Node event\n" +
                        "\tnode id: %d\n" +
                        "\tevent id: %d",
                        notification.getNodeId(),
                        notification.getEvent()
                ));
                break;
            case NODE_NAMING:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Node identified as: " +
                        manager.getNodeManufacturerName(notification.getHomeId(), notification.getNodeId()) + ", " +
                        manager.getNodeProductName(notification.getHomeId(), notification.getNodeId())
                        );
                break;
            case NODE_PROTOCOL_INFO:
                System.out.println(String.format("Node protocol info\n" +
                        "\tnode id: %d\n" +
                        "\ttype: %s",
                        notification.getNodeId(),
                        manager.getNodeType(notification.getHomeId(), notification.getNodeId())
                ));
                break;
            case VALUE_ADDED:
                System.out.println(String.format("Value added\n" +
                        "\tnode id: %d\n" +
                        "\tcommand class: %d\n" +
                        "\tinstance: %d\n" +
                        "\tindex: %d\n" +
                        "\tgenre: %s\n" +
                        "\ttype: %s\n" +
                        "\tlabel: %s\n" +
                        "\tvalue: %s",
                        notification.getNodeId(),
                        notification.getValueId().getCommandClassId(),
                        notification.getValueId().getInstance(),
                        notification.getValueId().getIndex(),
                        notification.getValueId().getGenre().name(),
                        notification.getValueId().getType().name(),
                        manager.getValueLabel(notification.getValueId()),
                        getValue(notification.getValueId())
                ));
                break;
            case VALUE_REMOVED:
                System.out.println(String.format("Value removed\n" +
                        "\tnode id: %d\n" +
                        "\tcommand class: %d\n" +
                        "\tinstance: %d\n" +
                        "\tindex: %d",
                        notification.getNodeId(),
                        notification.getValueId().getCommandClassId(),
                        notification.getValueId().getInstance(),
                        notification.getValueId().getIndex()
                ));
                break;
            case VALUE_CHANGED:
                System.out.println(String.format("Value changed\n" +
                        "\tnode id: %d\n" +
                        "\tcommand class: %d\n" +
                        "\tinstance: %d\n" +
                        "\tindex: %d\n" +
                        "\tvalue: %s",
                        notification.getNodeId(),
                        notification.getValueId().getCommandClassId(),
                        notification.getValueId().getInstance(),
                        notification.getValueId().getIndex(),
                        getValue(notification.getValueId())
                ));
                break;
            case VALUE_REFRESHED:
                System.out.println(String.format("Value refreshed\n" +
                        "\tnode id: %d\n" +
                        "\tcommand class: %d\n" +
                        "\tinstance: %d\n" +
                        "\tindex: %d" +
                        "\tvalue: %s",
                        notification.getNodeId(),
                        notification.getValueId().getCommandClassId(),
                        notification.getValueId().getInstance(),
                        notification.getValueId().getIndex(),
                        getValue(notification.getValueId())
                ));
                break;
            case GROUP:
                System.out.println(String.format("Group\n" +
                        "\tnode id: %d\n" +
                        "\tgroup id: %d",
                        notification.getNodeId(),
                        notification.getGroupIdx()
                ));
                break;

            case SCENE_EVENT:
                System.out.println(String.format("Scene event\n" +
                        "\tscene id: %d",
                        notification.getSceneId()
                ));
                break;
            case CREATE_BUTTON:
                System.out.println(String.format("Button create\n" +
                        "\tbutton id: %d",
                        notification.getButtonId()
                ));
                break;
            case DELETE_BUTTON:
                System.out.println(String.format("Button delete\n" +
                        "\tbutton id: %d",
                        notification.getButtonId()
                ));
                break;
            case BUTTON_ON:
                System.out.println(String.format("Button on\n" +
                        "\tbutton id: %d",
                        notification.getButtonId()
                ));
                break;
            case BUTTON_OFF:
                System.out.println(String.format("Button off\n" +
                        "\tbutton id: %d",
                        notification.getButtonId()
                ));
                break;
            case NOTIFICATION:
                logger.fine("[NodeID: " + notification.getNodeId() + "] Received notification, will query dynamic state of node.");
                //if (!manager.requestNodeDynamic(notification.getHomeId(), notification.getNodeId()))
                //    logger.fine("[NodeID: " + notification.getNodeId() + "] Requesting dynamic state failed.");
                break;
            default:
                logger.warning("Unknown notification type: " + notification.getType().name());
                break;
        }
    }

    private static Object getValue(ValueId valueId) {
        switch (valueId.getType()) {
            case BOOL:
                AtomicReference<Boolean> b = new AtomicReference<>();
                Manager.get().getValueAsBool(valueId, b);
                return b.get();
            case BYTE:
                AtomicReference<Short> bb = new AtomicReference<>();
                Manager.get().getValueAsByte(valueId, bb);
                return bb.get();
            case DECIMAL:
                AtomicReference<Float> f = new AtomicReference<>();
                Manager.get().getValueAsFloat(valueId, f);
                return f.get();
            case INT:
                AtomicReference<Integer> i = new AtomicReference<>();
                Manager.get().getValueAsInt(valueId, i);
                return i.get();
            case LIST:
                return null;
            case SCHEDULE:
                return null;
            case SHORT:
                AtomicReference<Short> s = new AtomicReference<>();
                Manager.get().getValueAsShort(valueId, s);
                return s.get();
            case STRING:
                AtomicReference<String> ss = new AtomicReference<>();
                Manager.get().getValueAsString(valueId, ss);
                return ss.get();
            case BUTTON:
                return null;
            case RAW:
                AtomicReference<short[]> sss = new AtomicReference<>();
                Manager.get().getValueAsRaw(valueId, sss);
                return sss.get();
            default:
                return null;
        }
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
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }

}
