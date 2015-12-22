package se.koc.hal;

import se.koc.hal.intf.HalSensorController;
import se.koc.hal.struct.HalEvent;
import se.koc.hal.struct.HalSensor;
import zutil.log.LogUtil;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class ControllerManager {
    private static final Logger logger = LogUtil.getLogger();
    private static ControllerManager instance;


    private ArrayList<Class<?>> availableSensors = new ArrayList<>();
    private HashMap<Class,HalSensorController> controllerMap = new HashMap<>();



    public void register(HalSensor sensor) throws IllegalAccessException, InstantiationException {
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller;
        if (controllerMap.containsKey(c))
            controller = controllerMap.get(c);
        else {
            // Instantiate controller
            logger.fine("Instantiating controller: " + c.getName());
            controller = c.newInstance();
            controllerMap.put(c, controller);
        }

        controller.register(sensor);
    }

    public void deregister(HalSensor sensor){
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller;
        if (controllerMap.containsKey(c)) {
            controller = controllerMap.get(c);
            controller.deregister(sensor);
            if(controller.size() == 0){
                // Remove controller as it has no more registered sensors
                logger.fine("Closing controller as it has no more registered sensors: "+c.getName());
                controller.close();
                controllerMap.remove(c);
            }
        }
    }

    public List<Class<?>> getAvailableSensors(){
        return availableSensors;
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
        }
        instance = manager;
    }

    public static ControllerManager getInstance(){
        return instance;
    }
}
