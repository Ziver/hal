package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
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


    public ZclCluster getZigbeeCluster(ZigbeeController controller) {
        ZigBeeNode node = controller.getNode(getZigbeeNodeAddress());

        for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
            ZclCluster cluster = endpoint.getInputCluster(getZigbeeClusterId());
            if (cluster != null) {
                return cluster;
            }
        }

        return null;
    }

    // --------------------------
    // Abstract Methods
    // --------------------------

    /**
     * Method will configured a newly discovered or updated cluster. Method by default
     * does nothing and will need to be extended by subclasses to add specific initialization logic.
     *
     * @param cluster is the cluster to be initialized.
     */
    public void initialize(ZclCluster cluster) {}

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
        return this.getClass() == that.getClass() &&
                Objects.equals(zigbeeNodeAddressStr, that.zigbeeNodeAddressStr) &&
                this.getZigbeeClusterId() == that.getZigbeeClusterId();
    }


    @Override
    public String toString() {
        return "Address: " + getZigbeeNodeAddress() + "; Cluster ID: " + getZigbeeClusterId();
    }
}
