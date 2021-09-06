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

package se.hal.plugin.nutups;

import se.hal.HalContext;
import se.hal.intf.HalAutostartController;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceReportListener;
import se.hal.intf.HalSensorController;
import zutil.log.LogUtil;
import zutil.osal.linux.app.NutUPSClient;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NutUpsController implements HalSensorController, HalAutostartController, Runnable {
    public static Logger logger = LogUtil.getLogger();

    private static final int SYNC_INTERVAL = 60 * 1000;
    public static final String CONFIG_HOST = "hal_nutups.host";
    public static final String CONFIG_PORT = "hal_nutups.port";

    private HashMap<String, NutUpsDevice> registeredDevices = new HashMap<>();
    private NutUPSClient client;
    private ScheduledExecutorService executor;
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();



    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_HOST);
    }
    @Override
    public void initialize() throws Exception {
        if (client == null) {
            int port = NutUPSClient.DEFAULT_PORT;
            if (HalContext.containsProperty(CONFIG_PORT))
                port = HalContext.getIntegerProperty(CONFIG_PORT);

            client = new NutUPSClient(HalContext.getStringProperty(CONFIG_HOST), port);

            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(this, 5000, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }


    @Override
    public void run() {
        try {
            if (client != null) {
                for (HalDeviceReportListener deviceListener : deviceListeners) {
                    for (NutUPSClient.UPSDevice ups : client.getUPSList()) {
                        NutUpsDevice device = registeredDevices.get(ups.getId());
                        if (device == null)
                            device = new NutUpsDevice(ups);

                        deviceListener.reportReceived(device, device.read(ups));
                    }
                }
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, "NutUps thread crashed", e);
        }
    }

    @Override
    public void close() {
        client = null;
        executor.shutdownNow();
    }


    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof NutUpsDevice)
            registeredDevices.put(((NutUpsDevice) deviceConfig).getUpsId(), (NutUpsDevice) deviceConfig);
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(((NutUpsDevice) deviceConfig).getUpsId());
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

}
