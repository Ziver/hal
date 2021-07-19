package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.CommandResult;
import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import se.hal.intf.HalEventData;
import zutil.log.LogUtil;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic class that is extended by all Endpoint config classes.
 */
public abstract class ZigbeeHalEventDeviceConfig extends ZigbeeHalDeviceConfig {
    private static final Logger logger = LogUtil.getLogger();


    public ZclCluster getZigbeeCluster(ZigBeeNetworkManager networkManager) {
        ZigBeeNode node = networkManager.getNode(getZigbeeNodeAddress());

        for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
            ZclCluster cluster = endpoint.getInputCluster(getZigbeeClusterId());
            if (cluster != null) {
                return cluster;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public void sendZigbeeCommand(ZigBeeNetworkManager networkManager, HalEventData data) {
        ZclCluster cluster = getZigbeeCluster(networkManager);

        if (cluster != null) {
            try {
                ZclCommand command = getZigbeeCommandObject(data);

                // Need to do reflection as the generic method has visibility protected.

                Method method = ZclCluster.class.getDeclaredMethod("sendCommand", ZclCommand.class);
                method.setAccessible(true);
                CommandResult result = ((Future<CommandResult>) method.invoke(cluster, command)).get();

                if (result.isError() || result.isTimeout()) {
                    logger.warning("[Node: " + getZigbeeNodeAddress() + ", Endpoint: " + cluster.getZigBeeAddress() + "] Command failed with error: " + result.isError() + " (timeout=" + result.isTimeout() + ")");
                } else {
                    logger.info("[Node: " + getZigbeeNodeAddress() + ", Endpoint: " + cluster.getZigBeeAddress() + "] Command has been successfully sent.");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "[Node: " + getZigbeeNodeAddress() + ", Endpoint: " + cluster.getZigBeeAddress() + "] Failed to send command.", e);
            }
        } else {
            logger.warning("[Node: " + getZigbeeNodeAddress() + "] Unable to find cluster: " + getZigbeeClusterId());
        }
    }

    /**
     * Method will create a Zigbee command object based on the value of the Hal event data.
     *
     * @param data  is the Hal event data value that should be converted.
     * @return a new Zigbee command object or null if no equal representation can be created based on the data.
     */
    protected abstract ZclCommand getZigbeeCommandObject(HalEventData data);

}
