package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.HalWebPage;
import se.hal.page.HalAlertManager.AlertLevel;
import se.hal.page.HalAlertManager.AlertTTL;
import se.hal.page.HalAlertManager.HalAlert;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class PluginConfigWebPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/plugin_config.tmpl";


    public PluginConfigWebPage(){
        super("plugins");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Plugins").setWeight(500);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        if (request.containsKey("action")) {
            String name = request.get("action_id");
            HalServer.setPluginEnabled(name,
                    (request.containsKey("enabled") && "on".equals(request.get("enabled"))));

            HalAlertManager.getInstance().addAlert(new HalAlert(
                    AlertLevel.SUCCESS, "Successfully updated plugin " + name + ", change will take affect after restart.", AlertTTL.ONE_VIEW));
        }

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("plugins", HalServer.getPlugins());
        tmpl.set("controllers", ControllerManager.getInstance().getControllers());
        return tmpl;
    }
}
