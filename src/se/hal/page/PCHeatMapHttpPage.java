package se.hal.page;

import se.hal.intf.HalHttpPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class PCHeatMapHttpPage extends HalHttpPage {
	private static final String TEMPLATE = "resource/web/pc_heatmap.tmpl";


	public PCHeatMapHttpPage() {
		super("Heatmap", "pc_heatmap");
        super.getRootNav().getSubNav("sensors").addSubNav(super.getNav());
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
