package se.hal.util;

import se.hal.HalContext;
import zutil.ObjectUtil;
import zutil.net.http.page.oauth.OAuth2RegistryStore;
import zutil.parser.json.JSONObjectInputStream;
import zutil.parser.json.JSONObjectOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HalOAuth2RegistryStore implements OAuth2RegistryStore {
    private static final String CONFIG_HTTP_EXTERNAL_OAUTH2_REGISTRY = "hal_core.http_external.oauth2_registry";

    private List<OAuth2ClientRegister> registerList = new ArrayList<>();


    @Override
    public synchronized List<OAuth2ClientRegister> getClientRegistries() {
        String json = HalContext.getStringProperty(CONFIG_HTTP_EXTERNAL_OAUTH2_REGISTRY);

        if (!ObjectUtil.isEmpty(json)) {
            registerList = JSONObjectInputStream.parse(json);
            return registerList;
        }
        return Collections.emptyList();
    }

    @Override
    public synchronized void storeClientRegister(OAuth2ClientRegister register) {
        registerList.remove(register);
        registerList.add(register);

        HalContext.setProperty(CONFIG_HTTP_EXTERNAL_OAUTH2_REGISTRY,
                JSONObjectOutputStream.toString(registerList));
    }
}
