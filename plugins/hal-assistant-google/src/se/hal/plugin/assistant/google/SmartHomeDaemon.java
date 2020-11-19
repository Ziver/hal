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
import se.hal.intf.HalDaemon;
import se.hal.plugin.assistant.google.endpoint.OAuth2AuthPage;
import se.hal.plugin.assistant.google.endpoint.OAuth2TokenPage;
import se.hal.plugin.assistant.google.endpoint.SmartHomePage;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;


public class SmartHomeDaemon implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    private static final String PARAM_PORT = "assistant.google.port";
    private static final String PARAM_KEYSTORE_PATH = "assistant.google.keystore";
    private static final String PARAM_KEYSTORE_PASSWORD = "assistant.google.keystore_psw";
    private static final String PARAM_GOOGLE_CREDENTIALS = "assistant.google.credentials";

    private HttpServer httpServer;
    private SmartHomeImpl smartHome;

    @Override
    public void initiate(ScheduledExecutorService executor) {
        if (smartHome == null) {
            if (!HalContext.containsProperty(PARAM_PORT) ||
                    !HalContext.containsProperty(PARAM_KEYSTORE_PATH) ||
                    !HalContext.containsProperty(PARAM_KEYSTORE_PASSWORD) ||
                    !HalContext.containsProperty(PARAM_GOOGLE_CREDENTIALS)) {
                logger.severe("Missing configuration, abort initializations.");
            }

            smartHome = new SmartHomeImpl(
                    HalContext.RESOURCE_ROOT + "/" + HalContext.getStringProperty(PARAM_GOOGLE_CREDENTIALS)
            );

            httpServer = new HttpServer(HalContext.getIntegerProperty(PARAM_PORT));
            httpServer.setPage(OAuth2AuthPage.ENDPOINT_URL, new OAuth2AuthPage(smartHome,
                    "https://oauth-redirect.googleusercontent.com/r/optimal-comfort-93608"));
            httpServer.setPage(OAuth2TokenPage.ENDPOINT_URL, new OAuth2TokenPage(smartHome));
            httpServer.setPage(SmartHomePage.ENDPOINT_URL, new SmartHomePage(smartHome));
            httpServer.start();
        }
    }

    @Override
    public void run() { }
}
