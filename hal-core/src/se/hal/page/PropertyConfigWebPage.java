package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class PropertyConfigWebPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/property_config.tmpl";


    public PropertyConfigWebPage() {
        super("properties");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Properties");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        Map<String,String> properties = HalContext.getProperties();

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("properties", properties.entrySet());
        return tmpl;

    }
}
