package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.clusters.ZclIlluminanceMeasurementCluster;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalSensorConfig;
import se.hal.struct.devicedata.IlluminanceSensorData;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeIlluminanceConfig extends ZigbeeHalDeviceConfig implements HalSensorConfig {

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        if (zclAttribute.getCluster().getId() == getZigbeeClusterId() &&
                zclAttribute.getId() == ZclIlluminanceMeasurementCluster.ATTR_MEASUREDVALUE)
            return new IlluminanceSensorData(
                    (int) zclAttribute.getLastValue(),
                    zclAttribute.getLastReportTime().getTimeInMillis());
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclIlluminanceMeasurementCluster.CLUSTER_ID;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return IlluminanceSensorData.class;
    }
}
