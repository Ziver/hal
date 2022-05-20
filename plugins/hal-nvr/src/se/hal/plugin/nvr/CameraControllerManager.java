package se.hal.plugin.nvr;

import se.hal.HalContext;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalEventController;
import se.hal.plugin.nvr.intf.HalCameraConfig;
import se.hal.plugin.nvr.intf.HalCameraController;
import se.hal.plugin.nvr.struct.Camera;
import se.hal.struct.Sensor;
import se.hal.util.HalDeviceUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CameraControllerManager extends HalAbstractControllerManager<HalCameraController, Camera, HalCameraConfig> {
    private static final Logger logger = LogUtil.getLogger();
    private static CameraControllerManager instance;

    /** List of all registered cameras **/
    private List<Camera> registeredCameras = Collections.synchronizedList(new ArrayList<>());


    public void initialize(PluginManager pluginManager){
        super.initialize(pluginManager);
        instance = this;

        // Read in existing devices

        try {
            DBConnection db = HalContext.getDB();

            logger.info("Reading in existing cameras.");

            for (Camera camera : Camera.getCameras(db)) {
                register(camera);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to read in existing cameras.", e);
        }
    }


    @Override
    public void register(Camera camera) {
        if (camera.getDeviceConfig() == null) {
            logger.warning("Camera config is null: " + camera);
            return;
        }
        if (!getAvailableDeviceConfigs().contains(camera.getDeviceConfig().getClass())) {
            logger.warning("Camera data plugin not available: " + camera.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new camera(id: " + camera.getId() + "): " + camera.getDeviceConfig().getClass());
        Class<? extends HalCameraController> controllerClass = (Class<? extends HalCameraController>) camera.getControllerClass();
        HalCameraController controller = getControllerInstance(controllerClass);

        if (controller != null)
            controller.register(camera.getDeviceConfig());

        registeredCameras.add(camera);
        //detectedCameras.remove(HalDeviceUtil.findDevice(camera.getDeviceConfig(), detectedCameras)); // Remove if this device was detected
    }

    @Override
    public void deregister(Camera camera) {
        if (camera.getDeviceConfig() == null) {
            logger.warning("Camera config is null: " + camera);
            return;
        }

        Class<? extends HalCameraController> controllerClass = (Class<? extends HalCameraController>) camera.getControllerClass();
        HalCameraController controller = (HalCameraController) controllerMap.get(controllerClass);
        if (controller != null) {
            logger.info("Deregistering camera(id: " + camera.getId() + "): " + camera.getDeviceConfig().getClass());
            controller.deregister(camera.getDeviceConfig());
            registeredCameras.remove(camera);
            removeControllerIfEmpty(controller);
        } else {
            logger.warning("Controller not instantiated: " + camera.getControllerClass());
        }
    }

    @Override
    public List<Camera> getRegisteredDevices() {
        return registeredCameras;
    }

    @Override
    public List<Camera> getDetectedDevices() {
        return Collections.EMPTY_LIST;
    }


    @Override
    public void clearDetectedDevices() {

    }

    public static CameraControllerManager getInstance(){
        return instance;
    }
}
