package se.hal.page;

import se.hal.HalContext;
import se.hal.SensorControllerManager;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalScannableController;
import se.hal.intf.HalWebPage;
import se.hal.struct.Room;
import se.hal.util.ClassConfigurationFacade;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import se.hal.util.RoomValueProvider;
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

    private ArrayList<ClassConfigurationFacade> sensorConfigurations;


    public SensorConfigWebPage() {
        super("sensor_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Sensor Settings").setWeight(100);

        sensorConfigurations = new ArrayList<>();
        for (Class c : SensorControllerManager.getInstance().getAvailableDeviceConfigs())
            sensorConfigurations.add(new ClassConfigurationFacade(c));
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();
        User localUser = User.getLocalUser(db);

        // Save new input
        if (request.containsKey("action")) {
            int sensorId = (ObjectUtil.isEmpty(request.get("sensor-id")) ? -1 : Integer.parseInt(request.get("sensor-id")));
            int userId = (ObjectUtil.isEmpty(request.get("user-id")) ? -1 : Integer.parseInt(request.get("user-id")));
            int roomId = (ObjectUtil.isEmpty(request.get("room-id")) ? -1 : Integer.parseInt(request.get("room-id")));

            Sensor sensor = null;
            User user = null;
            Room room = (roomId >= 0 ? Room.getRoom(db, roomId) : null);

            if (sensorId >= 0) {
                // Read in requested id
                sensor = Sensor.getSensor(db, sensorId);

                if (sensor == null) {
                    logger.warning("Unknown sensor id: " + sensorId);
                    HalContext.getUserMessageManager().add(new UserMessage(
                            MessageLevel.ERROR, "Unknown sensor id: " + sensorId, MessageTTL.ONE_VIEW));
                }
            }

            if (userId >= 0) {
                // Read in requested id
                user = User.getUser(db, userId);

                if (user == null) {
                    logger.warning("Unknown user id: " + userId);
                    HalContext.getUserMessageManager().add(new UserMessage(
                            MessageLevel.ERROR, "Unknown user id: " + userId, MessageTTL.ONE_VIEW));
                }
            }

            switch(request.get("action")) {
                // ----------------------------------------
                // Local Sensors
                // ----------------------------------------

                case "create_local_sensor":
                    logger.info("Creating sensor: " + request.get("name"));
                    sensor = new Sensor();
                    sensor.setRoom(room);
                    sensor.setName(request.get("name"));
                    sensor.setType(request.get("type"));
                    sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                    sensor.setUser(localUser);
                    sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                    sensor.save(db);
                    SensorControllerManager.getInstance().register(sensor);

                    HalContext.getUserMessageManager().add(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new sensor: " + sensor.getName(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_local_sensor":
                    if (sensor != null) {
                        logger.info("Modifying sensor(id: " + sensor.getId() + "): " + sensor.getName());
                        sensor.setRoom(room);
                        sensor.setName(request.get("name"));
                        sensor.setType(request.get("type"));
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.getDeviceConfigurator().setValues(request).applyConfiguration();
                        sensor.save(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved sensor: " + sensor.getName(), MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_local_sensor":
                    if (sensor != null) {
                        logger.warning("Removing sensor(id: " + sensor.getId() + "): " + sensor.getName());
                        SensorControllerManager.getInstance().deregister(sensor);
                        sensor.delete(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed sensor: " + sensor.getName(), MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_all_detected_sensors":
                    SensorControllerManager.getInstance().clearDetectedDevices();
                    break;

                case "start_scan":
                    for (HalAbstractController controller : HalAbstractControllerManager.getControllers()) {
                        if (controller instanceof HalScannableController) {
                            ((HalScannableController) controller).startScan();

                            HalContext.getUserMessageManager().add(new UserMessage(
                                    MessageLevel.SUCCESS, "Initiated scanning on controller: " + controller.getClass().getName(), MessageTTL.ONE_VIEW));
                        }
                    }
                    break;

                // ----------------------------------------
                // External Users
                // ----------------------------------------

                case "create_external_user":
                    logger.info("Creating external user: " + request.get("hostname"));
                    user = new User();
                    user.setHostname(request.get("hostname"));
                    user.setPort(Integer.parseInt(request.get("port")));
                    user.setExternal(true);
                    user.save(db);

                    HalContext.getUserMessageManager().add(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new external user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_external_user":
                    if (user != null) {
                        logger.info("Modifying external user: " + user.getHostname());
                        user.setHostname(request.get("hostname"));
                        user.setPort(Integer.parseInt(request.get("port")));
                        user.save(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved external user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    }
                    break;
                case "remove_external_user":
                    if (user != null) {
                        logger.info("Removing external user: " + user.getHostname());
                        user.delete(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed user with host: "+user.getHostname(), MessageTTL.ONE_VIEW));
                    }
                    break;

                // ----------------------------------------
                // External Sensors
                // ----------------------------------------

                case "modify_external_sensor":
                    if (sensor != null) {
                        logger.warning("Modifying external sensor: " + sensor.getName());
                        sensor.setSynced(Boolean.parseBoolean(request.get("sync")));
                        sensor.save(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved external sensor: " + sensor.getName(), MessageTTL.ONE_VIEW));
                    }
                    break;
            }
        }

        // Is any scan active?
        boolean scanning = false;

        for (HalAbstractController controller : HalAbstractControllerManager.getControllers()) {
            if (controller instanceof HalScannableController) {
                scanning |= ((HalScannableController) controller).isScanning();
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);
        tmpl.set("rooms", Room.getRooms(db));
        tmpl.set("scanning", scanning);
        tmpl.set("localSensors", Sensor.getLocalSensors(db));
        tmpl.set("detectedSensors", SensorControllerManager.getInstance().getDetectedDevices());
        tmpl.set("extUsers", User.getExternalUsers(db));
        tmpl.set("extSensor", Sensor.getExternalSensors(db));
        tmpl.set("availableSensorConfigClasses", SensorControllerManager.getInstance().getAvailableDeviceConfigs());
        tmpl.set("availableSensorObjectConfig", sensorConfigurations);

        return tmpl;

    }

}
