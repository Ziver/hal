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
 * Created by ezivkoc on 2016-01-15.
 */
public abstract class AbstractDevice<T> extends DBBean {
    private static final Logger logger = LogUtil.getLogger();

    // Sensor specific data
    private String name;
    private String type;
    private String config; // only used to store the deviceData configuration in DB

    // Sensor specific data
    private transient T deviceData;

    // User configuration
    @DBColumn("user_id")
    private User user;


    public Configurator<T> getDeviceConfig() {
        T obj = getDeviceData();
        if (obj != null) {
            Configurator<T> configurator = new Configurator<>(obj);
            configurator.setPreConfigurationListener(ControllerManager.getInstance());
            configurator.setPostConfigurationListener(ControllerManager.getInstance());
            return configurator;
        }
        return null;
    }
    public T getDeviceData() {
        if (deviceData == null) {
            try {
                Class c = Class.forName(type);
                deviceData = (T) c.newInstance();

                if (config != null && !config.isEmpty()) {
                    Configurator<T> configurator = getDeviceConfig();
                    configurator.setValues(JSONParser.read(config));
                    configurator.applyConfiguration();
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read device data", e);
            }
        }
        return deviceData;
    }
    public void setDeviceData(T data) {
        this.deviceData = data;
        if(data != null)
            type = data.getClass().getName();
        updateConfigString();
    }
    public void save(DBConnection db) throws SQLException {
        if (deviceData != null)
            updateConfigString();
        else
            this.config = null;
        super.save(db);
    }
    protected void updateConfigString() {
        Configurator<T> configurator = getDeviceConfig();
        this.config = JSONWriter.toString(configurator.getValuesAsNode());
    }



    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        if (this.type == null || !this.type.equals(type)) {
            this.type = type;
            this.deviceData = null; // invalidate current sensor data object
        }
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
