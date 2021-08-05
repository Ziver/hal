package se.hal.util;

import zutil.net.acme.AcmeDataStore;

import java.security.KeyPair;

public class HalAcmeDataStore implements AcmeDataStore {
    private static final String CONFIG_HTTP_EXTERNAL_USER_KEY   = "hal_core.http_external_user_key";
    private static final String CONFIG_HTTP_EXTERNAL_DOMAIN_KEY = "hal_core.http_external_domain_key";

    @Override
    public KeyPair loadUserKeyPair() {
        return null;
    }

    @Override
    public void storeUserKeyPair(KeyPair keyPair) {

    }

    @Override
    public KeyPair loadDomainKeyPair() {
        return null;
    }

    @Override
    public void storeDomainKeyPair(KeyPair keyPair) {

    }
}
