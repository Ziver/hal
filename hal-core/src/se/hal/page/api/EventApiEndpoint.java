package se.hal.page.api;

import se.hal.EventControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalApiEndpoint;
import se.hal.intf.HalEventData;
import se.hal.struct.Event;
import se.hal.struct.devicedata.ColorEventData;
import se.hal.struct.devicedata.LevelEventData;
import se.hal.struct.devicedata.OnOffEventData;
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

        if ("modify".equals(request.get("action"))) {
           for (String idStr : reqIds) {
               int id = Integer.parseInt(idStr);
               Event event = Event.getEvent(db, id);

               if (event == null) {
                   logger.warning("Could not find given Event id: " + id);
                   continue;
               }

               // change event data
               HalEventData eventData;

               switch (event.getDeviceConfig().getDeviceDataClass().getSimpleName()) {
                   case "LevelEventData":
                       eventData = new LevelEventData(Integer.parseInt(request.get("data")) / 100.0, 0);
                       break;

                   case "ColorEventData":
                       eventData = new ColorEventData(request.get("data"), 0);
                       break;

                   case "OnOffEventData":
                       boolean booleanData = "on".equalsIgnoreCase(request.get("data")) || "true".equalsIgnoreCase(request.get("data"));
                       eventData = new OnOffEventData(booleanData, 0);
                       break;

                   default:
                       logger.warning("Unable to process event change request for event " + id + ", data type not supported: " + event.getDeviceConfig().getDeviceDataClass());
                       continue;
               }

               logger.info("Modifying Event(" + id + ") state: " + eventData);
               EventControllerManager.getInstance().send(event, eventData);
           }
        } else {
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
        }

        return root;
    }
}
