package se.hal.page;

import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.HalWebPage;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class PluginConfigWebPage extends HalWebPage {
    private static final String TEMPLATE = "resource/web/plugin_config.tmpl";


    public PluginConfigWebPage(){
        super("plugins");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Plugins").setWeight(100);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();

        PluginManager pluginManager = HalServer.getPluginManager();

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("plugins", pluginManager.toArray());
        return tmpl;

    }
}
