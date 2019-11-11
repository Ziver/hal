package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalJsonPage;
import se.hal.struct.AbstractDevice;
import se.hal.struct.Event;
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
public class MapJsonPage extends HalJsonPage {
    private static final Logger logger = LogUtil.getLogger();

    public MapJsonPage() {
        super("data/map");
    }


    @Override
    public DataNode jsonRespond(Map<String, Object> session,
                                 Map<String, String> cookie,
                                 Map<String, String> request) throws Exception {
        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.Map);

        if ("getdata".equals(request.get("action"))) {
            getDeviceNode(db, root);
        } else if ("save".equals(request.get("action"))) {
            int id = Integer.parseInt(request.get("id"));
            AbstractDevice device = null;

            logger.info("Saving Sensor coordinates.");

            if ("sensor".equals(request.get("type")))
                device = Sensor.getSensor(db, id);
            else if ("event".equals(request.get("type")))
                device = Event.getEvent(db, id);

            device.setX(Float.parseFloat(request.get("x")));
            device.setY(Float.parseFloat(request.get("y")));
            device.save(db);
        }
        return root;
    }


    private void getDeviceNode(DBConnection db, DataNode root) throws SQLException {
        DataNode sensorsNode = new DataNode(DataNode.DataType.List);

        for (Sensor sensor : Sensor.getLocalSensors(db)) {
            DataNode sensorNode = getDeviceNode(sensor);
            sensorNode.set("data", ""+sensor.getDeviceData());
            sensorsNode.add(sensorNode);
        }
        root.set("sensors", sensorsNode);

        DataNode eventsNode = new DataNode(DataNode.DataType.List);

        for (Event event : Event.getLocalEvents(db)) {
            DataNode eventNode = getDeviceNode(event);
            eventNode.set("data", ""+event.getDeviceData());
            eventsNode.add(eventNode);
        }
        root.set("events", eventsNode);
    }

    private DataNode getDeviceNode(AbstractDevice device) {
        DataNode deviceNode = new DataNode(DataNode.DataType.Map);
        deviceNode.set("id", device.getId());
        deviceNode.set("name", device.getName());
        deviceNode.set("x", device.getX());
        deviceNode.set("y", device.getY());
        return deviceNode;
    }

}
