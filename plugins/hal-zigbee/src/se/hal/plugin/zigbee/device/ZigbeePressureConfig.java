package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.clusters.ZclTemperatureMeasurementCluster;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalSensorConfig;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.PressureSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeePressureConfig extends ZigbeeHalDeviceConfig implements HalSensorConfig {

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public HalDeviceData getDeviceData(ZclAttribute zclAttribute) {
        return new PressureSensorData(((int) zclAttribute.getLastValue()), zclAttribute.getLastReportTime().getTimeInMillis());
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclTemperatureMeasurementCluster.CLUSTER_ID;
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
        return TemperatureSensorData.class;
    }
}
