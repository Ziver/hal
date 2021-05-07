package se.hal.intf;

import zutil.ClassUtil;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @param <T>   is the device controller class
 * @param <V>   is the device class
 * @param <C>   is the device configuration class
 */
public abstract class HalAbstractControllerManager<T extends HalAbstractController, V extends HalAbstractDevice, C extends HalDeviceConfig> {
    private static final Logger logger = LogUtil.getLogger();

    /** A map of all instantiated controllers **/
    protected HashMap<Class, T> controllerMap = new HashMap<>();


    /**
     * Will instantiate a generic ControllerManager.
     *
     * @param pluginManager     a PluginManager instance that will be used to find Controller plugins.
     */
    public void initialize(PluginManager pluginManager) {
        Class[] genericClasses = ClassUtil.getGenericClasses(HalAbstractControllerManager.class);

        if (genericClasses.length >= 1 && genericClasses[0] != null) {
            for (Iterator<Class<C>> it = pluginManager.getClassIterator(genericClasses[0]); it.hasNext(); ) {
                addAvailableDeviceConfig(it.next());
            }
        } else {
            logger.severe("Unable to retrieve Controller class from generics.");
        }

        for (Iterator<Class<? extends HalAutoScannableController>> it = pluginManager.getClassIterator(HalAutoScannableController.class); it.hasNext(); ){
            Class controller = it.next();
            getControllerInstance(controller); // Instantiate controller
        }
    }

    // ----------------------------------------------------
    //                  Abstract methods
    // ----------------------------------------------------

    /**
     * Register a device instance on the manager.
     * The manager will start to track and save reported data for the registered device.
     */
    public abstract void register(V device);

    /**
     * Deregisters a device from the manager.
     * Data reported on the device will no longer be saved but already saved data will not be modified.
     * The Controller that owns the device will be deallocated if it has no more registered devices.
     */
    public abstract void deregister(V device);

    /**
     * Registers a device configuration class type as usable by the manager
     */
    public abstract void addAvailableDeviceConfig(Class<? extends C> deviceConfigClass);

    /**
     * @return a List of all available devices that can be registered with this manager
     */
    public abstract List<Class<? extends C>> getAvailableDeviceConfigs();

    /**
     * @return a List of device instances that have been registered on this manager
     */
    public abstract List<V> getRegisteredDevices();

    /**
     * @return a List of device instances that have reported data but have not yet been registered on the manager
     */
    public abstract List<V> getDetectedDevices();

    /**
     * Removes all auto detected devices.
     */
    public abstract void clearDetectedDevices();

    // ----------------------------------------------------
    //                   Common Logic
    // ----------------------------------------------------

    /**
     * @return all active instantiated controllers.
     */
    public Collection<T> getControllers() {
        return controllerMap.values();
    }

    /**
     * Will return a singleton controller instance of the given class.
     * If a instance does not exist yet the a new instance will be allocated
     * depending on if the controller is ready thorough the {@link HalAutoScannableController#isAvailable()} method.
     *
     * @param clazz     is the class of the wanted object instance wanted
     * @return A singleton instance of the input clazz or null if the class is unavailable or not ready to be instantiated.
     */
    protected T getControllerInstance(Class<? extends T> clazz){
        T controller;

        if (controllerMap.containsKey(clazz)) {
            controller = controllerMap.get(clazz);
        } else {
            try {
                // Instantiate controller
                controller = clazz.newInstance();

                if (controller instanceof HalAutoScannableController &&
                        ! ((HalAutoScannableController) controller).isAvailable()) {
                    logger.warning("Controller is not ready: " + clazz.getName());
                    return null;
                }

                logger.info("Instantiating new controller: " + clazz.getName());
                controller.initialize();

                if (this instanceof HalDeviceReportListener)
                    controller.setListener((HalDeviceReportListener)this);

                controllerMap.put(clazz, controller);
            } catch (Exception e){
                logger.log(Level.SEVERE, "Unable to instantiate controller: " + clazz.getName(), e);
                return null;
            }
        }

        return controller;
    }

    /**
     * Will check if a controller no longer has any managed devices,
     * in that case the controller will be deallocated.
     *
     * @param controller    is the controller instance.
     */
    protected void removeControllerIfEmpty(HalAbstractController controller){
        if (controller instanceof HalAutoScannableController)
            return; // Don't do anything if controller is scannable

        if (controller.size() < 0){
            // Remove controller as it has no more registered sensors
            logger.info("Closing controller as it has no more registered devices: " + controller.getClass().getName());
            controllerMap.remove(controller.getClass());

            controller.close();
        }
    }
}
