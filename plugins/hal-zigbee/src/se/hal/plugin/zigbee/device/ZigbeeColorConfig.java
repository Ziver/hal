package se.hal.plugin.zigbee.device;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import com.zsmartsystems.zigbee.zcl.clusters.ZclColorControlCluster;
import com.zsmartsystems.zigbee.zcl.clusters.colorcontrol.MoveToColorCommand;
import com.zsmartsystems.zigbee.zcl.clusters.colorcontrol.MoveToHueAndSaturationCommand;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.plugin.zigbee.util.Cie;
import se.hal.struct.devicedata.ColorEventData;
import zutil.log.LogUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A device configuration for a specific endpoint on a Zigbee device.
 */
public class ZigbeeColorConfig extends ZigbeeHalEventDeviceConfig implements HalEventConfig {
    private static final Logger logger = LogUtil.getLogger();

    // --------------------------
    // Zigbee Methods
    // --------------------------

    @Override
    public void initialize(ZclCluster cluster) {
        if (! (cluster instanceof ZclColorControlCluster))
            return;

        try {
            ZclAttribute attribute = cluster.getAttribute(ZclColorControlCluster.ATTR_CURRENTHUE);
            //attribute.setReporting(1, 900).get();
            attribute.readValue(60);

            attribute = cluster.getAttribute(ZclColorControlCluster.ATTR_CURRENTSATURATION);
            //attribute.setReporting(1, 900).get();
            attribute.readValue(60);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Was unable to initialize cluster reporting rate.", e);
        }
    }

    @Override
    public HalDeviceData getDeviceData(ZigBeeEndpoint endpoint, ZclAttribute zclAttribute) {
        if (zclAttribute.getClusterType().getId() == getZigbeeClusterId() &&
                (zclAttribute.getId() == ZclColorControlCluster.ATTR_CURRENTHUE ||
                        zclAttribute.getId() == ZclColorControlCluster.ATTR_CURRENTSATURATION)) {

            ZclCluster cluster = endpoint.getInputCluster(zclAttribute.getClusterType().getId());
            float hue = (Integer) cluster.getAttribute(ZclColorControlCluster.ATTR_CURRENTHUE).getLastValue();
            float saturation = (Integer) cluster.getAttribute(ZclColorControlCluster.ATTR_CURRENTSATURATION).getLastValue();
            return ColorEventData.createFromHLS(
                    hue, 0.5f, saturation,
                    zclAttribute.getLastReportTime().getTimeInMillis());
        }
        return null;
    }

    @Override
    public int getZigbeeClusterId() {
        return ZclColorControlCluster.CLUSTER_ID;
    }

    @Override
    protected ZclCommand getZigbeeCommandObject(HalEventData data) {
        if (! (data instanceof ColorEventData))
            return null;

        //float[] cie = ((ColorEventData) data).getCieXYZ();
        //int x = (int) (cie[0] * 65536);
        //int y = (int) (cie[1] * 65536);
        Cie cie = Cie.rgb2cie(((ColorEventData) data).getRed(), ((ColorEventData) data).getGreen(), ((ColorEventData) data).getBlue());
        int x = (int) (cie.x * 65536);
        int y = (int) (cie.y * 65536);

        if (x > 65279)
            x = 65279;
        if (y > 65279)
            y = 65279;

        return new MoveToColorCommand(x, y, 10);

        //double hue = ((ColorEventData) data).getHue() * 254.0f / 360.0f + 0.5f;
        //double saturation = ((ColorEventData) data).getSaturation() * 256;
        //return new MoveToHueAndSaturationCommand((int) hue, (int) saturation, 1);
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return ColorEventData.class;
    }
}
