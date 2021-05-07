package se.hal.util;

import se.hal.HalServer;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalAbstractDevice;
import se.hal.intf.HalDeviceConfig;
import zutil.ui.conf.Configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A listener implementation that will deregister a device and then re-register it on all interested managers.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HalDeviceChangeListener<C extends HalDeviceConfig> implements Configurator.PreConfigurationActionListener<C>, Configurator.PostConfigurationActionListener<C> {
    /** List of sensors that are currently being reconfigured **/
    private Map<HalAbstractDevice, List<HalAbstractControllerManager>> limboDevices = new HashMap<>();


    @Override
    public void preConfigurationAction(Configurator configurator, HalDeviceConfig deviceConfig) {
        List<HalAbstractControllerManager> managers = HalServer.getControllerManagers();

        for (HalAbstractControllerManager manager : managers) {
            HalAbstractDevice device = HalDeviceUtil.findDevice(deviceConfig, manager.getRegisteredDevices());
            if (device != null) {
                manager.deregister(device);

                if (!limboDevices.containsKey(device))
                    limboDevices.put(device, new ArrayList<>(2));
                limboDevices.get(device).add(manager);
            }
        }
    }

    @Override
    public void postConfigurationAction(Configurator configurator, HalDeviceConfig deviceConfig) {
        HalAbstractDevice device = HalDeviceUtil.findDevice(deviceConfig, new ArrayList<>(limboDevices.keySet()));
        List<HalAbstractControllerManager> managers = limboDevices.get(device);

        if (managers != null) {
            for (HalAbstractControllerManager manager : managers) {
                manager.register(device);
            }

            limboDevices.remove(device);
        }
    }
}