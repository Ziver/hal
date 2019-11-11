package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.page.HalAlertManager.AlertLevel;
import se.hal.page.HalAlertManager.AlertTTL;
import se.hal.page.HalAlertManager.HalAlert;
import se.hal.struct.ClassConfigurationData;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.Map;

public class SensorConfigHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/sensor_config.tmpl";

    private ArrayList<ClassConfigurationData> sensorConfigurations;


    public SensorConfigHttpPage() {
        super("sensor_config");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Configuration").setWeight(100);

        sensorConfigurations = new ArrayList<>();
        for(Class c : ControllerManager.getInstance().getAvailableSensors())
            sensorConfigurations.add(new ClassConfigurationData(c));
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
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));
            Sensor sensor;
            User user;

            switch(request.get("action")) {
                // Local Sensors
                case "create_local_sensor":
                    sensor = new Sensor();
                    sensor.setName(request.get("name"));
                    sensor.setType(request.get("type"));
                    sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                    sensor.setUser(localUser);
                    sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                    sensor.save(db);
                    ControllerManager.getInstance().register(sensor);

                    HalAlertManager.getInstance().addAlert(new HalAlert(
                            AlertLevel.SUCCESS, "Successfully created new sensor: "+sensor.getName(), AlertTTL.ONE_VIEW));
                    break;

                case "modify_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null){
                        sensor.setName(request.get("name"));
                        sensor.setType(request.get("type"));
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                        sensor.save(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully saved sensor: "+sensor.getName(), AlertTTL.ONE_VIEW));
                    } else {
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown sensor id: "+id, AlertTTL.ONE_VIEW));
                    }
                    break;

                case "remove_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null) {
                        ControllerManager.getInstance().deregister(sensor);
                        sensor.delete(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully deleted sensor: "+sensor.getName(), AlertTTL.ONE_VIEW));
                    } else {
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown sensor id: "+id, AlertTTL.ONE_VIEW));
                    }
                    break;

                case "remove_all_detected_sensors":
                    ControllerManager.getInstance().clearDetectedSensors();
                    break;

                // External Users
                case "create_external_user":
                    user = new User();
                    user.setHostname(request.get("hostname"));
                    user.setPort(Integer.parseInt(request.get("port")));
                    user.setExternal(true);
                    user.save(db);

                    HalAlertManager.getInstance().addAlert(new HalAlert(
                            AlertLevel.SUCCESS, "Successfully created new external user with host: "+user.getHostname(), AlertTTL.ONE_VIEW));
                    break;
                case "modify_external_user":
                    user = User.getUser(db, id);
                    if(user != null){
                        user.setHostname(request.get("hostname"));
                        user.setPort(Integer.parseInt(request.get("port")));
                        user.save(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully saved external user with host: "+user.getHostname(), AlertTTL.ONE_VIEW));
                    } else {
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown user id: "+id, AlertTTL.ONE_VIEW));
                    }
                    break;
                case "remove_external_user":
                    user = User.getUser(db, id);
                    if (user != null) {
                        user.delete(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully deleted user with host: "+user.getHostname(), AlertTTL.ONE_VIEW));
                    } else {
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown user id: "+id, AlertTTL.ONE_VIEW));
                    }
                    break;

                // External Sensors
                case "modify_external_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if(sensor != null){
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.save(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully saved external sensor: "+sensor.getName(), AlertTTL.ONE_VIEW));
                    } else {
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown sensor id: "+id, AlertTTL.ONE_VIEW));
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
