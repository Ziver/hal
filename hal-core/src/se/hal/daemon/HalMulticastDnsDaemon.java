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

import static se.hal.HalContext.CONFIG_DNS_LOCAL_DOMAIN;


public class HalMulticastDnsDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private MulticastDnsServer server;

    @Override
    public void initiate(ScheduledExecutorService executor) {
        String localDomain = HalContext.getStringProperty(HalContext.CONFIG_DNS_LOCAL_DOMAIN, "hal.local");

        if (!localDomain.isEmpty()) {
            try {
                logger.info("Initializing local mDNS server for domain: " + localDomain);

                server = new MulticastDnsServer();
                server.addEntry(localDomain, InetAddress.getLocalHost());
                server.start();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Was unable to start mDNS Server.", e);
            }
        } else {
            logger.info("Disabling local mDNS server.");
        }
    }

}
