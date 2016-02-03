package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.intf.HalSensorData;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.ui.Configurator;
import zutil.ui.Configurator.*;

import java.util.Map;

public class SensorConfigHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "web-resource/sensor_config.tmpl";

    private class SensorDataParams{
        public Class clazz;
        public ConfigurationParam[] params;
    }
    private SensorDataParams[] sensorConfigurations;


    public SensorConfigHttpPage() {
        super("Configuration", "sensor_config");
        super.getRootNav().getSubNav("sensors").addSubNav(super.getNav());

        sensorConfigurations = new SensorDataParams[
                ControllerManager.getInstance().getAvailableSensors().size()];
        int i=0;
        for(Class c : ControllerManager.getInstance().getAvailableSensors()){
            sensorConfigurations[i] = new SensorDataParams();
            sensorConfigurations[i].clazz = c;
            sensorConfigurations[i].params = Configurator.getConfiguration(c);
            ++i;
        }
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
            int id = (request.containsKey("id") ? Integer.parseInt(request.get("id")) : -1);
            Sensor sensor;
            User user;
            Configurator<HalSensorData> configurator;
            switch(request.get("action")) {
                // Local Sensors
                case "create_local_sensor":
                    sensor = new Sensor();
                    sensor.setName(request.get("name"));
                    sensor.setType(request.get("type"));
                    sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                    sensor.setUser(localUser);
                    configurator = sensor.getDeviceConfig();
                    configurator.setValues(request);
                    configurator.applyConfiguration();
                    sensor.save(db);
                    ControllerManager.getInstance().register(sensor);
                    break;
                case "modify_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null){
                        sensor.setName(request.get("name"));
                        sensor.setType(request.get("type"));
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.getDeviceConfig().setValues(request).applyConfiguration();
                        sensor.save(db);
                    }
                    break;
                case "remove_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null) {
                        ControllerManager.getInstance().deregister(sensor);
                        sensor.delete(db);
                    }
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
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null){
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.save(db);
                    }
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);
        tmpl.set("localSensors", Sensor.getLocalSensors(db));
        tmpl.set("localSensorConf", sensorConfigurations);
        tmpl.set("detectedSensors", ControllerManager.getInstance().getDetectedSensors());
        tmpl.set("extUsers", User.getExternalUsers(db));
        tmpl.set("extSensor", Sensor.getExternalSensors(db));

        tmpl.set("availableSensors", ControllerManager.getInstance().getAvailableSensors());

        return tmpl;

    }

}
