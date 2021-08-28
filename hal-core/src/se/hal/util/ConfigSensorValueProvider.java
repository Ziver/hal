package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.Timer;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.ui.conf.Configurator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.hal.util.ConfigEventValueProvider.DEVICE_REFRESH_TIME_IN_SECONDS;

/**
 * A value provider that will give all Enum values
 */
public class ConfigSensorValueProvider implements Configurator.ConfigValueProvider<Sensor> {
    private static final Logger logger = LogUtil.getLogger();

    private Timer updateTimer = new Timer(DEVICE_REFRESH_TIME_IN_SECONDS * 1000);
    private Map<String, Sensor> sensors = new HashMap<>();


    public ConfigSensorValueProvider() {
        refreshEventMap();
    }


    private synchronized void refreshEventMap() {
        if (!updateTimer.hasTimedOut())
            return;

        try {
            DBConnection db = HalContext.getDB();

            sensors.clear();
            for (Sensor sensor : Sensor.getLocalSensors(db)) {
                sensors.put(getValue(sensor), sensor);
            }

            updateTimer.start();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to retrieve Sensor devices.", e);
        }
    }


    @Override
    public String getValue(Sensor sensor) {
        return sensor.getName() + " (id: " + sensor.getId() + ")";
    }

    @Override
    public List<String> getPossibleValues() {
        refreshEventMap();
        return new ArrayList<>(sensors.keySet());
    }

    @Override
    public Sensor getObject(String value) {
        return sensors.get(value);
    }
}