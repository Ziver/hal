/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.assistant.google;

import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.HalDaemon;
import zutil.log.LogUtil;
import zutil.net.http.page.oauth.OAuth2AuthorizationPage;
import zutil.net.http.page.oauth.OAuth2Registry;
import zutil.net.http.page.oauth.OAuth2TokenPage;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;


public class SmartHomeDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private   static final String ENDPOINT_SMARTHOME = "api/assistant/google/smarthome";
    protected static final String CONFIG_CLIENT_ID   = "hal_assistant.google.client_id";

    private SmartHomeImpl smartHome;


    @Override
    public void initiate(ScheduledExecutorService executor) {
        if (smartHome == null) {
            if (!HalContext.containsProperty(CONFIG_CLIENT_ID)) {
                logger.severe("Missing configuration, aborting initializations.");
                return;
            }

            smartHome = new SmartHomeImpl();

            HalServer.getExternalWebDaemon().getOAuth2Registry().addWhitelist(HalContext.getStringProperty(CONFIG_CLIENT_ID));
            HalServer.getExternalWebDaemon().getOAuth2Registry().addTokenListener(smartHome);
            HalServer.getExternalWebDaemon().registerPage(ENDPOINT_SMARTHOME, new SmartHomePage(smartHome));
        }
    }
}
