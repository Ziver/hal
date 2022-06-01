package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalJsonPage;
import se.hal.struct.Event;
import zutil.ArrayUtil;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.DataNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Available HTTP Get Request parameters:
 * <pre>
 * Event filtering parameters:
 * id: comma separated numeric id for specific events
 * type: event data type name
 * </pre>
 */
public class EventJsonPage extends HalJsonPage {
    private static final Logger logger = LogUtil.getLogger();


    public EventJsonPage() {
        super("api/event");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.List);

        // Get Events

        String[] req_ids = new String[0];
        if (request.get("id") != null)
            req_ids = request.get("id").split(",");
        String req_type = request.get("type");

        List<Event> events = new ArrayList<>();
        for (Event event : Event.getLocalEvents(db)) {
            if (ArrayUtil.contains(req_ids, "" + event.getId())) { // id filtering
                events.add(event);
            }

            if (!ObjectUtil.isEmpty(req_type) &&
                    event.getDeviceConfig().getDeviceDataClass().getSimpleName().contains(req_type)) { // device type filtering
                events.add(event);
            }

            // no options defined, then add all events
            if (ObjectUtil.isEmpty(req_ids, req_type)) {
                events.add(event);
            }
        }

        // Generate DataNode

        for (Event event : events) {
            DataNode deviceNode = event.getDataNode();
            root.add(deviceNode);
        }

        return root;
    }
}
