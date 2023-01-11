package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalApiEndpoint;
import se.hal.struct.Room;
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
 * REST service to fetch event device information.
 * For web interface definition see the OpenApi definition hal-core/resources/web/api/doc.html
 */
public class RoomApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();


    public RoomApiEndpoint() {
        super("api/room");
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

        // Filter devices

        List<Room> rooms = new ArrayList<>();
        for (Room room : Room.getRooms(db)) {
            boolean filter_match = true;

            // id filtering
            if (!ObjectUtil.isEmpty((Object) req_ids) && !ArrayUtil.contains(req_ids, "" + room.getId())) {
                filter_match = false;
            }

            // Check the filter
            if (filter_match) {
                rooms.add(room);
            }
        }

        // --------------------------------------
        // Generate DataNode
        // --------------------------------------

        for (Room room : rooms) {
            DataNode deviceNode = room.getDataNode();
            root.add(deviceNode);
        }

        return root;
    }
}
