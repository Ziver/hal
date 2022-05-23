package se.hal.plugin.zigbee.page;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import se.hal.HalContext;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalWebPage;
import se.hal.plugin.zigbee.ZigbeeController;
import se.hal.struct.Event;
import zutil.ObjectUtil;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;
import java.util.Set;

public class ZigbeeNetworkPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/zigbee_network.tmpl";

    public ZigbeeNetworkPage() {
        super("zigbee_network");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Zigbee Network").setWeight(10_000);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        ZigbeeController controller = HalAbstractControllerManager.getController(ZigbeeController.class);

        if (request.containsKey("action")) {
            ZigBeeNode node = null;
            ZigBeeEndpoint endpoint = null;
            ZclCluster cluster = null;
            ZclAttribute attribute = null;

            if (!ObjectUtil.isEmpty(request.get("nodeAddress"))) {
                String nodeAddress = request.get("nodeAddress");
                node = controller.getNode(new IeeeAddress(nodeAddress));

                if (!ObjectUtil.isEmpty(request.get("endpointId"))) {
                    int endpointId = Integer.parseInt(request.get("endpointId"));
                    endpoint = node.getEndpoint(endpointId);

                    if (!ObjectUtil.isEmpty(request.get("clusterId"))) {
                        int clusterId = Integer.parseInt(request.get("clusterId"));
                        cluster = endpoint.getInputCluster(clusterId);

                        if (!ObjectUtil.isEmpty(request.get("attributeId"))) {
                            int attributeId = Integer.parseInt(request.get("attributeId"));
                            attribute = cluster.getAttribute(attributeId);
                        }
                    }
                }
            }

            switch (request.get("action")) {
                case "refresh":
                    if (attribute != null)
                        attribute.readValue(0);
                    break;
            }
        }

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("controller", controller);
        tmpl.set("nodes", controller.getNodes());
        return tmpl;
    }
}
