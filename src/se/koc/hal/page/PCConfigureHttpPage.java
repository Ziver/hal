package se.koc.hal.page;

import se.koc.hal.HalContext;
import se.koc.hal.intf.HalHttpPage;
import se.koc.hal.struct.HalSensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

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
            int id = (request.containsKey("id") ? Integer.parseInt(request.get("id")) : -1);
            HalSensor sensor;
            User user;
            switch(action) {
                // Local User
                case "modify_local_user":
                    localUser.setUserName(request.get("username"));
                    localUser.setAddress(request.get("address"));
                    localUser.save(db);
                    break;

                // Local Sensors
                case "create_local_sensor":
                    sensor = new HalSensor();
                    sensor.setName(request.get("name"));
                    sensor.setType(request.get("type"));
                    sensor.setConfig(request.get("config"));
                    sensor.setUser(localUser);
                    sensor.setSynced(true);
                    sensor.save(db);
                case "modify_local_sensor":
                    sensor = HalSensor.getSensor(db, id);
                    if(sensor != null){
                        sensor.setName(request.get("name"));
                        sensor.setType(request.get("type"));
                        sensor.setConfig(request.get("config"));
                        sensor.save(db);
                    }
                    break;
                case "remove_local_sensor":
                    sensor = HalSensor.getSensor(db, id);
                    if(sensor != null)
                        sensor.delete(db);
                    break;

                // External Users
                case "create_external_user":
                    user = new User();
                    user.setHostname(request.get("hostname"));
                    user.setPort(Integer.parseInt(request.get("port")));
                    user.setExternal(true);
                    user.save(db);
                    break;
                case "modify_external_user":
                    user = User.getUser(db, id);
                    if(user != null){
                        user.setHostname(request.get("hostname"));
                        user.setPort(Integer.parseInt(request.get("port")));
                        user.save(db);
                    }
                    break;
                case "remove_external_user":
                    user = User.getUser(db, id);
                    if(user != null)
                        user.delete(db);
                    break;

                // External Sensors
                case "modify_external_sensor":
                    sensor = HalSensor.getSensor(db, id);
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
        tmpl.set("localSensor", HalSensor.getLocalSensors(db));
        tmpl.set("extUsers", User.getExternalUsers(db));
        tmpl.set("extSensor", HalSensor.getExternalSensors(db));

        return tmpl;

    }

}
