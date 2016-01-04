package se.koc.hal;

import se.koc.hal.intf.HalEvent;
import se.koc.hal.intf.HalEventController;
import se.koc.hal.intf.HalSensor;
import se.koc.hal.intf.HalSensorController;
import se.koc.hal.struct.Event;
import se.koc.hal.struct.Sensor;
import zutil.log.LogUtil;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class ControllerManager {
    private static final Logger logger = LogUtil.getLogger();
    private static ControllerManager instance;


    /** All available sensor plugins **/
    private ArrayList<Class<?>> availableSensors = new ArrayList<>();
    /** List of all registered sensors **/
    private ArrayList<Sensor> registeredSensors = new ArrayList<>();
    /** List of auto detected sensors **/
    private ArrayList<HalSensor> detectedSensors = new ArrayList<>();


    /** All available event plugins **/
    private ArrayList<Class<?>> availableEvents = new ArrayList<>();
    /** List of all registered events **/
    private ArrayList<Event> registeredEvents = new ArrayList<>();
    /** List of auto detected events **/
    private ArrayList<HalEvent> detectedEvents = new ArrayList<>();


    /** A map of all instantiated controllers **/
    private HashMap<Class,Object> controllerMap = new HashMap<>();



    /////////////////////////////// SENSORS ///////////////////////////////////

    public void register(Sensor sensor) throws IllegalAccessException, InstantiationException {
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(sensor.getSensorData());
        registeredSensors.add(sensor);
    }

    public void deregister(Sensor sensor){
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = (HalSensorController) controllerMap.get(c);;
        if (controller != null) {
            controller.deregister(sensor.getSensorData());
            registeredSensors.remove(sensor);
            removeControllerIfEmpty(controller);
        }
    }

    public List<Class<?>> getAvailableSensors(){
        return availableSensors;
    }

    public List<HalSensor> getDetectedSensors(){
        return detectedSensors;
    }


    //////////////////////////////// EVENTS ///////////////////////////////////

    public void register(Event event) throws IllegalAccessException, InstantiationException {
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(event.getEventData());
        registeredEvents.add(event);
    }

    public void deregister(Event event){
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = (HalEventController) controllerMap.get(c);
        if (controller != null) {
            controller.deregister(event.getEventData());
            registeredEvents.remove(event);
            removeControllerIfEmpty(controller);
        }
    }

    public List<Class<?>> getAvailableEvents(){
        return availableEvents;
    }

    public List<HalEvent> getDetectedEvents(){
        return detectedEvents;
    }


    /////////////////////////////// GENERAL ///////////////////////////////////

    private <T> T getControllerInstance(Class<T> c){
        Object controller = null;
        if (controllerMap.containsKey(c))
            controller = controllerMap.get(c);
        else {
            // Instantiate controller
            logger.fine("Instantiating new controller: " + c.getName());
            try {
                controller = c.newInstance();
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
            logger.fine("Closing controller as it has no more registered sensors: "+controller.getClass().getName());
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
            Iterator<Class<?>> pluginIt = plugin.getClassIterator(HalSensor.class);
            while (pluginIt.hasNext()){
                manager.availableSensors.add(pluginIt.next());
            }

            pluginIt = plugin.getClassIterator(HalEvent.class);
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
