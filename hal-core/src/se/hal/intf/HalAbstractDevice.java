package se.hal.intf;

import se.hal.HalContext;
import se.hal.struct.Room;
import se.hal.struct.User;
import se.hal.util.HalDeviceChangeListener;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.log.LogUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;
import zutil.ui.conf.Configurator;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains logic and data common to devices (Events and Sensors)
 *
 * @param   <V>     is the device type
 * @param   <C>     is the device configuration class
 * @param   <D>     is the device data class
 */
public abstract class HalAbstractDevice<V extends HalAbstractDevice, C extends HalDeviceConfig, D extends HalDeviceData> extends DBBean {
    private static final Logger logger = LogUtil.getLogger();

    // Sensor specific data
    private String name;
    private String type;
    private String config; // only used to store the deviceConfig configuration in DB

    /** Sensor specific configuration **/
    private transient C deviceConfig;
    /** latest device data received **/
    private transient D deviceData;

    // User configuration
    @DBColumn("user_id")
    private User user;

    @DBColumn("room_id")
    private Room room;

    // UI variables
    @DBColumn("map_x")
    private double mapX = 0;
    @DBColumn("map_y")
    private double mapY = 0;

    protected transient List<HalDeviceReportListener> deviceListeners = new LinkedList<>();


    // ----------------------------------------------------
    // Device config methods
    // ----------------------------------------------------

    public Configurator<C> getDeviceConfigurator() {
        C obj = getDeviceConfig();
        if (obj != null) {
            HalDeviceChangeListener<C> listener = new HalDeviceChangeListener<>();

            Configurator<C> configurator = new Configurator<>(obj);
            configurator.setPreConfigurationListener(listener);
            configurator.setPostConfigurationListener(listener);
            return configurator;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public C getDeviceConfig() {
        if (deviceConfig == null || !deviceConfig.getClass().getName().equals(type)) {
            try {
                Class clazz = Class.forName(type);
                deviceConfig = (C) clazz.getDeclaredConstructor().newInstance();

                applyConfig();
                deviceData = getLatestDeviceData(HalContext.getDB());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable instantiate HalDeviceConfig: " + type, e);
            }
        }
        return deviceConfig;
    }

    /**
     * Will replace the current device configuration.
     * The device configuration will be reset if the input is set as null.
     */
    public void setDeviceConfig(C data) {
        if (data != null) {
            type = data.getClass().getName();
            deviceConfig = data;
            deviceData = getLatestDeviceData(HalContext.getDB());
        } else {
            deviceConfig = null;
            deviceData = null;
            type = null;
            config = null;
        }
    }

    @Override
    public void save(DBConnection db) throws SQLException {
        if (deviceConfig != null)
            updateConfigString();
        else
            this.config = null;
        super.save(db);
    }

    /**
     * Will update the config String that will be stored in DB.
     */
    private void updateConfigString() {
        Configurator<C> configurator = getDeviceConfigurator();
        this.config = JSONWriter.toString(configurator.getValuesAsNode());
    }
    /**
     * This method will configure the current DeviceData with the
     * configuration from the config String.
     */
    private void applyConfig() {
        if (config != null && !config.isEmpty()) {
            Configurator<C> configurator = getDeviceConfigurator();
            configurator.setValues(JSONParser.read(config));
            configurator.applyConfiguration();
        }
    }

    /**
     * @return the class of the Controller responsible for this device type.
     */
    public Class<? extends HalAbstractController> getControllerClass() {
        return getDeviceConfig().getDeviceControllerClass();
    }

    // ----------------------------------------------------
    // Device data methods
    // ----------------------------------------------------

    /**
     * @return the latest known data from the device
     */
    public D getDeviceData() {
        if (deviceData == null)
            deviceData = getLatestDeviceData(HalContext.getDB());
        return deviceData;
    }

    public void setDeviceData(D deviceData) {
        this.deviceData = deviceData;
    }

    /**
     * Reads latest device data from DB
     */
    protected abstract D getLatestDeviceData(DBConnection db);

    // ----------------------------------------------------
    // Other methods
    // ----------------------------------------------------

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return a String containing the class name of the DeviceData
     */
    public String getType() {
        return type;
    }

    /**
     * Will set the DeviceData class type. This method will
     * reset set the current DeviceData if the input type is
     * null or a different type from the current DeviceData class.
     */
    public void setType(String type) {
        if (this.type == null || !this.type.equals(type)) {
            setDeviceConfig(null); // reset
            this.type = type;
        }
    }

    /**
     * @return the owner of this device.
     */
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the room that this device is located in.
     */
    public Room getRoom() {
        return room;
    }
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * @return the X coordinate of this device on the floor map
     */
    public double getMapX() {
        return mapX;
    }
    /**
     * @return the Y coordinate of this device on the floor map
     */
    public double getMapY() {
        return mapY;
    }
    public void setMapCoordinates(double x, double y) {
        this.mapX = x;
        this.mapY = y;
    }

    public void addReportListener(HalDeviceReportListener listener) {
        deviceListeners.add(listener);
    }
    public void removeReportListener(HalDeviceReportListener listener) {
        deviceListeners.remove(listener);
    }
    public List<HalDeviceReportListener> getReportListeners() {
        return deviceListeners;
    }

    /**
     * @return a DataNode object containing general Device information that can be written in JSON format.
     */
    public DataNode getDataNode() {
        DataNode deviceNode = new DataNode(DataNode.DataType.Map);
        deviceNode.set("id", getId());
        deviceNode.set("name", getName());
        deviceNode.set("user", getUser().getUsername());

        DataNode mapNode = deviceNode.set("map", DataNode.DataType.Map);
        mapNode.set("x", getMapX());
        mapNode.set("y", getMapY());

        if (getDeviceConfig() != null) {
            DataNode configNode = deviceNode.set("config", DataNode.DataType.Map);
            configNode.set("typeConfig", getDeviceConfig().getClass().getSimpleName());
            configNode.set("typeData", getDeviceConfig().getDeviceDataClass().getSimpleName());

            for (Configurator.ConfigurationParam param : getDeviceConfigurator().getConfiguration()) {
                configNode.set(param.getName(), param.getString());
            }
        }

        if (getDeviceData() != null) {
            DataNode dataNode = deviceNode.set("data", DataNode.DataType.Map);
            dataNode.set("value", getDeviceData().getData());
            dataNode.set("valueStr", getDeviceData().toString());
            dataNode.set("timestamp", getDeviceData().getTimestamp());
        }

        return deviceNode;
    }
}
