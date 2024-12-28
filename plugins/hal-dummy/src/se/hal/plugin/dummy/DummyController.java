/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Ziver Koc
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

package se.hal.plugin.dummy;

import se.hal.HalServer;
import se.hal.intf.*;
import se.hal.util.ListenerUtil;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DummyController implements HalSensorController, HalEventController, Runnable, HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private List<DummyDevice> registeredDevices = new ArrayList<>();
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();


    public DummyController() {}


    @Override
    public void initialize() {
        HalServer.registerDaemon(this);
    }

    @Override
    public void initiate(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void run() {
        try {
            for (DummyDevice device : registeredDevices) {
                HalDeviceData data = device.generateData();

                ListenerUtil.callReportReceived(deviceListeners, device, data);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Dummy device data generation has crashed.", e);
        }
    }

    @Override
    public synchronized void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof DummyDevice)
            registeredDevices.add((DummyDevice) deviceConfig);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public synchronized void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public synchronized void send(HalEventConfig eventConfig, HalEventData eventData) {
        // Nothing to do as this is a dummy controller
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
        registeredDevices = new ArrayList<>();
    }
}
