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

import java.io.IOException;
import java.io.InputStream;
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
 * Handles Google SMartHome request received via HTTP POST.
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

        int contentLength = 0;
        if (headers.containsHeader(HttpHeader.HEADER_CONTENT_LENGTH))
            contentLength = Integer.parseInt(headers.getHeader(HttpHeader.HEADER_CONTENT_LENGTH));

        String body = IOUtil.readContentAsString(headers.getInputStream(), contentLength);
        logger.finest("Request body = " + body);

        try {
            String response = smartHome.handleRequest(body, request).get();
            logger.finest("Response body = " + response);

            out.setHeader("Content-Type", "application/json");
            out.setHeader("Access-Control-Allow-Origin", "*");
            out.setHeader("Pragma", "no-cache");
            out.println(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Was unable to handle SmartHome request.", e);
        }
    }
}