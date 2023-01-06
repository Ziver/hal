package se.hal.util;

import se.hal.HalContext;
import se.hal.struct.Room;
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
 * A value provider that will give all Rooms as selection option
 */
public class RoomValueProvider implements Configurator.ConfigValueProvider<Room> {
    private static final Logger logger = LogUtil.getLogger();

    private static int REFRESH_TIME_IN_SECONDS = 3;

    private Map<String, Room> rooms = new HashMap<>();
    private Timer updateTimer = new Timer(REFRESH_TIME_IN_SECONDS * 1000);


    public RoomValueProvider() {
        refreshEventMap();
    }


    private void refreshEventMap() {
        if (!updateTimer.hasTimedOut())
            return;

        try {
            DBConnection db = HalContext.getDB();

            for (Room room : Room.getRooms(db)) {
                rooms.put(getValue(room), room);
            }

            updateTimer.start();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to retrieve rooms.", e);
        }
    }


    @Override
    public String getValue(Room room) {
        return (room != null ?
                room.getName() + " (id: " + room.getId() + ")" :
                null);
    }

    @Override
    public List<String> getPossibleValues() {
        refreshEventMap();
        return new ArrayList<>(rooms.keySet());
    }

    @Override
    public Room getObject(String value) {
        return rooms.get(value);
    }
}