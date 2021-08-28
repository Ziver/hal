/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.zigbee;

import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalDaemon;
import se.hal.plugin.zigbee.device.ZigbeeHalDeviceConfig;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZigbeeAttributeUpdateDaemon implements HalDaemon, Runnable {
    private static final Logger logger = LogUtil.getLogger();


    @Override
    public void initiate(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        logger.info("Requesting zigbee attribute updates.");
        ZigbeeController controller = HalAbstractControllerManager.getController(ZigbeeController.class);

        for (ZigbeeHalDeviceConfig device : controller.getRegisteredDevices()) {
            try {
                ZclCluster cluster = device.getZigbeeCluster(controller);
                Collection<ZclAttribute> attributes = cluster.getAttributes();

                List<Integer> attributeIds = new ArrayList<>();
                for (ZclAttribute attr : attributes) {
                    attributeIds.add(attr.getId());
                }

                cluster.readAttributes(attributeIds);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Was unable to read attribute for device: " + device.getZigbeeNodeAddress(), e);
            }
        }
    }
}
