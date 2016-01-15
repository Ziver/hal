package se.hal;

import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class ControllerManager implements HalSensorReportListener, HalEventReportListener {
    private static final Logger logger = LogUtil.getLogger();
    private static ControllerManager instance;


    /** All available sensor plugins **/
    private ArrayList<Class<?>> availableSensors = new ArrayList<>();
    /** List of all registered sensors **/
    private ArrayList<Sensor> registeredSensors = new ArrayList<>();
    /** List of auto detected sensors **/
    private ArrayList<HalSensorData> detectedSensors = new ArrayList<>();


    /** All available event plugins **/
    private ArrayList<Class<?>> availableEvents = new ArrayList<>();
    /** List of all registered events **/
    private ArrayList<Event> registeredEvents = new ArrayList<>();
    /** List of auto detected events **/
    private ArrayList<HalEventData> detectedEvents = new ArrayList<>();


    /** A map of all instantiated controllers **/
    private HashMap<Class,Object> controllerMap = new HashMap<>();



    /////////////////////////////// SENSORS ///////////////////////////////////

    public void register(Sensor sensor) throws IllegalAccessException, InstantiationException {
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

    public List<HalSensorData> getDetectedSensors(){
        return detectedSensors;
    }

    @Override
    public void reportReceived(HalSensorData sensorData) {
        try{
            DBConnection db = HalContext.getDB();
            Sensor sensor = null;
            for (Sensor s : registeredSensors) {
                if (sensorData.equals(s.getDeviceData())) {
                    sensor = s;
                    sensor.setDeviceData(sensorData); // Set the latest data
                    break;
                }
            }

            if (sensor != null) {
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, sensor_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, sensorData.getTimestamp());
                stmt.setLong(2, sensor.getId());
                stmt.setDouble(3, sensorData.getData());
                db.exec(stmt);
                logger.finest("Received report from sensor: "+ sensorData);
            }
            else { // unknown sensor
                logger.finest("Received report from unregistered sensor: "+ sensorData);
                if(!detectedSensors.contains(sensorData)) {
                    detectedSensors.add(sensorData);
                }
            }
        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store sensor report", e);
        }
    }

    //////////////////////////////// EVENTS ///////////////////////////////////

    public void register(Event event) throws IllegalAccessException, InstantiationException {
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

    public List<HalEventData> getDetectedEvents(){
        return detectedEvents;
    }

    @Override
    public void reportReceived(HalEventData eventData) {
        try {
            DBConnection db = HalContext.getDB();
            Event event = null;
            for (Event e : registeredEvents) {
                if (eventData.equals(e.getDeviceData())) {
                    event = e;
                    event.setDeviceData(eventData); // Set the latest data
                    break;
                }
            }

            if (event != null) {
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO event_data_raw (timestamp, event_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, eventData.getTimestamp());
                stmt.setLong(2, event.getId());
                stmt.setDouble(3, eventData.getData());
                db.exec(stmt);
                logger.finest("Received report from event: "+ eventData);
            }
            else { // unknown sensor
                logger.info("Received report from unregistered event: "+ eventData);
                if(!detectedEvents.contains(eventData)) {
                    detectedEvents.add(eventData);
                }
            }
        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store event report", e);
        }
    }

    /////////////////////////////// GENERAL ///////////////////////////////////

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
            logger.info("Closing controller as it has no more registered objects: "+controller.getClass().getName());
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
