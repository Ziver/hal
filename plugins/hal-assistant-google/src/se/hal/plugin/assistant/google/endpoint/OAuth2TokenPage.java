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

import java.util.Map;

import se.hal.plugin.assistant.google.SmartHomeImpl;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPrintStream;
import zutil.net.http.page.HttpJsonPage;
import zutil.parser.DataNode;

/**
 * This endpoint is the second step in the OAuth 2 procedure.
 * The purpose of this page is give a token that should be used for all consequent HTTP.
 */
public class OAuth2TokenPage extends HttpJsonPage {
    private static final int SECONDS_IN_DAY = 86400;
    public static final String ENDPOINT_URL = "api/assistant/google/auth/token";

    protected final String ACCESS_TOKEN = "SUPER-SECURE-TOKEN";


    public OAuth2TokenPage(SmartHomeImpl smartHome) {}


    @Override
    public DataNode jsonRespond(
            HttpPrintStream out,
            HttpHeader headers,
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) {

        // POST

        out.setHeader("Access-Control-Allow-Origin", "*");
        out.setHeader("Pragma", "no-cache");

        DataNode jsonRes = new DataNode(DataNode.DataType.Map);

        if (OAuth2AuthPage.SECRET_CODE.equals(request.get("code"))) {
            jsonRes.set("refresh_token", "123refresh");
        } else {
            out.setResponseStatusCode(400);
            DataNode jsonErr = new DataNode(DataNode.DataType.Map);
            jsonRes.set("error", "Invalid code value provided.");
            return jsonErr;
        }

        if ("authorization_code".equals(request.get("grant_type"))) {
            jsonRes.set("refresh_token", "123refresh");
        } else {
            out.setResponseStatusCode(400);
            DataNode jsonErr = new DataNode(DataNode.DataType.Map);
            jsonRes.set("error", "Unsupported grant_type: " + request.containsKey("grant_type"));
            return jsonErr;
        }

        jsonRes.set("access_token", ACCESS_TOKEN);
        jsonRes.set("token_type", "bearer");
        jsonRes.set("expires_in", SECONDS_IN_DAY);

        return jsonRes;
    }
}
