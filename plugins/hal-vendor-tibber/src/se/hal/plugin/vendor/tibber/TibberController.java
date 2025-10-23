/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024-2025 Ziver Koc
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

package se.hal.plugin.vendor.tibber;

import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.*;
import se.hal.plugin.vendor.tibber.device.TibberElectricityCostSensor;
import se.hal.plugin.vendor.tibber.device.TibberElectricityPriceSensor;
import se.hal.plugin.vendor.tibber.device.TibberPowerConsumptionSensor;
import se.hal.struct.devicedata.PriceSensorData;
import se.hal.util.ListenerUtil;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TibberController implements HalSensorController, Runnable, HalDaemon, HalAutostartController {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_TIBBER_TOKEN = "hal_tibber.token";
    private static final int POLL_TIME = 60*60; // poll every 1h defined in sec

    private TibberAPIClient tibberClient;
    private List<TibberDevice> registeredDevices = new ArrayList<>();
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private ScheduledFuture<?> threadSchedule;


    public TibberController() {}

    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_TIBBER_TOKEN);
    }

    @Override
    public void initialize() {
        tibberClient = new TibberAPIClient(HalContext.getStringProperty(CONFIG_TIBBER_TOKEN));
        HalServer.registerDaemon(this);
    }

    @Override
    public void initiate(ScheduledExecutorService executor) {
        threadSchedule = executor.scheduleAtFixedRate(this, 10, POLL_TIME, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void run() {
        if (!isAvailable()) {
            logger.warning("Controller not available yet.");
            return;
        }

        try {
            logger.fine("Querying Tibber API for new data.");

            TibberAPIClient.TibberConsumption consumption = tibberClient.getConsumption();

            if (consumption == null) {
                logger.severe("There was a error fetching data from Tibber API");
                return;
            }

            if (consumption.cost > 0) {
                ListenerUtil.callReportReceived(deviceListeners, new TibberElectricityCostSensor(), new PriceSensorData(consumption.cost, consumption.timestamp));
            }
            if (consumption.unitPriceVAT > 0) {
                ListenerUtil.callReportReceived(deviceListeners, new TibberElectricityPriceSensor(), new PriceSensorData(consumption.unitPriceVAT, consumption.timestamp));
            }
            if (consumption.consumption > 0) {
                ListenerUtil.callReportReceived(deviceListeners, new TibberPowerConsumptionSensor(), new PriceSensorData(consumption.consumption, consumption.timestamp));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Tibber API querying failed.", e);
        }
    }

    @Override
    public synchronized void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof TibberDevice)
            registeredDevices.add((TibberDevice) deviceConfig);
    }

    @Override
    public synchronized void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }

    @Override
    public synchronized void close() {
        registeredDevices.clear();
        threadSchedule.cancel(false);
    }
}
