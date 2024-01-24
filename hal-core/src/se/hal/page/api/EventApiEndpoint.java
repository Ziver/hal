package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalApiEndpoint;
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
 * RESTish API for accessing and managing Events.
 * For web interface definition see the OpenApi definition hal-core/resources/web/api/doc.html
 */
public class EventApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();


    public EventApiEndpoint() {
        super("api/event");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.List);

        // --------------------------------------
        // Get Action
        // --------------------------------------

        String[] reqIds = new String[0];
        if (request.get("id") != null)
            reqIds = request.get("id").split(",");

        String reqConfigType = request.get("configType");
        String reqDataType = request.get("dataType");

        // Filter devices

        List<Event> events = new ArrayList<>();
        for (Event event : Event.getLocalEvents(db)) {
            boolean filter_match = true;

            // id filtering
            if (!ObjectUtil.isEmpty((Object) reqIds) && !ArrayUtil.contains(reqIds, "" + event.getId())) {
                filter_match = false;
            }

            // device type filtering
            if (!ObjectUtil.isEmpty(reqConfigType) &&
                    !event.getDeviceConfig().getClass().getSimpleName().equals(reqConfigType)) {
                filter_match = false;
            }

            // data type filtering
            if (!ObjectUtil.isEmpty(reqDataType) &&
                    !event.getDeviceConfig().getDeviceDataClass().getSimpleName().equals(reqDataType)) {
                filter_match = false;
            }

            // Check the filter
            if (filter_match) {
                events.add(event);
            }
        }

        // --------------------------------------
        // Generate DataNode
        // --------------------------------------

        for (Event event : events) {
            DataNode deviceNode = event.getDataNode();
            deviceNode.set("type", "Event");
            root.add(deviceNode);
        }

        return root;
    }
}
