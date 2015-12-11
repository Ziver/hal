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
        if(request.containsKey("action")){
            String action = request.get("action");
            switch(action) {
                case "modify_local_user":
                    localUser.setUserName(request.get("username"));
                    localUser.setAddress(request.get("address"));
                    localUser.save(db);
                    break;

                case "create_local_sensor": break;
                case "modify_local_sensor": break;
                case "remove_local_sensor": break;

                case "create_external_user": break;
                case "modify_external_user": break;
                case "remove_external_user": break;

                case "modify_external_sensor":
                    Sensor sensor = Sensor.getSensor(db, Integer.parseInt(request.get("id")));
                    if(sensor != null){
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.save(db);
                    }
                    break;
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
