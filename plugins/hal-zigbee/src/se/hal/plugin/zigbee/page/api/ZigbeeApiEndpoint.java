package se.hal.plugin.zigbee.page.api;

import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.zdo.field.NeighborTable;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalApiEndpoint;
import se.hal.plugin.zigbee.ZigbeeController;
import zutil.log.LogUtil;
import zutil.parser.DataNode;

import java.util.Map;
import java.util.logging.Logger;

/**
 * RESTish API for accessing and managing Zigbee devices.
 * For web interface definition see the OpenApi definition hal-core/resources/web/api/doc.html
 */
public class ZigbeeApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();


    public ZigbeeApiEndpoint() {
        super("api/zigbee");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DataNode root = new DataNode(DataNode.DataType.List);

        ZigbeeController controller = HalAbstractControllerManager.getController(ZigbeeController.class);

        // --------------------------------------
        // Generate DataNode
        // --------------------------------------

        for (ZigBeeNode node : controller.getNodes()) {
            DataNode zigbeeNode = new DataNode(DataNode.DataType.Map);
            root.add(zigbeeNode);
            zigbeeNode.set("address", node.getIeeeAddress().toString());
            zigbeeNode.set("logicalType", node.getLogicalType().toString());

            DataNode powerNode = new DataNode(DataNode.DataType.Map);
            zigbeeNode.set("powerDescriptor", powerNode);
            if (node.getPowerDescriptor() != null) {
                powerNode.set("currentPowerMode", node.getPowerDescriptor().getCurrentPowerMode().toString());
                powerNode.set("currentPowerSource", node.getPowerDescriptor().getCurrentPowerSource().toString());
                powerNode.set("powerLevel", node.getPowerDescriptor().getPowerLevel().toString());
            }

            DataNode neighborsNode = new DataNode(DataNode.DataType.List);
            zigbeeNode.set("neighbors", neighborsNode);
            for (NeighborTable neighbor : node.getNeighbors()) {
                neighborsNode.add(neighbor.getExtendedAddress().toString());
            }
        }

        return root;
    }
}
