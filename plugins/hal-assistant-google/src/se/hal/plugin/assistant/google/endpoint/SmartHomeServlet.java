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

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.hal.plugin.assistant.google.endpoint;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.actions.api.smarthome.SmartHomeApp;
import com.google.auth.oauth2.GoogleCredentials;
import se.hal.intf.HalJsonPage;
import se.hal.plugin.assistant.google.MySmartHomeApp;
import zutil.io.IOUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPrintStream;
import zutil.parser.DataNode;

/**
 * Handles request received via HTTP POST and delegates it to your Actions app. See: [Request
 * handling in Google App
 * Engine](https://cloud.google.com/appengine/docs/standard/java/how-requests-are-handled).
 */
public class SmartHomeServlet extends HalJsonPage {
    private static final Logger logger = LogUtil.getLogger();
    private final SmartHomeApp actionsApp = new MySmartHomeApp();

    {
        try {
            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(getClass().getResourceAsStream("/smart-home-key.json"));
            actionsApp.setCredentials(credentials);
        } catch (Exception e) {
            logger.severe("couldn't load credentials");
        }
    }


    public SmartHomeServlet() {
        super("api/assistant/google/smarthome");
    }


    @Override
    protected DataNode jsonRespond(
            HttpPrintStream out,
            HttpHeader headers,
            Map<String,Object> session,
            Map<String,String> cookie,
            Map<String,String> request) throws Exception {

        String body = IOUtil.readContentAsString(headers.getInputStream());
        logger.info("doPost, body = " + body);

        try {
            String response = actionsApp.handleRequest(body, request).get();

            System.out.println("response = " + asJson);
            res.getWriter().write(asJson);
            res.getWriter().flush();
        } catch (ExecutionException | InterruptedException e) {
            logger.log(Level.SEVERE, "Failed to handle fulfillment request", e);
        }
    }
}