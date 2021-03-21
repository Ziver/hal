package se.hal.plugin.nvr;

import se.hal.intf.HalAbstractControllerManager;
import se.hal.plugin.nvr.intf.HalCameraConfig;
import se.hal.plugin.nvr.intf.HalCameraController;
import se.hal.plugin.nvr.struct.Camera;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


public class CameraControllerManager extends HalAbstractControllerManager<HalCameraController, Camera, HalCameraConfig> {
    private static final Logger logger = LogUtil.getLogger();
    private static CameraControllerManager instance;


    @Override
    public void register(Camera device) {

    }

    @Override
    public void deregister(Camera device) {

    }

    @Override
    public List<Class<? extends HalCameraConfig>> getAvailableDeviceConfigs() {
        return null;
    }

    @Override
    public List<Camera> getRegisteredDevices() {
        return null;
    }

    @Override
    public List<Camera> getDetectedDevices() {
        return null;
    }

    @Override
    public void addAvailableDevice(Class deviceConfigClass) {

    }

    @Override
    public void clearDetectedDevices() {

    }

    @Override
    public Collection<HalCameraController> getControllers() {
        return null;
    }


    public void initialize(PluginManager pluginManager){
        super.initialize(pluginManager);

        instance = this;
    }

    public static CameraControllerManager getInstance(){
        return instance;
    }
}
