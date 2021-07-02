package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.clusters.ZclRelativeHumidityMeasurementCluster;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalSensorConfig;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.TemperatureSensorData;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeHumidityConfig extends ZigbeeHalDeviceConfig implements HalSensorConfig {

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        return new HumiditySensorData(((int) zclAttribute.getLastValue()) / 100.0, zclAttribute.getLastReportTime().getTimeInMillis());
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclRelativeHumidityMeasurementCluster.CLUSTER_ID;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public long getDataInterval() {
        return 0;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return HumiditySensorData.class;
    }
}
