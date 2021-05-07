package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Event;
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
public class ConfigEventValueProvider implements Configurator.ConfigValueProvider<Event> {
    private Event currentValue;
    private Map<String, Event> events = new HashMap<>();


    public ConfigEventValueProvider(Class<Enum> fieldType, Object fieldValue) {
        this.currentValue = (Event) fieldValue;

        try {
            DBConnection db = HalContext.getDB();

            for (Event event : Event.getLocalEvents(db)) {
                events.put(getValue(event), event);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to collect Event objects.", e);
        }
    }


    private String getValue(Event event) {
        return event.getName() + " (id: " + event.getId() + ")";
    }

    @Override
    public String getCurrentValue() {
        return getValue(currentValue);
    }

    @Override
    public List<String> getPossibleValues() {
        return new ArrayList<String>(events.keySet());
    }

    @Override
    public Event getObject(String value) {
        return events.get(value);
    }
}