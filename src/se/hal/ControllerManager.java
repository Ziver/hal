package se.hal;

import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;
import zutil.ui.Configurator;
import zutil.ui.Configurator.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class ControllerManager implements HalSensorReportListener,
        HalEventReportListener,
        PreConfigurationActionListener,
        PostConfigurationActionListener {
    private static final Logger logger = LogUtil.getLogger();
    private static ControllerManager instance;


    /** All available sensor plugins **/
    private List<Class<?>> availableSensors = new ArrayList<>();
    /** List of all registered sensors **/
    private List<Sensor> registeredSensors = new ArrayList<>();
    /** List of auto detected sensors **/
    private List<Sensor> detectedSensors = new ArrayList<>();
    /** List of sensors that are currently being reconfigured **/
    private List<Sensor> limboSensors = new LinkedList<>();


    /** All available event plugins **/
    private List<Class<?>> availableEvents = new ArrayList<>();
    /** List of all registered events **/
    private List<Event> registeredEvents = new ArrayList<>();
    /** List of auto detected events **/
    private List<Event> detectedEvents = new ArrayList<>();
    /** List of all registered events **/
    private List<Event> limboEvents = new LinkedList<>();


    /** A map of all instantiated controllers **/
    private HashMap<Class,Object> controllerMap = new HashMap<>();



    /////////////////////////////// SENSORS ///////////////////////////////////

    public void register(Sensor sensor) {
        if(sensor.getDeviceData() == null) {
            logger.warning("Sensor data is null: "+ sensor);
            return;
        }
        if(!availableSensors.contains(sensor.getDeviceData().getClass())) {
            logger.warning("Sensor data plugin not available: "+ sensor.getDeviceData().getClass());
            return;
        }

        logger.info("Registering new sensor(id: "+ sensor.getId() +"): "+ sensor.getDeviceData().getClass());
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(sensor.getDeviceData());
        registeredSensors.add(sensor);
        detectedSensors.remove(findSensor(sensor.getDeviceData(), detectedSensors)); // Remove if this device was detected
    }

    public void deregister(Sensor sensor){
        if(sensor.getDeviceData() == null) {
            logger.warning("Sensor data is null: "+ sensor);
            return;
        }

        logger.info("Deregistering sensor(id: "+ sensor.getId() +"): "+ sensor.getDeviceData().getClass());
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = (HalSensorController) controllerMap.get(c);;
        if (controller != null) {
            controller.deregister(sensor.getDeviceData());
            registeredSensors.remove(sensor);
            removeControllerIfEmpty(controller);
        }
    }

    public List<Class<?>> getAvailableSensors(){
        return availableSensors;
    }

    public List<Sensor> getDetectedSensors(){
        return detectedSensors;
    }

    @Override
    public void reportReceived(HalSensorData sensorData) {
        try{
            DBConnection db = HalContext.getDB();
            Sensor sensor = findSensor(sensorData, registeredSensors);

            if (sensor != null) {
                logger.finest("Received report from sensor: "+ sensorData);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, sensor_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, sensorData.getTimestamp());
                stmt.setLong(2, sensor.getId());
                stmt.setDouble(3, sensorData.getData());
                db.exec(stmt);
            }
            else { // unknown sensor
                logger.finest("Received report from unregistered sensor: "+ sensorData);
                sensor = findSensor(sensorData, detectedSensors);
                if(sensor == null) {
                    sensor = new Sensor();
                    detectedSensors.add(sensor);
                }
            }
            sensor.setDeviceData(sensorData); // Set the latest data

        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store sensor report", e);
        }
    }

    private static Sensor findSensor(HalSensorData sensorData, List<Sensor> list){
        for (Sensor s : list) {
            if (sensorData.equals(s.getDeviceData())) {
                return s;
            }
        }
        return null;
    }

    //////////////////////////////// EVENTS ///////////////////////////////////

    public void register(Event event) {
        if(event.getDeviceData() == null) {
            logger.warning("Sensor data is null: "+ event);
            return;
        }
        if(!availableEvents.contains(event.getDeviceData().getClass())) {
            logger.warning("Sensor data plugin not available: "+ event.getDeviceData().getClass());
            return;
        }

        logger.info("Registering new event(id: "+ event.getId() +"): "+ event.getDeviceData().getClass());
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(event.getDeviceData());
        registeredEvents.add(event);
        detectedEvents.remove(findEvent(event.getDeviceData(), detectedEvents)); // Remove if this device was detected
    }

    public void deregister(Event event){
        if(event.getDeviceData() == null) {
            logger.warning("Sensor data is null: "+ event);
            return;
        }

        logger.info("Deregistering event(id: "+ event.getId() +"): "+ event.getDeviceData().getClass());
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = (HalEventController) controllerMap.get(c);
        if (controller != null) {
            controller.deregister(event.getDeviceData());
            registeredEvents.remove(event);
            removeControllerIfEmpty(controller);
        }
    }

    public List<Class<?>> getAvailableEvents(){
        return availableEvents;
    }

    public List<Event> getDetectedEvents(){
        return detectedEvents;
    }

    @Override
    public void reportReceived(HalEventData eventData) {
        try {
            DBConnection db = HalContext.getDB();
            Event event = findEvent(eventData, registeredEvents);

            if (event != null) {
                logger.finest("Received report from event: "+ eventData);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO event_data_raw (timestamp, event_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, eventData.getTimestamp());
                stmt.setLong(2, event.getId());
                stmt.setDouble(3, eventData.getData());
                db.exec(stmt);
            }
            else { // unknown sensor
                logger.info("Received report from unregistered event: "+ eventData);
                event = findEvent(eventData, detectedEvents);
                if(event == null) {
                    event = new Event();
                    detectedEvents.add(event);
                }
            }
            event.setDeviceData(eventData); // Set the latest data

        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store event report", e);
        }
    }

    private static Event findEvent(HalEventData eventData, List<Event> list){
        for (Event e : list) {
            if (eventData.equals(e.getDeviceData())) {
                return e;
            }
        }
        return null;
    }

    public void send(Event event){
        HalEventController controller = getControllerInstance(event.getController());
        if(controller != null) {
            controller.send(event.getDeviceData());
            reportReceived(event.getDeviceData()); // save action to db
        }
        else
            logger.warning("No controller found for event id: "+ event.getId());
    }

    /////////////////////////////// GENERAL ///////////////////////////////////
    @Override
    public void preConfigurationAction(Configurator configurator, Object obj) {
        if(obj instanceof HalSensorData) {
            Sensor sensor = findSensor((HalSensorData) obj, registeredSensors);
            if(sensor != null){
                deregister(sensor);
                limboSensors.add(sensor);
            }
        }
        else if(obj instanceof HalEventController) {
            Event event = findEvent((HalEventData) obj, registeredEvents);
            if(event != null){
                deregister(event);
                limboEvents.add(event);
            }
        }
    }

    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        if(obj instanceof HalSensorController) {
            Sensor sensor = findSensor((HalSensorData) obj, limboSensors);
            if(sensor != null){
                register(sensor);
                limboSensors.remove(sensor);
            }
        }
        else if(obj instanceof HalEventController) {
            Event event = findEvent((HalEventData) obj, limboEvents);
            if(event != null){
                register(event);
                limboEvents.remove(event);
            }
        }
    }

    private <T> T getControllerInstance(Class<T> c){
        Object controller = null;
        if (controllerMap.containsKey(c))
            controller = controllerMap.get(c);
        else {
            // Instantiate controller
            logger.info("Instantiating new controller: " + c.getName());
            try {
                controller = c.newInstance();
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
            }
        }
        return (T)controller;
    }

    private void removeControllerIfEmpty(Object controller){
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




    public static void initialize(){
        ControllerManager manager = new ControllerManager();
        PluginManager pluginManager = new PluginManager("./");
        Iterator<PluginData> it = pluginManager.iterator();
        while (it.hasNext()){
            PluginData plugin = it.next();
            Iterator<Class<?>> pluginIt = plugin.getClassIterator(HalSensorData.class);
            while (pluginIt.hasNext()){
                manager.availableSensors.add(pluginIt.next());
            }

            pluginIt = plugin.getClassIterator(HalEventData.class);
            while (pluginIt.hasNext()){
                manager.availableEvents.add(pluginIt.next());
            }
        }
        instance = manager;
    }

    public static ControllerManager getInstance(){
        return instance;
    }

}
