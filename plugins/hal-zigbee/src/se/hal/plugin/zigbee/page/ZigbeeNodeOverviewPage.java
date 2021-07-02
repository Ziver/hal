package se.hal.plugin.zigbee.page;

import com.zsmartsystems.zigbee.ZigBeeNode;
import se.hal.HalContext;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalWebPage;
import se.hal.plugin.zigbee.HalZigbeeController;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;
import java.util.Set;

public class ZigbeeNodeOverviewPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/zigbee_node_overview.tmpl";

    public ZigbeeNodeOverviewPage() {
        super("zigbee_overview");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Zigbee Overview").setWeight(10_000);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        Set<ZigBeeNode> nodes = null;
        for (HalAbstractController controller : HalAbstractControllerManager.getControllers()) {
            if (controller instanceof HalZigbeeController) {
                nodes = ((HalZigbeeController) controller).getNodes();
                break;
            }
        }

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("nodes", nodes);
        return tmpl;
    }
}
