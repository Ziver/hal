package se.koc.hal.page;

import se.koc.hal.intf.HalHttpPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class PCHeatMapHttpPage extends HalHttpPage {

	public PCHeatMapHttpPage() {
		super("Heatmap", "map");
	}

	@Override
	public Templator httpRespond(
			Map<String, Object> session,
			Map<String, String> cookie,
			Map<String, String> request)
			throws Exception{

		Templator tmpl = new Templator(FileUtil.find("web-resource/heatmap.tmpl"));
		return tmpl;
	}

}
