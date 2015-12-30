package se.koc.hal;

import net.didion.jwnl.data.Exc;
import se.koc.hal.intf.HalSensorController;
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


    private ArrayList<Class<?>> availableSensors = new ArrayList<>();
    private HashMap<Class,HalSensorController> controllerMap = new HashMap<>();



    public void register(Sensor sensor) throws IllegalAccessException, InstantiationException {
        Class<? extends HalSensorController> c = sensor.getController();
        HalSensorController controller = null;
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

        if(controller != null)
            controller.register(sensor);
    }

    public void deregister(Sensor sensor){
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
            Iterator<Class<?>> pluginIt = plugin.getClassIterator(Sensor.class);
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
