package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.ui.conf.Configurator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A value provider that will give all Enum values
 */
public class ConfigSensorValueProvider implements Configurator.ConfigValueProvider<Sensor> {
    private Sensor currentValue;
    private Map<String, Sensor> sensors = new HashMap<>();


    public ConfigSensorValueProvider(Class<Enum> fieldType, Object fieldValue) {
        this.currentValue = (Sensor) fieldValue;

        try {
            DBConnection db = HalContext.getDB();

            for (Sensor sensor : Sensor.getLocalSensors(db)) {
                sensors.put(getValue(sensor), sensor);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to collect Event objects.", e);
        }
    }


    private String getValue(Sensor sensor) {
        return sensor.getName() + " (id: " + sensor.getId() + ")";
    }

    @Override
    public String getCurrentValue() {
        return getValue(currentValue);
    }

    @Override
    public List<String> getPossibleValues() {
        return new ArrayList<String>(sensors.keySet());
    }

    @Override
    public Sensor getObject(String value) {
        return sensors.get(value);
    }
}