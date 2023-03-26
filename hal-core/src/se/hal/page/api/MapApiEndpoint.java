package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalApiEndpoint;
import se.hal.struct.Event;
import se.hal.struct.Room;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.DataNode;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TODO: This json endpoint might not be needed as we have SensorJsonPage?
 */
public class MapApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();

    public MapApiEndpoint() {
        super("api/map");
    }


    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception {

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.Map);

        if ("save".equals(request.get("action"))) {
            int id = Integer.parseInt(request.get("id"));

            logger.info("Saving map coordinates.");

            switch (request.get("type")) {
                case "room":
                    Room room = Room.getRoom(db, id);
                    room.setMapCoordinates(
                            Float.parseFloat(request.get("x")),
                            Float.parseFloat(request.get("y")),
                            Float.parseFloat(request.get("width")),
                            Float.parseFloat(request.get("height")));
                    room.save(db);
                    break;

                case "sensor":
                    Sensor sensor = Sensor.getSensor(db, id);
                    sensor.setMapCoordinates(
                            Float.parseFloat(request.get("x")),
                            Float.parseFloat(request.get("y")));
                    sensor.save(db);
                    break;

                case "event":
                    Event event = Event.getEvent(db, id);
                    event.setMapCoordinates(
                            Float.parseFloat(request.get("x")),
                            Float.parseFloat(request.get("y")));
                    event.save(db);
                    break;
            }
        }

        return root;
    }
}