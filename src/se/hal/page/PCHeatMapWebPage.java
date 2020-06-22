package se.hal.page;

import se.hal.intf.HalWebPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class PCHeatMapWebPage extends HalWebPage {
    private static final String TEMPLATE = "resource/web/pc_heatmap.tmpl";


    public PCHeatMapWebPage() {
        super("pc_heatmap");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Heatmap").setWeight(60);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        return tmpl;
    }

}
