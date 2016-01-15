package se.hal.struct;

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
    private String config;

    // Sensor specific data
    private transient T deviceData;

    // User configuration
    @DBColumn("user_id")
    private User user;



    public T getDeviceData() {
        if (config != null && deviceData == null) {
            try {
                Class c = Class.forName(type);
                deviceData = (T) c.newInstance();

                Configurator<T> configurator = new Configurator<>(deviceData);
                configurator.setValues(JSONParser.read(config));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to read device data", e);
            }
        }
        return deviceData;
    }

    public void setDeviceData(T data) {
        this.deviceData = data;
        updateConfig();
    }


    public void setConfig(String config) {
        if (this.config == null || !this.config.equals(config)) {
            this.config = config;
            this.deviceData = null; // invalidate current sensor data object
        }
    }

    protected void updateConfig() {
        Configurator<T> configurator = new Configurator<>(deviceData);
        this.config = JSONWriter.toString(configurator.getValuesAsNode());
    }

    public String getConfig() {
        return config;
    }


    public void save(DBConnection db) throws SQLException {
        if (deviceData != null)
            updateConfig();
        else
            this.config = null;
        super.save(db);
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
        if (!this.type.equals(type)) {
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
