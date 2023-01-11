package se.hal.page.api;

import se.hal.HalContext;
import se.hal.intf.HalApiEndpoint;
import se.hal.struct.Event;
import zutil.ArrayUtil;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.DataNode;
import zutil.ui.UserMessageManager;
import zutil.ui.UserMessageManager.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * RESTish API for accessing and managing user alert.
 * For web interface definition see the OpenApi definition hal-core/resources/web/api/doc.html
 */
public class AlertApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();


    public AlertApiEndpoint() {
        super("api/alert");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        // --------------------------------------
        // Filter alerts
        // --------------------------------------

        String[] req_ids = new String[0];
        if (request.get("id") != null)
            req_ids = request.get("id").split(",");

        // Filter devices

        List<UserMessage> messages = new ArrayList<>();
        for (UserMessage message : HalContext.getUserMessageManager().getMessages()) {
            boolean filter_match = true;

            // id filtering
            if (!ObjectUtil.isEmpty((Object) req_ids) && !ArrayUtil.contains(req_ids, "" + message.getId())) {
                filter_match = false;
            }

            // Check the filter
            if (filter_match) {
                messages.add(message);
            }
        }

        if (ObjectUtil.isEmpty(request.get("action"))) {
            messages.clear(); // Reset the message list
        } else {
            switch (request.get("action")) {
                case "poll":
                    for (UserMessage message : messages) {
                        message.decreaseTTL();
                    }
                case "peek":
                    break;

                case "dismiss":
                    for (UserMessage message : messages) {
                        message.dismiss();
                    }
                    break;

                default:
                    messages.clear();
                    break;
            }
        }

        // --------------------------------------
        // Generate DataNode
        // --------------------------------------

        DataNode root = new DataNode(DataNode.DataType.List);

        for (UserMessage message : messages) {
            root.add(getUserMessageDataNode(message));
        }

        return root;
    }

    /**
     * @param message the source alert to generate data node for.
     * @return a DataNode containing relevant information from the given alert. Will return null if the message is null.
     */
    public static DataNode getUserMessageDataNode(UserMessage message) {
        if (message == null)
            return null;

        DataNode node = new DataNode(DataNode.DataType.Map);
        node.set("id", message.getId());
        node.set("level", message.getLevel().toString());
        node.set("ttl", message.getTitle());
        node.set("title", message.getTitle());
        node.set("description", message.getDescription());
        return node;
    }
}
