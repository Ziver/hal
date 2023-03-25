package se.hal.daemon;

import se.hal.HalContext;
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
    private static HalMulticastDnsDaemon instance;

    private MulticastDnsServer server;

    @Override
    public synchronized void initiate(ScheduledExecutorService executor) {
        if (instance != null)
            return;

        String localDomain = HalContext.getStringProperty(HalContext.CONFIG_DNS_LOCAL_DOMAIN, "hal.local");

        if (!localDomain.isEmpty()) {
            try {
                logger.info("Initializing local mDNS server for domain: " + localDomain);

                server = new MulticastDnsServer();
                server.addEntry(localDomain, InetAddress.getLocalHost());
                server.start();

                instance = this;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Was unable to start mDNS Server.", e);
            }
        } else {
            logger.info("Disabling local mDNS server.");
        }
    }

    public void addDnsEntry(String name, InetAddress ip) {
        server.addEntry(name, ip);
    }


    public static HalMulticastDnsDaemon getInstance() {
        return instance;
    }
}
