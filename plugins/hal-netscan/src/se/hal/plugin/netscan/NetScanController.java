package se.hal.plugin.netscan;

import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.struct.devicedata.AvailabilityEventData;
import zutil.InetUtil;
import zutil.log.LogUtil;
import zutil.net.InetScanner;
import zutil.net.InetScanner.InetScanListener;
import zutil.osal.MultiCommandExecutor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetScanController implements HalEventController, HalAutostartController, InetScanListener, Runnable {
    public static Logger logger = LogUtil.getLogger();
    private static final int NETWORK_SYNC_INTERVAL = 3 * 60 * 60 * 1000; // 3 hours
    private static final int PING_INTERVAL = 10 * 1000; // 10 sec
    private static final String PARAM_IPSCAN = "netscan.ipscan";

    private ScheduledExecutorService executor;
    private HalDeviceReportListener listener;
    /** A register and a cache of previous state **/
    private HashMap<NetworkDevice, AvailabilityEventData> devices = new HashMap<>();



    @Override
    public boolean isAvailable() {
        return ! InetUtil.getLocalInet4Address().isEmpty();
    }

    @Override
    public void initialize() throws Exception {
        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(NetScanController.this, 10_000, PING_INTERVAL, TimeUnit.MILLISECONDS);
        if (HalContext.getBooleanProperty(PARAM_IPSCAN, true)) {
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.fine("Starting network scan...");
                        InetScanner scanner = new InetScanner();
                        scanner.setListener(NetScanController.this);
                        scanner.scan(InetUtil.getLocalInet4Address().get(0));
                        logger.fine("Network scan done");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, null, e);
                    }
                }
            }, 30_000, NETWORK_SYNC_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void run() {
        try(MultiCommandExecutor executor = new MultiCommandExecutor()){
            for (Map.Entry<NetworkDevice, AvailabilityEventData> entry : devices.entrySet()) {
                NetworkDevice device = entry.getKey();
                AvailabilityEventData prevData = entry.getValue();
                if (listener != null) {
                    // We ping two times to increase reliability
                    boolean ping = false;
                    ping |= InetScanner.isReachable(device.getHost(), executor);
                    if (!ping)
                        ping |= InetScanner.isReachable(device.getHost(), executor);

                    // Should we report?
                    if (prevData == null || prevData.isAvailable() != ping) {
                        AvailabilityEventData newData = new AvailabilityEventData(ping, System.currentTimeMillis());
                        entry.setValue(newData);
                        logger.fine("IP " + device.getHost() + " state has changed to " + newData);
                        listener.reportReceived(device, newData);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    @Override
    public void foundInetAddress(InetAddress ip) {
        logger.fine("Auto Detected ip: "+ip.getHostAddress());
        if (listener != null)
            listener.reportReceived(
                    new NetworkDevice(ip.getHostAddress()),
                    new AvailabilityEventData(true, System.currentTimeMillis()));
    }



    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof NetworkDevice)
            devices.put((NetworkDevice) deviceConfig, null);
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        devices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return devices.size();
    }


    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) { }


    @Override
    public void setListener(HalDeviceReportListener listener) {
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
