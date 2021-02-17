package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.HalWebPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.ui.UserMessageManager;

import java.util.Map;

import static zutil.ui.UserMessageManager.*;

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
            HalServer.enablePlugin(name,
                    (request.containsKey("enabled") && "on".equals(request.get("enabled"))));

            HalAlertManager.getInstance().addAlert(new UserMessage(
                    MessageLevel.SUCCESS, "Successfully updated plugin " + name + ", change will take affect after restart.", MessageTTL.ONE_VIEW));
        }

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("plugins", HalServer.getAllPlugins());
        tmpl.set("controllers", ControllerManager.getInstance().getControllers());
        return tmpl;
    }
}
