package se.hal.plugin.nutups;

import se.hal.HalContext;
import se.hal.intf.HalAutoScannableController;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.intf.HalSensorReportListener;
import zutil.log.LogUtil;
import zutil.osal.app.linux.NutUPSClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2016-05-25.
 */
public class NutUpsController implements HalSensorController, HalAutoScannableController, Runnable{
    public static Logger logger = LogUtil.getLogger();
    private static final int SYNC_INTERVAL = 60 * 1000;

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
                    listener.reportReceived(new NutUpsDevice(ups));
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
    public void register(HalSensorData sensor) {

    }
    @Override
    public void deregister(HalSensorData sensor) {

    }
    @Override
    public int size() {
        return 0;
    }

}
