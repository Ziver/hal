package se.hal.daemon;

import org.shredzone.acme4j.exception.AcmeException;
import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalWebPage;
import se.hal.page.HalAlertManager;
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
import zutil.ui.UserMessageManager;

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
                logger.warning("Missing '" + CONFIG_HTTP_EXTERNAL_PORT + "' and '" + CONFIG_HTTP_EXTERNAL_DOMAIN + "' configuration, will not setup external web-server.");
                HalAlertManager.getInstance().addAlert(new UserMessageManager.UserMessage(
                        UserMessageManager.MessageLevel.WARNING, "Missing '" + CONFIG_HTTP_EXTERNAL_PORT + "' and '" + CONFIG_HTTP_EXTERNAL_DOMAIN + "' configuration, will not setup external web-server.", UserMessageManager.MessageTTL.DISMISSED));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Was unable to initiate external web-server.", e);
            HalAlertManager.getInstance().addAlert(new UserMessageManager.UserMessage(
                    UserMessageManager.MessageLevel.ERROR, "Was unable to initiate external web-server.", UserMessageManager.MessageTTL.DISMISSED));
        }
    }

    private void renewCertificate() throws AcmeException, IOException {
        if (!AcmeClient.isCertificateValid(certificate)) {
            // Prepare ACME Client
            AcmeClient acme;
            HttpServer tmpHttpServer = null;
            String acmeType = HalContext.getStringProperty(HalContext.CONFIG_HTTP_EXTERNAL_CERT, "acme_http");

            try {
                if ("acme_http".equals(acmeType)) {
                    tmpHttpServer = new HttpServer(80);
                    tmpHttpServer.start();

                    acme = new AcmeClient(acmeDataStore, new AcmeHttpChallengeFactory(tmpHttpServer), AcmeClient.ACME_SERVER_LETSENCRYPT_STAGING);
                } else if ("none".equals(acmeType)) {
                    acme = null;
                } else if ("acme_dns".equals(acmeType)) {
                    acme = new AcmeClient(acmeDataStore, new AcmeManualDnsChallengeFactory());
                } else {
                    throw new IllegalArgumentException("Unknown config value for " + externalServerUrl);
                }

                // Request certificate and start the external webserver

                if (acme != null) {
                    acme.addDomain(HalContext.getStringProperty(CONFIG_HTTP_EXTERNAL_DOMAIN));
                    acme.prepareRequest();
                    certificate = acme.requestCertificate();
                    acmeDataStore.storeCertificate(certificate);

                    logger.info("SSL certificate successfully generated.");
                    HalAlertManager.getInstance().addAlert(new UserMessageManager.UserMessage(
                            UserMessageManager.MessageLevel.INFO, "SSL certificate successfully generated for external web-server.", UserMessageManager.MessageTTL.DISMISSED));
                } else {
                    logger.warning("No SSL certificate is configured for external HTTP Server.");
                    HalAlertManager.getInstance().addAlert(new UserMessageManager.UserMessage(
                            UserMessageManager.MessageLevel.WARNING, "No SSL certificate is configured for external web-server.", UserMessageManager.MessageTTL.DISMISSED));
                    certificate = null;
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to request cert from ACME service.", e);
                HalAlertManager.getInstance().addAlert(new UserMessageManager.UserMessage(
                        UserMessageManager.MessageLevel.WARNING, "Was unable to generate SSL certificate for external web-server: " + e.getMessage(), UserMessageManager.MessageTTL.DISMISSED));
            }

            // Cleanup
            if (tmpHttpServer != null) {
                tmpHttpServer.close();
            }
        }
    }

    private void startHttpServer() throws GeneralSecurityException, IOException {
        // Shutdown old server
        if (httpExternal != null)
            httpExternal.close();

        // Start new Server

        if (certificate != null)
            httpExternal = new HttpServer(HalContext.getIntegerProperty(CONFIG_HTTP_EXTERNAL_PORT), acmeDataStore.getDomainKeyPair().getPrivate(), certificate);
        else
            httpExternal = new HttpServer(HalContext.getIntegerProperty(CONFIG_HTTP_EXTERNAL_PORT));

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
