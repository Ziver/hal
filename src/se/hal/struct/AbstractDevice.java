package se.hal.struct;

import se.hal.ControllerManager;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.log.LogUtil;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;
import zutil.ui.Configurator;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2016-01-15.
 */
public abstract class AbstractDevice<T> extends DBBean {
    private static final Logger logger = LogUtil.getLogger();

    // Sensor specific data
    private String name;
    private String type;
    private String config; // only used to store the deviceConfig configuration in DB

    /** Sensor specific configuration **/
    private transient T deviceConfig;
    /** latest device data received **/
    private transient Object latestDeviceData;

    // User configuration
    @DBColumn("user_id")
    private User user;

    // UI variables
    @DBColumn("map_x")
    private double x;
    @DBColumn("map_y")
    private double y;



    public Configurator<T> getDeviceConfigurator() {
        T obj = getDeviceConfig();
        if (obj != null) {
            Configurator<T> configurator = new Configurator<>(obj);
            configurator.setPreConfigurationListener(ControllerManager.getInstance());
            configurator.setPostConfigurationListener(ControllerManager.getInstance());
            return configurator;
        }
        return null;
    }
    public T getDeviceConfig() {
        if (deviceConfig == null || !deviceConfig.getClass().getName().equals(type)) {
            try {
                Class c = Class.forName(type);
                deviceConfig = (T) c.newInstance();

                applyConfig();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable instantiate DeviceData: "+type, e);
            }
        }
        return deviceConfig;
    }

    /**
     * Will replace the current DeviceData.
     * And the current config will be applied on the new DeviceData.
     * DeviceData will be reset if the input is set as null.
     */
    public void setDeviceConfig(T data) {
        if(data != null) {
            deviceConfig = data;
            type = data.getClass().getName();
            applyConfig(); // TODO: this is a bit clunky, should probably be solved in another way
        } else {
            deviceConfig = null;
            type = null;
            config = null;
        }
    }

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
    protected void updateConfigString() {
        Configurator<T> configurator = getDeviceConfigurator();
        this.config = JSONWriter.toString(configurator.getValuesAsNode());
    }
    /**
     * This method will configure the current DeviceData with the
     * configuration from the config String.
     */
    protected void applyConfig(){
        if (config != null && !config.isEmpty()) {
            Configurator<T> configurator = getDeviceConfigurator();
            configurator.setValues(JSONParser.read(config));
            configurator.applyConfiguration();
        }
    }



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
            this.type = type;
            this.config = null;
            this.deviceConfig = null; // invalidate current sensor data object
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

}
