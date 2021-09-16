package se.hal.plugin.zigbee.page;

import com.zsmartsystems.zigbee.ZigBeeNode;
import se.hal.HalContext;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalWebPage;
import se.hal.plugin.zigbee.ZigbeeController;
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

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("controller", controller);
        tmpl.set("nodes", controller.getNodes());
        return tmpl;
    }
}
