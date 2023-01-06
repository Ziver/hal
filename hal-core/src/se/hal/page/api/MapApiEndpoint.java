package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalApiEndpoint;
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

        if ("getdata".equals(request.get("action"))) {
            getDeviceNode(db, root);
        } else if ("save".equals(request.get("action"))) {
            int id = Integer.parseInt(request.get("id"));
            HalAbstractDevice device = null;

            logger.info("Saving Sensor coordinates.");

            if ("sensor".equals(request.get("type")))
                device = Sensor.getSensor(db, id);
            else if ("event".equals(request.get("type")))
                device = Event.getEvent(db, id);

            device.setMapCoordinates(Float.parseFloat(request.get("x")), Float.parseFloat(request.get("y")));
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

    private DataNode getDeviceNode(HalAbstractDevice device) {
        DataNode deviceNode = new DataNode(DataNode.DataType.Map);
        deviceNode.set("id", device.getId());
        deviceNode.set("name", device.getName());
        deviceNode.set("x", device.getMapX());
        deviceNode.set("y", device.getMapY());
        return deviceNode;
    }

}
