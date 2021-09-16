package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.clusters.ZclTemperatureMeasurementCluster;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalSensorConfig;
import se.hal.struct.devicedata.TemperatureSensorData;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeTemperatureConfig extends ZigbeeHalDeviceConfig implements HalSensorConfig {


    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        if (zclAttribute.getCluster().getId() == getZigbeeClusterId() &&
                zclAttribute.getId() == ZclTemperatureMeasurementCluster.ATTR_MEASUREDVALUE)
            return new TemperatureSensorData(
                    ((int) zclAttribute.getLastValue()) / 100.0,
                    zclAttribute.getLastReportTime().getTimeInMillis());
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclTemperatureMeasurementCluster.CLUSTER_ID;
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
        return TemperatureSensorData.class;
    }
}
