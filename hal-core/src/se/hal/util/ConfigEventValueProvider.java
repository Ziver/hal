package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Event;
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

/**
 * A value provider that will give all Enum values
 */
public class ConfigEventValueProvider implements Configurator.ConfigValueProvider<Event> {
    private static final Logger logger = LogUtil.getLogger();

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
            logger.log(Level.SEVERE, "Unable to retrieve local events.", e);
        }
    }

    @Override
    public String getValue(Event event) {
        return (event != null ?
                event.getName() + " (id: " + event.getId() + ")" :
                null);
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