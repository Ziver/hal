package se.koc.hal.page;

import java.util.Map;

import se.koc.hal.HalContext;
import se.koc.hal.struct.Sensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
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
        User localUser = User.getLocalUser(db);

        // Save new input
        if(request.containsKey("form")){
            if(request.get("form").equals("user")){
                localUser.setUserName(request.get("username"));
                localUser.setAddress(request.get("address"));
                localUser.save(db);
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find("web-resource/configure.tmpl"));
        tmpl.set("user", localUser);
        tmpl.set("localSensor", Sensor.getLocalSensors(db));
        tmpl.set("extUsers", User.getExternalUsers(db));
        tmpl.set("extSensor", Sensor.getExternalSensors(db));

        return tmpl;

    }

}
