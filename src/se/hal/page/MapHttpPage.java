package se.hal.page;

import se.hal.intf.HalHttpPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

/**
 * Created by Ziver on 2016-06-23.
 */
public class MapHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/map.tmpl";


    public MapHttpPage() {
        super("map");
        super.getRootNav().createSubNav(this.getId(), "Map").setWeight(-100);
        super.showSubNav(false);
    }

    @Override
    public Templator httpRespond(Map<String, Object> session, Map<String, String> cookie, Map<String, String> request) throws Exception {
        return new Templator(FileUtil.find(TEMPLATE));
    }
}
