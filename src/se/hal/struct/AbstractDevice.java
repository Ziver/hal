package se.hal.struct;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.log.LogUtil;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;
import zutil.ui.Configurator;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains logic and data common to devices (Events and Sensors)
 *
 * @param   <T>     is the device type
 * @param   <C>     is the device configuration class
 * @param   <D>     is the device data class
 */
public abstract class AbstractDevice<T extends AbstractDevice, C,D extends HalDeviceData> extends DBBean {
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

    // UI variables
    @DBColumn("map_x")
    private double x;
    @DBColumn("map_y")
    private double y;

    protected transient List<HalDeviceReportListener<T>> listeners = new LinkedList<>();


    /**************** DEVICE CONFIG ******************/

    public Configurator<C> getDeviceConfigurator() {
        C obj = getDeviceConfig();
        if (obj != null) {
            Configurator<C> configurator = new Configurator<>(obj);
            configurator.setPreConfigurationListener(ControllerManager.getInstance());
            configurator.setPostConfigurationListener(ControllerManager.getInstance());
            return configurator;
        }
        return null;
    }
    public C getDeviceConfig() {
        if (deviceConfig == null || !deviceConfig.getClass().getName().equals(type)) {
            try {
                Class c = Class.forName(type);
                deviceConfig = (C) c.newInstance();

                applyConfig();
                deviceData = getLatestDeviceData(HalContext.getDB());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable instantiate DeviceConfig: "+type, e);
            }
        }
        return deviceConfig;
    }

    /**
     * Will replace the current DeviceData.
     * And the current config will be applied on the new DeviceData.
     * DeviceData will be reset if the input is set as null.
     */
    public void setDeviceConfig(C data) {
        if(data != null) {
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
    private void applyConfig(){
        if (config != null && !config.isEmpty()) {
            Configurator<C> configurator = getDeviceConfigurator();
            configurator.setValues(JSONParser.read(config));
            configurator.applyConfiguration();
        }
    }

    public abstract Class<?> getController();

    /**************** DEVICE DATA ******************/

    /**
     * @return the latest known data from the device
     */
    public D getDeviceData(){
        return deviceData;
    }

    public void setDeviceData(D latest){
        this.deviceData = latest;
    }

    /**
     * Reads latest device data from DB
     */
    protected abstract D getLatestDeviceData(DBConnection db);

    /**************** OTHER ******************/

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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public void addReportListener(HalDeviceReportListener<T> listener){
        listeners.add(listener);
    }
    public void removeReportListener(HalDeviceReportListener<T> listener){
        listeners.remove(listener);
    }
    public List<HalDeviceReportListener<T>> getReportListeners(){
        return listeners;
    }
}
