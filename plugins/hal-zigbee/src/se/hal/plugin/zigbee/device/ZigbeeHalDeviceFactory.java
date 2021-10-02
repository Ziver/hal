package se.hal.plugin.zigbee.device;

import zutil.log.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory class for getting new instances of {@link ZigbeeHalDeviceConfig} objects.
 */
public class ZigbeeHalDeviceFactory {
    private static final Logger logger = LogUtil.getLogger();

    private static final HashMap<Integer, Class<? extends ZigbeeHalDeviceConfig>> clusterDeviceMap = new HashMap<>();
    static {
        clusterDeviceMap.put(new ZigbeeHumidityConfig().getZigbeeClusterId(), ZigbeeHumidityConfig.class);
        clusterDeviceMap.put(new ZigbeeIlluminanceConfig().getZigbeeClusterId(), ZigbeeIlluminanceConfig.class);
        clusterDeviceMap.put(new ZigbeeOccupancyConfig().getZigbeeClusterId(), ZigbeeOccupancyConfig.class);
        clusterDeviceMap.put(new ZigbeeOnOffConfig().getZigbeeClusterId(), ZigbeeOnOffConfig.class);
        clusterDeviceMap.put(new ZigbeePressureConfig().getZigbeeClusterId(), ZigbeePressureConfig.class);
        clusterDeviceMap.put(new ZigbeeTemperatureConfig().getZigbeeClusterId(), ZigbeeTemperatureConfig.class);
    }


    public static ZigbeeHalDeviceConfig getDeviceConfig(int cluster) {
        try {
            Class<? extends ZigbeeHalDeviceConfig> clazz = clusterDeviceMap.get(cluster);

            if (clazz != null) {
                return clazz.getDeclaredConstructor().newInstance();
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Unable to instantiate ZigbeeHalDeviceConfig: " + clusterDeviceMap.get(cluster), e);
        }

        return null;
    }
}
