package se.hal.page;

import se.hal.HalContext;
import se.hal.SensorControllerManager;
import se.hal.intf.HalWebPage;
import se.hal.struct.ClassConfigurationData;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;


public class SensorConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/sensor_config.tmpl";

    private ArrayList<ClassConfigurationData> sensorConfigurations;


    public SensorConfigWebPage() {
        super("sensor_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Sensor Settings").setWeight(100);

        sensorConfigurations = new ArrayList<>();
        for (Class c : SensorControllerManager.getInstance().getAvailableDeviceConfigs())
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
        if (request.containsKey("action")) {
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));
            Sensor sensor;
            User user;

            switch(request.get("action")) {
                // Local Sensors
                case "create_local_sensor":
                    logger.info("Creating sensor: " + request.get("name"));
                    sensor = new Sensor();
                    sensor.setName(request.get("name"));
                    sensor.setType(request.get("type"));
                    sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                    sensor.setUser(localUser);
                    sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                    sensor.save(db);
                    SensorControllerManager.getInstance().register(sensor);

                    HalAlertManager.getInstance().addAlert(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new sensor: "+sensor.getName(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if (sensor != null) {
                        logger.info("Modifying sensor: " + sensor.getName());
                        sensor.setName(request.get("name"));
                        sensor.setType(request.get("type"));
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                        sensor.save(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved sensor: "+sensor.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown sensor id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown sensor id: " + id, MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_local_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if (sensor != null) {
                        logger.warning("Removing sensor: " + sensor.getName());
                        SensorControllerManager.getInstance().deregister(sensor);
                        sensor.delete(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed sensor: "+sensor.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown sensor id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown sensor id: " + id, MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_all_detected_sensors":
                    SensorControllerManager.getInstance().clearDetectedDevices();
                    break;

                // External Users
                case "create_external_user":
                    logger.info("Creating external user: " + request.get("hostname"));
                    user = new User();
                    user.setHostname(request.get("hostname"));
                    user.setPort(Integer.parseInt(request.get("port")));
                    user.setExternal(true);
                    user.save(db);

                    HalAlertManager.getInstance().addAlert(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new external user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_external_user":
                    user = User.getUser(db, id);
                    if (user != null) {
                        logger.info("Modifying external user: " + user.getHostname());
                        user.setHostname(request.get("hostname"));
                        user.setPort(Integer.parseInt(request.get("port")));
                        user.save(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved external user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown user id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown user id: " + id, MessageTTL.ONE_VIEW));
                    }
                    break;
                case "remove_external_user":
                    user = User.getUser(db, id);
                    if (user != null) {
                        logger.info("Removing external user: " + user.getHostname());
                        user.delete(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown user id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown user id: "+id, MessageTTL.ONE_VIEW));
                    }
                    break;

                // External Sensors
                case "modify_external_sensor":
                    sensor = Sensor.getSensor(db, id);
                    if (sensor != null) {
                        logger.warning("Modifying external sensor: " + sensor.getName());
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.save(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved external sensor: "+sensor.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown user id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown sensor id: "+id, MessageTTL.ONE_VIEW));
                    }
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);
        tmpl.set("localSensors", Sensor.getLocalSensors(db));
        tmpl.set("detectedSensors", SensorControllerManager.getInstance().getDetectedDevices());
        tmpl.set("extUsers", User.getExternalUsers(db));
        tmpl.set("extSensor", Sensor.getExternalSensors(db));
        tmpl.set("availableSensorConfigClasses", SensorControllerManager.getInstance().getAvailableDeviceConfigs());
        tmpl.set("availableSensorObjectConfig", sensorConfigurations);

        return tmpl;

    }

}
