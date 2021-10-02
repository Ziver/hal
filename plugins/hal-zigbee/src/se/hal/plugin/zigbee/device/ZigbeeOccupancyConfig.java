package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.clusters.ZclOccupancySensingCluster;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventConfig;
import se.hal.struct.devicedata.OccupancyEventData;
import zutil.log.LogUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeOccupancyConfig extends ZigbeeHalEventDeviceConfig implements HalEventConfig {
    private static final Logger logger = LogUtil.getLogger();

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public void initialize(ZclCluster cluster) {
        if (! (cluster instanceof ZclOccupancySensingCluster))
            return;

        try {
            ZclAttribute attribute = cluster.getAttribute(ZclOccupancySensingCluster.ATTR_OCCUPANCY);
            attribute.setReporting(1, 900).get();
            attribute.readValue(60);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Was unable to initialize cluster reporting rate.", e);
        }
    }

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        if (zclAttribute.getCluster().getId() == getZigbeeClusterId() &&
                zclAttribute.getId() == ZclOccupancySensingCluster.ATTR_OCCUPANCY)
            return new OccupancyEventData(
                    (boolean) zclAttribute.getLastValue(),
                    zclAttribute.getLastReportTime().getTimeInMillis());
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclOccupancySensingCluster.CLUSTER_ID;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return OccupancyEventData.class;
    }
}
