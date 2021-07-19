package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.clusters.ZclOnOffCluster;
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OffCommand;
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OnCommand;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.struct.devicedata.OnOffEventData;
import zutil.log.LogUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeOnOffConfig extends ZigbeeHalEventDeviceConfig implements HalEventConfig {
    private static final Logger logger = LogUtil.getLogger();

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public void initialize(ZclCluster cluster) {
        if (! (cluster instanceof ZclOnOffCluster))
            return;

        try {
            ZclAttribute attribute = cluster.getAttribute(ZclOnOffCluster.ATTR_ONOFF);
            attribute.setReporting(1, 900).get();
            attribute.readValue(60);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Was unable to initialize cluster reporting rate.", e);
        }
    }

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        if (zclAttribute.getId() == ZclOnOffCluster.ATTR_ONOFF)
            return new OnOffEventData(
                    (boolean) zclAttribute.getLastValue(),
                    zclAttribute.getLastReportTime().getTimeInMillis());
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclOnOffCluster.CLUSTER_ID;
    }

    @Override
    protected ZclCommand getZigbeeCommandObject(HalEventData data) {
        if (! (data instanceof OnOffEventData))
            return null;

        return (((OnOffEventData) data).isOn() ? new OnCommand() : new OffCommand());
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return OnOffEventData.class;
    }
}
