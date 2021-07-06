package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.plugin.zigbee.ZigbeeController;
import zutil.ui.conf.Configurator;

import java.util.Objects;

/**
 * A generic class that is extended by all Endpoint config classes.
 */
public abstract class ZigbeeHalDeviceConfig implements HalDeviceConfig {
    @Configurator.Configurable(value = "Node IeeeAddress")
    private String zigbeeNodeAddressStr;


    public void setZigbeeNodeAddress(IeeeAddress zigbeeNodeAddress) {
        this.zigbeeNodeAddressStr = zigbeeNodeAddress.toString();
    }

    public IeeeAddress getZigbeeNodeAddress() {
        return new IeeeAddress(zigbeeNodeAddressStr);
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
        return ZigbeeController.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZigbeeHalDeviceConfig)) return false;

        ZigbeeHalDeviceConfig that = (ZigbeeHalDeviceConfig) o;
        return Objects.equals(zigbeeNodeAddressStr, that.zigbeeNodeAddressStr);
    }


    @Override
    public String toString() {
        return "Address: " + getZigbeeNodeAddress() + "; Cluster ID: " + getZigbeeClusterId();
    }
}
