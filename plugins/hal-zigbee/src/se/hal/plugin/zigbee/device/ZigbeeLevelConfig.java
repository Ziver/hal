package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.clusters.ZclLevelControlCluster;
import com.zsmartsystems.zigbee.zcl.clusters.levelcontrol.MoveToLevelCommand;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.struct.devicedata.LevelEventData;
import zutil.log.LogUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeLevelConfig extends ZigbeeHalEventDeviceConfig implements HalEventConfig {
    private static final Logger logger = LogUtil.getLogger();

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public void initialize(ZclCluster cluster) {
        if (! (cluster instanceof ZclLevelControlCluster))
            return;

        try {
            ZclAttribute attribute = cluster.getAttribute(ZclLevelControlCluster.ATTR_CURRENTLEVEL);
            //attribute.setReporting(1, 900).get();
            attribute.readValue(60);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Was unable to initialize cluster reporting rate.", e);
        }
    }

    @Override
    public HalDeviceData getDeviceData(ZigBeeEndpoint endpoint, ZclAttribute zclAttribute) {
        if (zclAttribute.getClusterType().getId() == getZigbeeClusterId() &&
                zclAttribute.getId() == ZclLevelControlCluster.ATTR_CURRENTLEVEL)
            return new LevelEventData(
                    ((int) zclAttribute.getLastValue()) / 255.0,
                    zclAttribute.getLastReportTime().getTimeInMillis());
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclLevelControlCluster.CLUSTER_ID;
    }

    @Override
    protected ZclCommand getZigbeeCommandObject(HalEventData data) {
        if (! (data instanceof LevelEventData))
            return null;

        return new MoveToLevelCommand((int) (data.getData() * 255), 1);
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return LevelEventData.class;
    }
}
