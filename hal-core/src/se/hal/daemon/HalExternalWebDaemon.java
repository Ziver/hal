package se.hal.daemon;

import org.shredzone.acme4j.exception.AcmeException;
import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalWebPage;
import se.hal.util.HalAcmeDataStore;
import se.hal.util.HalOAuth2RegistryStore;
import zutil.log.LogUtil;
import zutil.net.acme.AcmeClient;
import zutil.net.acme.AcmeHttpChallengeFactory;
import zutil.net.acme.AcmeManualDnsChallengeFactory;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpServer;
import zutil.net.http.page.oauth.OAuth2AuthorizationPage;
import zutil.net.http.page.oauth.OAuth2Registry;
import zutil.net.http.page.oauth.OAuth2TokenPage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.hal.HalContext.CONFIG_HTTP_EXTERNAL_DOMAIN;
import static se.hal.HalContext.CONFIG_HTTP_EXTERNAL_PORT;


public class HalExternalWebDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    public static final String ENDPOINT_AUTH  = "api/auth/authorize";
    public static final String ENDPOINT_TOKEN = "api/auth/token";


    // Certificate fields
    private HalAcmeDataStore acmeDataStore = new HalAcmeDataStore();
    private String externalServerUrl;
    private X509Certificate certificate;

    // Oauth fields
    private OAuth2Registry oAuth2Registry;

    // Web server fields
    private HttpServer httpExternal;
    private HashMap<String, HttpPage> pageMap = new HashMap<>();


    @Override
    public void initiate(ScheduledExecutorService executor) {
        // ------------------------------------
        // Initialize Oauth2
        // ------------------------------------

        oAuth2Registry = new OAuth2Registry(new HalOAuth2RegistryStore());
        registerPage(ENDPOINT_AUTH, new OAuth2AuthorizationPage(oAuth2Registry));
        registerPage(ENDPOINT_TOKEN, new OAuth2TokenPage(oAuth2Registry));

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
            logger.log(Level.SEVERE, "Was unable to initiate external web server.", e);
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

                acme = new AcmeClient(acmeDataStore, new AcmeHttpChallengeFactory(tmpHttpServer), AcmeClient.ACME_SERVER_LETSENCRYPT_STAGING);
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


    /**
     * @return the OAuth2Registry used by the general OAuth2 authorization pages.
     */
    public OAuth2Registry getOAuth2Registry() {
        return oAuth2Registry;
    }

    /**
     * Registers the given page with the external Hal web server.
     * Note: as this page will most likely be accessible through the internet it needs to be robust and secure.
     *
     * @param url  is the web path to the page.
     * @param page is the page to register with the server.
     */
    public void registerPage(String url, HttpPage page) {
        pageMap.put(url, page);

        if (httpExternal != null)
            httpExternal.setPage(url, page);
    }

    /**
     * Registers the given page with the external Hal web server.
     * Note: as this page will most likely be accessible through the internet it needs to be robust and secure.
     *
     * @param page is the page to register with the server.
     */
    public void registerPage(HalWebPage page){
        registerPage(page.getId(), page);
    }
}
