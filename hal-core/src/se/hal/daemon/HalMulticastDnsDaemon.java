package se.hal.daemon;

import se.hal.intf.HalDaemon;
import zutil.log.LogUtil;
import zutil.net.dns.MulticastDnsServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HalMulticastDnsDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private MulticastDnsServer server;

    @Override
    public void initiate(ScheduledExecutorService executor) {
        try {
            server = new MulticastDnsServer();
            server.addEntry("hal.local", InetAddress.getLocalHost());
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Was unable to start mDNS Server.", e);
        }
    }

}
