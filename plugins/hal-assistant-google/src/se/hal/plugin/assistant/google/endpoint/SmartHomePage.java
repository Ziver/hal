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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.actions.api.smarthome.SmartHomeApp;
import se.hal.plugin.assistant.google.SmartHomeImpl;
import zutil.io.IOUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;

/**
 * Handles request received via HTTP POST and delegates it to your Actions app. See: [Request
 * handling in Google App
 * Engine](https://cloud.google.com/appengine/docs/standard/java/how-requests-are-handled).
 */
public class SmartHomePage implements HttpPage {
    private static final Logger logger = LogUtil.getLogger();

    private SmartHomeImpl smartHome;


    public SmartHomePage(SmartHomeImpl smartHome) {
        this.smartHome = smartHome;
    }


    @Override
    public void respond(
            HttpPrintStream out,
            HttpHeader headers,
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws IOException {

        String body = IOUtil.readContentAsString(headers.getInputStream());
        logger.fine("doPost, body = " + body);

        try {
            String response = smartHome.handleRequest(body, request).get();

            out.setHeader("Content-Type", "application/json");
            out.setHeader("Access-Control-Allow-Origin", "*");
            out.setHeader("Pragma", "no-cache");
            out.println(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Was unable to handle SmartHome request.", e);
        }
    }
}