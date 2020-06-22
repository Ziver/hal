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
import se.hal.intf.HalAutoScannableController;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorReportListener;
import zutil.log.LogUtil;
import zutil.osal.linux.app.NutUPSClient;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutUpsController implements HalSensorController, HalAutoScannableController, Runnable{
    public static Logger logger = LogUtil.getLogger();
    private static final int SYNC_INTERVAL = 60 * 1000;

    private HashMap<String, NutUpsDevice> registeredDevices = new HashMap<>();
    private NutUPSClient client;
    private ScheduledExecutorService executor;
    private HalSensorReportListener listener;



    @Override
    public boolean isAvailable() {
        return HalContext.getStringProperty("nutups.host") != null;
    }
    @Override
    public void initialize() throws Exception {
        if (client == null) {
            int port = NutUPSClient.DEFAULT_PORT;
            if (HalContext.getStringProperty("nutups.port") != null)
                port = Integer.parseInt(HalContext.getStringProperty("nutups.port"));
            client = new NutUPSClient(HalContext.getStringProperty("nutups.host"), port);

            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(this, 5000, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public void setListener(HalSensorReportListener listener) {
        this.listener = listener;
    }


    @Override
    public void run() {
        try {
            if (client != null && listener != null) {
                for (NutUPSClient.UPSDevice ups : client.getUPSList()) {
                    NutUpsDevice device = registeredDevices.get(ups.getId());
                    if (device == null)
                        device = new NutUpsDevice(ups);
                    listener.reportReceived(device, device.read(ups));
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
    public void register(HalSensorConfig sensor) {
        registeredDevices.put(((NutUpsDevice) sensor).getUpsId(), (NutUpsDevice) sensor);
    }
    @Override
    public void deregister(HalSensorConfig sensor) {
        registeredDevices.remove(((NutUpsDevice) sensor).getUpsId());
    }
    @Override
    public int size() {
        return 0;
    }

}
