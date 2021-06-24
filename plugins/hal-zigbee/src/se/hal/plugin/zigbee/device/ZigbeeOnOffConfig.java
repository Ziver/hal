package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
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

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeOnOffConfig extends ZigbeeHalEventDeviceConfig implements HalEventConfig {

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
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
