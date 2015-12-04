package se.koc.hal.page;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import se.koc.hal.PowerChallenge;
import se.koc.hal.struct.Sensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

public class PCConfigureHttpPage implements HttpPage {

	@Override
	public void respond(HttpPrintStream out, HttpHeaderParser client_info,
			Map<String, Object> session, Map<String, String> cookie,
			Map<String, String> request) throws IOException {

		try {
			DBConnection db = PowerChallenge.db;
			
			Templator tmpl = new Templator(FileUtil.find("web-resource/configure.html"));
			tmpl.set("user", User.getLocalUser(db));
			tmpl.set("localSensor", Sensor.getLocalSensors(db));
			tmpl.set("extUsers", User.getExternalUsers(db));
			tmpl.set("extSensor", Sensor.getExternalSensors(db));
			out.print(tmpl.compile());

		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
