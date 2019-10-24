package se.hal.plugin.zwave;

import org.zwave4j.*;
import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.plugin.tellstick.TellstickDevice;
import zutil.log.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @author zagumennikov
 */
public class HalZWaveController implements HalSensorController, HalEventController, HalAutoScannableController, NotificationWatcher{
    private static final Logger logger = LogUtil.getLogger();

    public static final String CONFIG_ZWAVE_PORT = "zwave.com_port";
    public static final String CONFIG_ZWAVE_CFG_PATH = "zwave.cfg_path";

    private String serialPort;
    private long homeId;

    private Options options;
    private Manager manager;

    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;
    private List<TellstickDevice> registeredDevices;


    public static void main(String[] args) throws IOException {
        HalZWaveController controller = new HalZWaveController();
        controller.initialize(
                "/dev/serial/by-id/usb-0658_0200-if00",
                "./");

        System.in.read();
    }

    public HalZWaveController() {
        NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);
    }

    @Override
    public boolean isAvailable() {
        return HalContext.getStringProperty(CONFIG_ZWAVE_PORT) != null &&
                HalContext.getStringProperty(CONFIG_ZWAVE_CFG_PATH) != null;
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
        manager.removeWatcher(this, null);
        manager.removeDriver(serialPort);
        manager.destroy();
	    Options.destroy();
    }


    @Override
    public void onNotification(Notification notification, Object context) {
        switch (notification.getType()) {
            case DRIVER_READY:
                System.out.println(String.format("Driver ready\n" +
                        "\thome id: %d",
                        notification.getHomeId()
                ));
                homeId = notification.getHomeId();
                break;
            case DRIVER_FAILED:
                System.out.println("Driver failed");
                break;
            case DRIVER_RESET:
                System.out.println("Driver reset");
                break;
            case AWAKE_NODES_QUERIED:
                System.out.println("Awake nodes queried");
                break;
            case ALL_NODES_QUERIED_SOME_DEAD:
                System.out.println("Some Nodes are dead");
            case ALL_NODES_QUERIED:
                System.out.println("Finished querying nodes");
                manager.writeConfig(homeId);
                // Controller is done initializing
                break;
            case POLLING_ENABLED:
                System.out.println("Polling enabled");
                break;
            case POLLING_DISABLED:
                System.out.println("Polling disabled");
                break;
            case NODE_NEW:
                System.out.println(String.format("Node new\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
                break;
            case NODE_ADDED:
                System.out.println(String.format("Node added\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
                break;
            case NODE_REMOVED:
                System.out.println(String.format("Node removed\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
                break;
            case ESSENTIAL_NODE_QUERIES_COMPLETE:
                System.out.println(String.format("Node essential queries complete\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
                break;
            case NODE_QUERIES_COMPLETE:
                System.out.println(String.format("Node queries complete\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
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
                System.out.println(String.format("Node naming\n" +
                        "\tnode id: %d",
                        notification.getNodeId()
                ));
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
                System.out.println("Notification");
                break;
            default:
                System.out.println(notification.getType().name());
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