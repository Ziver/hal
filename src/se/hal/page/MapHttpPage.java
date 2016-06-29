package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
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
        DBConnection db = HalContext.getDB();
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("sensors", Sensor.getLocalSensors(db));
        tmpl.set("events", Event.getLocalEvents(db));

        return tmpl;
    }
}
