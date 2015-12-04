package se.koc.hal;

import java.io.IOException;
import java.util.Map;

import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

public class PowerChallengeHttpPage implements HttpPage {

	@Override
	public void respond(HttpPrintStream out, HttpHeaderParser client_info,
			Map<String, Object> session, Map<String, String> cookie,
			Map<String, String> request) throws IOException {
		
		Templator tmpl = new Templator("resource/index.tmpl");
		
		out.print(tmpl.compile());
	}

}
