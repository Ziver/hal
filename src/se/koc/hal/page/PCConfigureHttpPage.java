package se.koc.hal.page;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import se.koc.hal.HalContext;
import se.koc.hal.struct.Sensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

public class PCConfigureHttpPage extends HalHttpPage {

    public PCConfigureHttpPage() {
        super("Configuration", "config");
    }

    @Override
	public Templator httpRespond(
                Map<String, Object> session,
                Map<String, String> cookie,
                Map<String, String> request)
                throws Exception{


			DBConnection db = HalContext.getDB();
			
			Templator tmpl = new Templator(FileUtil.find("web-resource/configure.tmpl"));
			tmpl.set("user", User.getLocalUser(db));
			tmpl.set("localSensor", Sensor.getLocalSensors(db));
			tmpl.set("extUsers", User.getExternalUsers(db));
			tmpl.set("extSensor", Sensor.getExternalSensors(db));

			return tmpl;

	}

}
