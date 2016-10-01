package se.hal.plugin.netscan;

import se.hal.intf.*;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.log.LogUtil;
import zutil.net.InetScanner;
import zutil.net.InetScanner.InetScanListener;
import zutil.osal.MultiCommandExecutor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2016-09-30.
 */
public class NetScanController implements HalEventController, HalAutoScannableController, InetScanListener, Runnable{
    public static Logger logger = LogUtil.getLogger();
    private static final int NETWORK_SYNC_INTERVAL = 3 * 60 * 60 * 1000; // 3 hours
    private static final int PING_INTERVAL = 10 * 1000; // 10 sec

    private ScheduledExecutorService executor;
    private HalEventReportListener listener;
    private ArrayList<NetworkDevice> devices = new ArrayList<>();



    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void initialize() throws Exception {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.fine("Starting network scan...");
                    InetScanner scanner = new InetScanner();
                    scanner.setListener(NetScanController.this);
                    scanner.scan(InetAddress.getLocalHost());
                    logger.fine("Network scan done");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 30_000, NETWORK_SYNC_INTERVAL, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(NetScanController.this, 10_001, PING_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try{
            MultiCommandExecutor executor = new MultiCommandExecutor();
            for (int i = 0; i < devices.size(); i++) {
                NetworkDevice device = devices.get(i);
                if (listener != null) {
                    logger.fine("Pinging ip: "+device.getIp());
                    boolean online = InetScanner.isReachable(InetAddress.getByName(device.getIp()), executor);
                    listener.reportReceived(device, new SwitchEventData(online, System.currentTimeMillis()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void foundInetAddress(InetAddress ip) {
        logger.fine("Detected ip: "+ip.getHostAddress());
        if (listener != null)
            listener.reportReceived(
                    new NetworkDevice(ip.getHostAddress()),
                    new SwitchEventData(true, System.currentTimeMillis()));
    }



    @Override
    public void register(HalEventConfig event) {
        if (event instanceof NetworkDevice)
            devices.add((NetworkDevice) event);
    }
    @Override
    public void deregister(HalEventConfig event) {
        devices.remove(event);
    }
    @Override
    public int size() {
        return devices.size();
    }


    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) { }


    @Override
    public void setListener(HalEventReportListener listener) {
        this.listener = listener;
    }


    @Override
    public void close() {
        if (executor != null){
            executor.shutdown();
            executor = null;
        }
    }
}
