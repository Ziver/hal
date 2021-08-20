package se.hal.util;

import org.shredzone.acme4j.util.KeyPairUtils;
import se.hal.HalContext;
import zutil.log.LogUtil;
import zutil.net.acme.AcmeDataStore;
import zutil.parser.Base64Decoder;
import zutil.parser.Base64Encoder;

import java.io.*;
import java.security.KeyPair;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalAcmeDataStore implements AcmeDataStore {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_HTTP_EXTERNAL_USER_KEY   = "hal_core.http_external_user_key";
    private static final String CONFIG_HTTP_EXTERNAL_DOMAIN_KEY = "hal_core.http_external_domain_key";

    @Override
    public KeyPair loadUserKeyPair() {
         return loadKeyPair(CONFIG_HTTP_EXTERNAL_USER_KEY);
    }

    @Override
    public KeyPair loadDomainKeyPair() {
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
    public void storeUserKeyPair(KeyPair keyPair) {
        storeKeyPair(keyPair, CONFIG_HTTP_EXTERNAL_USER_KEY);
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
}
