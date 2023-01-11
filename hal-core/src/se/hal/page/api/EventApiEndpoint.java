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

        String[] req_ids = new String[0];
        if (request.get("id") != null)
            req_ids = request.get("id").split(",");

        String req_typeConfig = request.get("typeConfig");
        String req_typeData = request.get("typeData");

        // Filter devices

        List<Event> events = new ArrayList<>();
        for (Event event : Event.getLocalEvents(db)) {
            boolean filter_match = true;

            // id filtering
            if (!ObjectUtil.isEmpty((Object) req_ids) && !ArrayUtil.contains(req_ids, "" + event.getId())) {
                filter_match = false;
            }

            // device type filtering
            if (!ObjectUtil.isEmpty(req_typeConfig) &&
                    !event.getDeviceConfig().getClass().getSimpleName().equals(req_typeConfig)) {
                filter_match = false;
            }

            // data type filtering
            if (!ObjectUtil.isEmpty(req_typeData) &&
                    !event.getDeviceConfig().getDeviceDataClass().getSimpleName().equals(req_typeData)) {
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
            root.add(deviceNode);
        }

        return root;
    }
}
