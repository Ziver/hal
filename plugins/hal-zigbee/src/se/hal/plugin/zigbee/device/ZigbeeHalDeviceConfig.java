package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.plugin.zigbee.HalZigbeeController;

/**
 * A generic class that is extended by all Endpoint config classes.
 */
public abstract class ZigbeeHalDeviceConfig implements HalDeviceConfig {
    private String zigbeeNodeAddressStr;
    private transient IeeeAddress zigbeeNodeAddress;


    public void setZigbeeNodeAddress(IeeeAddress zigbeeNodeAddress) {
        this.zigbeeNodeAddress = zigbeeNodeAddress;
        this.zigbeeNodeAddressStr = zigbeeNodeAddress.toString();
    }

    public IeeeAddress getZigbeeNodeAddress() {
        if (zigbeeNodeAddress == null && zigbeeNodeAddressStr != null)
            zigbeeNodeAddress = new IeeeAddress(zigbeeNodeAddressStr);
        return zigbeeNodeAddress;
    }

    // --------------------------
    // Abstract Methods
    // --------------------------

    /**
     * @param zclAttribute
     * @return a HalDeviceData object containing the same value representation as the endpoint.
     */
    public abstract HalDeviceData getDeviceData(ZclAttribute zclAttribute);

    /**
     * @return the cluster ID that is supported by this device config class
     */
    public abstract int getZigbeeClusterId();

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalAbstractController> getDeviceControllerClass() {
        return HalZigbeeController.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZigbeeHalDeviceConfig that = (ZigbeeHalDeviceConfig) o;
        return zigbeeNodeAddress.equals(that.zigbeeNodeAddress) &&
                getZigbeeClusterId() == that.getZigbeeClusterId();
    }


    @Override
    public String toString() {
        return "Address: " + getZigbeeNodeAddress() + "; Cluster ID: " + getZigbeeClusterId();
    }
}
