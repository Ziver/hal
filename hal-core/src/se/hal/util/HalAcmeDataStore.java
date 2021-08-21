package se.hal.util;

import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.KeyPairUtils;
import se.hal.HalContext;
import zutil.io.StringInputStream;
import zutil.log.LogUtil;
import zutil.net.acme.AcmeDataStore;
import zutil.parser.Base64Decoder;
import zutil.parser.Base64Encoder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalAcmeDataStore implements AcmeDataStore {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_HTTP_EXTERNAL_ACCOUNT_LOCATION = "hal_core.http_external_account_location";
    private static final String CONFIG_HTTP_EXTERNAL_ACCOUNT_KEY      = "hal_core.http_external_account_key";
    private static final String CONFIG_HTTP_EXTERNAL_DOMAIN_KEY       = "hal_core.http_external_domain_key";
    private static final String CONFIG_HTTP_EXTERNAL_CERTIFICATE      = "hal_core.http_external_certificate";


    @Override
    public URL getAccountLocation() {
        try {
            if (HalContext.containsProperty(CONFIG_HTTP_EXTERNAL_ACCOUNT_LOCATION))
                return new URL(HalContext.getStringProperty(CONFIG_HTTP_EXTERNAL_ACCOUNT_LOCATION));
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Unable to create account URL.", e);
        }

        return null;
    }

    @Override
    public KeyPair getAccountKeyPair() {
         return loadKeyPair(CONFIG_HTTP_EXTERNAL_ACCOUNT_KEY);
    }

    @Override
    public KeyPair getDomainKeyPair() {
         return loadKeyPair(CONFIG_HTTP_EXTERNAL_DOMAIN_KEY);
    }

    private KeyPair loadKeyPair(String configName) {
        if (HalContext.containsProperty(configName)) {
            try {
                byte[] data = Base64Decoder.decodeToByte(
                        HalContext.getStringProperty(configName));
                return KeyPairUtils.readKeyPair(new InputStreamReader(new ByteArrayInputStream(data)));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Was unable to load KeyPair from DB.", e);
            }
        }
        return null;
    }


    @Override
    public void storeAccountKeyPair(URL accountLocation, KeyPair accountKeyPair) {
        HalContext.setProperty(CONFIG_HTTP_EXTERNAL_ACCOUNT_LOCATION, accountLocation.toString());
        storeKeyPair(accountKeyPair, CONFIG_HTTP_EXTERNAL_ACCOUNT_KEY);
    }

    @Override
    public void storeDomainKeyPair(KeyPair keyPair) {
        storeKeyPair(keyPair, CONFIG_HTTP_EXTERNAL_DOMAIN_KEY);
    }

    private void storeKeyPair(KeyPair keyPair, String configName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            KeyPairUtils.writeKeyPair(keyPair, writer);

            HalContext.setProperty(configName, Base64Encoder.encode(out.toByteArray()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Was unable to store KeyPair to DB.", e);
        }
    }


    @Override
    public X509Certificate getCertificate() {
        if (HalContext.containsProperty(CONFIG_HTTP_EXTERNAL_CERTIFICATE)) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(
                        new StringInputStream(HalContext.getStringProperty(CONFIG_HTTP_EXTERNAL_CERTIFICATE)));
                return certificate;
            } catch (CertificateException e) {
                logger.log(Level.SEVERE, "Was unable to read certificate from DB.", e);
            }
        }
        return null;
    }

    @Override
    public void storeCertificate(X509Certificate certificate) {
        try (StringWriter out = new StringWriter()) {
            AcmeUtils.writeToPem(certificate.getEncoded(), AcmeUtils.PemLabel.CERTIFICATE, out);
            HalContext.setProperty(CONFIG_HTTP_EXTERNAL_CERTIFICATE, out.toString());
        } catch (IOException | CertificateEncodingException e) {
            logger.log(Level.SEVERE, "Was unable to store certificate to DB.", e);
        }
    }
}
