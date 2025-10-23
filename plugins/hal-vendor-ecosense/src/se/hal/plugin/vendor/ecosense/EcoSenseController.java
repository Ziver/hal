/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
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

package se.hal.plugin.vendor.ecosense;

import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.*;
import se.hal.plugin.vendor.ecosense.device.EccoCubeRadonSensorConfig;
import se.hal.struct.devicedata.RadonSensorData;
import zutil.ObjectUtil;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EcoSenseController implements HalSensorController, Runnable, HalDaemon, HalAutostartController {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_ECOSENSE_USERNAME = "hal_vendor_ecosense.username";
    private static final String CONFIG_ECOSENSE_PASSWORD = "hal_vendor_ecosense.password";
    private static final int POLL_TIME = 10*60; // poll every 10 min defined in sec

    private EcoSenseCloudAPIClient ecoSenseClient;
    private List<HalDeviceConfig> registeredDevices = new ArrayList<>();
    private HalDeviceReportListener deviceReportListener;
    private ScheduledFuture<?> threadSchedule;


    @Override
    public boolean isAvailable() {
        return  HalContext.containsProperty(CONFIG_ECOSENSE_USERNAME) &&
                HalContext.containsProperty(CONFIG_ECOSENSE_PASSWORD);
    }

    @Override
    public void initialize() throws Exception {
        ecoSenseClient = new EcoSenseCloudAPIClient(
                HalContext.getStringProperty(CONFIG_ECOSENSE_USERNAME),
                HalContext.getStringProperty(CONFIG_ECOSENSE_PASSWORD)
        );
        HalServer.registerDaemon(this);
    }

    @Override
    public void initiate(ScheduledExecutorService executor) {
        threadSchedule = executor.scheduleAtFixedRate(this, 10, POLL_TIME, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            if (!ObjectUtil.isEmpty(ecoSenseClient)) {
                List<EcoSenseCloudAPIClient.EcoSenseDevice> apiResponse = ecoSenseClient.getDevices();
                if (apiResponse.isEmpty()) {
                    logger.info("Received no devices from EcoSense API.");
                    return;
                }

                for (EcoSenseCloudAPIClient.EcoSenseDevice device : apiResponse) {
                    EccoCubeRadonSensorConfig radonSensor = new EccoCubeRadonSensorConfig();
                    radonSensor.setSerialNumber(device.serialNumber);

                    RadonSensorData radonSensorData = new RadonSensorData(device.radonLevel, device.lastRadonUpdateTime);

                    if (deviceReportListener != null) {
                        deviceReportListener.reportReceived(radonSensor, radonSensorData);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to poll EcoSense API.", e);
        }
    }

    @Override
    public void register(HalDeviceConfig deviceConfig) {
        registeredDevices.add(deviceConfig);
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        this.deviceReportListener = listener;
    }

    @Override
    public void close() {
        registeredDevices.clear();
        threadSchedule.cancel(false);
    }
}
