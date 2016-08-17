package se.hal;

import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;
import zutil.ui.Configurator;
import zutil.ui.Configurator.PostConfigurationActionListener;
import zutil.ui.Configurator.PreConfigurationActionListener;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
@SuppressWarnings("RedundantCast")
public class ControllerManager implements HalSensorReportListener,
        HalEventReportListener,
        PreConfigurationActionListener,
        PostConfigurationActionListener {
    private static final Logger logger = LogUtil.getLogger();
    private static ControllerManager instance;


    /** All available sensor plugins **/
    private List<Class<? extends HalSensorConfig>> availableSensors = new ArrayList<>();
    /** List of all registered sensors **/
    private List<Sensor> registeredSensors = Collections.synchronizedList(new ArrayList<Sensor>());
    /** List of auto detected sensors **/
    private List<Sensor> detectedSensors = Collections.synchronizedList(new ArrayList<Sensor>());
    /** List of sensors that are currently being reconfigured **/
    private List<Sensor> limboSensors = Collections.synchronizedList(new LinkedList<Sensor>());


    /** All available event plugins **/
    private List<Class<? extends HalEventConfig>> availableEvents = new ArrayList<>();
    /** List of all registered events **/
    private List<Event> registeredEvents = Collections.synchronizedList(new ArrayList<Event>());
    /** List of auto detected events **/
    private List<Event> detectedEvents = Collections.synchronizedList(new ArrayList<Event>());
    /** List of all registered events **/
    private List<Event> limboEvents = Collections.synchronizedList(new LinkedList<Event>());


    /** A map of all instantiated controllers **/
    private HashMap<Class,Object> controllerMap = new HashMap<>();



    /////////////////////////////// SENSORS ///////////////////////////////////

    public void register(Sensor sensor) {
        if(sensor.getDeviceConfig() == null) {
            logger.warning("Sensor data is null: "+ sensor);
            return;
        }
        if(!availableSensors.contains(sensor.getDeviceConfig().getClass())) {
            logger.warning("Sensor data plugin not available: "+ sensor.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new sensor(id: "+ sensor.getId() +"): "+ sensor.getDeviceConfig().getClass());
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(sensor.getDeviceConfig());
        registeredSensors.add(sensor);
        detectedSensors.remove(findSensor(sensor.getDeviceConfig(), detectedSensors)); // Remove if this device was detected
    }

    public void deregister(Sensor sensor){
        if(sensor.getDeviceConfig() == null) {
            logger.warning("Sensor data is null: "+ sensor);
            return;
        }

        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = (HalSensorController) controllerMap.get(c);
        if (controller != null) {
            logger.info("Deregistering sensor(id: "+ sensor.getId() +"): "+ sensor.getDeviceConfig().getClass());
            controller.deregister(sensor.getDeviceConfig());
            registeredSensors.remove(sensor);
            removeControllerIfEmpty(controller);
        } else {
            logger.warning("Controller not instantiated:"+ sensor.getController());
        }
    }

    public List<Class<? extends HalSensorConfig>> getAvailableSensors(){
        return availableSensors;
    }

    public List<Sensor> getDetectedSensors(){
        return detectedSensors;
    }

    @Override
    public void reportReceived(HalSensorConfig sensorConfig, HalSensorData sensorData) {
        try{
            DBConnection db = HalContext.getDB();
            Sensor sensor = findSensor(sensorConfig, registeredSensors);

            if (sensor != null) {
                logger.finest("Received report from sensor("+sensorConfig.getClass().getSimpleName()+"): "+ sensorConfig);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, sensor_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, sensorData.getTimestamp());
                stmt.setLong(2, sensor.getId());
                stmt.setDouble(3, sensorData.getData());
                DBConnection.exec(stmt);
            }
            else { // unknown sensor
                logger.finest("Received report from unregistered sensor" +
                        "("+sensorConfig.getClass().getSimpleName()+"): "+ sensorConfig);
                sensor = findSensor(sensorConfig, detectedSensors);
                if(sensor == null) {
                    sensor = new Sensor();
                    detectedSensors.add(sensor);
                }
            }
            sensor.setDeviceConfig(sensorConfig); // Set the latest data

        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store sensor report", e);
        }
    }

    private static Sensor findSensor(HalSensorConfig sensorData, List<Sensor> list){
        for (int i=0; i<list.size(); ++i) { // Don't use foreach for concurrency reasons
            Sensor s = list.get(i);
            if (sensorData.equals(s.getDeviceConfig())) {
                return s;
            }
        }
        return null;
    }

    //////////////////////////////// EVENTS ///////////////////////////////////

    public void register(Event event) {
        if(event.getDeviceConfig() == null) {
            logger.warning("Event data is null: "+ event);
            return;
        }
        if(!availableEvents.contains(event.getDeviceConfig().getClass())) {
            logger.warning("Event data plugin not available: "+ event.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new event(id: "+ event.getId() +"): "+ event.getDeviceConfig().getClass());
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(event.getDeviceConfig());
        registeredEvents.add(event);
        detectedEvents.remove(findEvent(event.getDeviceConfig(), detectedEvents)); // Remove if this device was detected
    }

    public void deregister(Event event){
        if(event.getDeviceConfig() == null) {
            logger.warning("Event data is null: "+ event);
            return;
        }

        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = (HalEventController) controllerMap.get(c);
        if (controller != null) {
            logger.info("Deregistering event(id: "+ event.getId() +"): "+ event.getDeviceConfig().getClass());
            controller.deregister(event.getDeviceConfig());
            registeredEvents.remove(event);
            removeControllerIfEmpty(controller);
        } else {
            logger.warning("Controller not instantiated: "+ event.getController());
        }
    }

    public List<Class<? extends HalEventConfig>> getAvailableEvents(){
        return availableEvents;
    }

    public List<Event> getDetectedEvents(){
        return detectedEvents;
    }

    @Override
    public void reportReceived(HalEventConfig eventConfig, HalEventData eventData) {
        try {
            DBConnection db = HalContext.getDB();
            Event event = findEvent(eventConfig, registeredEvents);

            if (event != null) {
                logger.finest("Received report from event("+eventConfig.getClass().getSimpleName()+"): "+ eventConfig);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO event_data_raw (timestamp, event_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, eventData.getTimestamp());
                stmt.setLong(2, event.getId());
                stmt.setDouble(3, eventData.getData());
                DBConnection.exec(stmt);
            }
            else { // unknown sensor
                logger.info("Received report from unregistered event" +
                        "("+eventConfig.getClass().getSimpleName()+"): "+ eventConfig);
                event = findEvent(eventConfig, detectedEvents);
                if(event == null) {
                    event = new Event();
                    detectedEvents.add(event);
                }
            }
            event.setDeviceConfig(eventConfig); // Set the latest data

        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store event report", e);
        }
    }

    private static Event findEvent(HalEventConfig eventData, List<Event> list){
        for (int i=0; i<list.size(); ++i) { // Don't use foreach for concurrency reasons
            Event e = list.get(i);
            if (eventData.equals(e.getDeviceConfig())) {
                return e;
            }
        }
        return null;
    }

    public void send(Event event){
        HalEventController controller = getControllerInstance(event.getController());
        if(controller != null) {
            controller.send(event.getDeviceConfig());
            reportReceived(event.getDeviceConfig(), event.getDeviceData()); // save action to db
        }
        else
            logger.warning("No controller found for event id: "+ event.getId());
    }

    /////////////////////////////// GENERAL ///////////////////////////////////

    @Override
    public void preConfigurationAction(Configurator configurator, Object obj) {
        if(obj instanceof HalSensorConfig) {
            Sensor sensor = findSensor((HalSensorConfig) obj, registeredSensors);
            if(sensor != null){
                deregister(sensor);
                limboSensors.add(sensor);
            }
        }
        else if(obj instanceof HalEventConfig) {
            Event event = findEvent((HalEventConfig) obj, registeredEvents);
            if(event != null){
                deregister(event);
                limboEvents.add(event);
            }
        }
    }

    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        if(obj instanceof HalSensorConfig) {
            Sensor sensor = findSensor((HalSensorConfig) obj, limboSensors);
            if(sensor != null){
                register(sensor);
                limboSensors.remove(sensor);
            }
        }
        else if(obj instanceof HalEventConfig) {
            Event event = findEvent((HalEventConfig) obj, limboEvents);
            if(event != null){
                register(event);
                limboEvents.remove(event);
            }
        }
    }

    private <T> T getControllerInstance(Class<T> c){
        Object controller;
        if (controllerMap.containsKey(c))
            controller = controllerMap.get(c);
        else {
            // Instantiate controller
            try {
                controller = c.newInstance();
                if (controller instanceof HalAutoScannableController &&
                        ! ((HalAutoScannableController)controller).isAvailable()) {
                    logger.warning("Controller not available: "+c.getName());
                    return null;
                }
                logger.info("Instantiating new controller: " + c.getName());

                if(controller instanceof HalSensorController) {
                    ((HalSensorController) controller).setListener(this);
                    ((HalSensorController) controller).initialize();
                }
                if(controller instanceof HalEventController) {
                    ((HalEventController) controller).setListener(this);
                    if( ! (controller instanceof HalSensorController))
                        ((HalEventController) controller).initialize();
                }

                controllerMap.put(c, controller);
            } catch (Exception e){
                logger.log(Level.SEVERE, "Unable to instantiate controller: "+c.getName(), e);
                return null;
            }
        }
        return (T)controller;
    }

    private void removeControllerIfEmpty(Object controller){
        if (controller instanceof HalAutoScannableController)
            return; // Don't do anything if controller is scannable

        int size = Integer.MAX_VALUE;
        if(controller instanceof HalSensorController)
            size = ((HalSensorController) controller).size();
        else if(controller instanceof HalEventController)
            size = ((HalEventController) controller).size();

        if(size < 0){
            // Remove controller as it has no more registered sensors
            logger.info("Closing controller as it has no more registered devices: "+controller.getClass().getName());
            controllerMap.remove(controller.getClass());

            if(controller instanceof HalSensorController)
                ((HalSensorController) controller).close();
            else if(controller instanceof HalEventController)
                ((HalEventController) controller).close();
        }
    }




    public static void initialize(PluginManager pluginManager){
        ControllerManager manager = new ControllerManager();

        for (Iterator<Class<? extends HalSensorConfig>> it = pluginManager.getClassIterator(HalSensorConfig.class);
             it.hasNext(); ){
            manager.availableSensors.add(it.next());
        }

        for (Iterator<Class<? extends HalEventConfig>> it = pluginManager.getClassIterator(HalEventConfig.class);
             it.hasNext(); ){
            manager.availableEvents.add(it.next());
        }

        for (Iterator<Class<? extends HalAutoScannableController>> it=
                    pluginManager.getClassIterator(HalAutoScannableController.class);
                it.hasNext(); ){
            manager.getControllerInstance(it.next()); // Instantiate controller
        }

        instance = manager;
    }

    public static ControllerManager getInstance(){
        return instance;
    }

}
