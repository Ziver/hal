package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Event;
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

/**
 * A value provider that will give all Enum values
 */
public class ConfigEventValueProvider implements Configurator.ConfigValueProvider<Event> {
    private static final Logger logger = LogUtil.getLogger();

    protected static int DEVICE_REFRESH_TIME_IN_SECONDS = 30;

    private Map<String, Event> events = new HashMap<>();
    private Timer updateTimer = new Timer(DEVICE_REFRESH_TIME_IN_SECONDS * 1000);


    public ConfigEventValueProvider() {
        refreshEventMap();
    }


    private void refreshEventMap() {
        if (!updateTimer.hasTimedOut())
            return;

        try {
            DBConnection db = HalContext.getDB();

            for (Event event : Event.getLocalEvents(db)) {
                events.put(getValue(event), event);
            }

            updateTimer.start();
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
        refreshEventMap();
        return new ArrayList<>(events.keySet());
    }

    @Override
    public Event getObject(String value) {
        return events.get(value);
    }
}