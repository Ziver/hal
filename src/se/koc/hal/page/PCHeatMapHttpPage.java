package se.koc.hal.page;

import java.io.IOException;
import java.util.Map;

import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

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
