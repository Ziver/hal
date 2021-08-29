package se.hal.daemon;

import org.shredzone.acme4j.exception.AcmeException;
import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.util.HalAcmeDataStore;
import zutil.log.LogUtil;
import zutil.net.acme.AcmeClient;
import zutil.net.acme.AcmeHttpChallengeFactory;
import zutil.net.acme.AcmeManualDnsChallengeFactory;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpServer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import static se.hal.HalContext.CONFIG_HTTP_EXTERNAL_DOMAIN;
import static se.hal.HalContext.CONFIG_HTTP_EXTERNAL_PORT;


public class HalExternalWebDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private HalAcmeDataStore acmeDataStore = new HalAcmeDataStore();
    private HttpServer httpExternal;
    private String externalServerUrl;
    private X509Certificate certificate;
    private HashMap<String, HttpPage> pageMap = new HashMap<>();


    @Override
    public void initiate(ScheduledExecutorService executor) {
        // ------------------------------------
        // Initialize External HttpServer
        // ------------------------------------

        try {
            if (HalContext.containsProperty(CONFIG_HTTP_EXTERNAL_DOMAIN) && HalContext.containsProperty(CONFIG_HTTP_EXTERNAL_PORT)) {
                externalServerUrl = "https://" + HalContext.getStringProperty(HalContext.CONFIG_HTTP_EXTERNAL_DOMAIN) + ":" + HalContext.getStringProperty(HalContext.CONFIG_HTTP_EXTERNAL_PORT);
                certificate = acmeDataStore.getCertificate();

                renewCertificate();
                startHttpServer();
            } else {
                logger.warning("Missing '" + CONFIG_HTTP_EXTERNAL_PORT + "' and '" + CONFIG_HTTP_EXTERNAL_DOMAIN + "' configuration, will not setup external http server.");
            }
        } catch (Exception e) {
            logger.severe("Was unable to initiate external web server.");
        }
    }

    private void renewCertificate() throws AcmeException, IOException {
        if (!AcmeClient.isCertificateValid(certificate)) {
            // Prepare ACME Client
            AcmeClient acme;
            HttpServer tmpHttpServer = null;

            if ("dns".equals(HalContext.getStringProperty(HalContext.CONFIG_HTTP_EXTERNAL_ACME_TYPE, ""))) {
                acme = new AcmeClient(acmeDataStore, new AcmeManualDnsChallengeFactory());
            } else if ("http".equals(HalContext.getStringProperty(HalContext.CONFIG_HTTP_EXTERNAL_ACME_TYPE, "http"))) {
                tmpHttpServer = new HttpServer(80);
                tmpHttpServer.start();

                acme = new AcmeClient(acmeDataStore, new AcmeHttpChallengeFactory(tmpHttpServer));
            } else {
                throw new IllegalArgumentException("Unknown config value for " + externalServerUrl);
            }

            // Request certificate and start the external webserver

            acme.addDomain(HalContext.getStringProperty(CONFIG_HTTP_EXTERNAL_DOMAIN));
            acme.prepareRequest();
            certificate = acme.requestCertificate();
            acmeDataStore.storeCertificate(certificate);

            // Cleanup
            if (tmpHttpServer != null) {
                tmpHttpServer.close();
            }
        }
    }

    private void startHttpServer() throws GeneralSecurityException, IOException {
        httpExternal = new HttpServer(HalContext.getIntegerProperty(CONFIG_HTTP_EXTERNAL_PORT), acmeDataStore.getDomainKeyPair().getPrivate(), certificate);

        for (String url : pageMap.keySet()) {
            httpExternal.setPage(url, pageMap.get(url));
        }
        httpExternal.start();

        logger.info("External https server up and running at: " + externalServerUrl);
    }


    public void setPage(String url, HttpPage page) {
        pageMap.put(url, page);
    }
}
