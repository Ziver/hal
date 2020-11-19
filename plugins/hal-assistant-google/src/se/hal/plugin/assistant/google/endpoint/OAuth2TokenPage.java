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
 *
 * RFC 6749: Chapter 4.1
 */
public class OAuth2TokenPage extends HttpJsonPage {
    private static final int SECONDS_IN_DAY = 86400;
    public static final String ENDPOINT_URL = "api/assistant/google/auth/token";

    /** The request is missing a required parameter, includes an unsupported parameter value (other than grant type),
     repeats a parameter, includes multiple credentials, utilizes more than one mechanism for authenticating the
     client, or is otherwise malformed. **/
    protected static final String ERROR_INVALID_REQUEST = "invalid_request";
    /** Client authentication failed (e.g., unknown client, no client authentication included, or unsupported
     authentication method).  **/
    protected static final String ERROR_INVALID_CLIENT = "invalid_client";
    /** The provided authorization grant (e.g., authorization code, resource owner credentials) or refresh token is
     invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to
     another client. **/
    protected static final String ERROR_INVALID_GRANT = "invalid_grant";
    /** The authenticated client is not authorized to use this authorization grant type. **/
    protected static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
    /** The authorization grant type is not supported by the authorization server. **/
    protected static final String ERROR_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    /** The requested scope is invalid, unknown, malformed, or exceeds the scope granted by the resource owner. **/
    protected static final String ERROR_INVALID_SCOPE = "invalid_scope";

    protected final String ACCESS_TOKEN = "SUPER-SECURE-TOKEN";
    protected final String REFRESH_ACCESS_TOKEN = "SUPER-SECURE-REFRESH-TOKEN";


    private String clientId;


    public OAuth2TokenPage(SmartHomeImpl smartHome) {}
    public OAuth2TokenPage(SmartHomeImpl smartHome, String clientId) {
        this.clientId = clientId;
    }


    @Override
    public DataNode jsonRespond(
            HttpPrintStream out,
            HttpHeader headers,
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) {

        // POST

        out.setHeader("Access-Control-Allow-Origin", "*");
        out.setHeader("Cache-Control", "no-store");
        out.setHeader("Pragma", "no-cache");

        DataNode jsonRes = new DataNode(DataNode.DataType.Map);

        if (!request.containsKey("client_id"))
            return errorResponse(out, ERROR_INVALID_REQUEST , request.get("state"), "Missing mandatory parameter client_id.");

        if (clientId != null && clientId.equals(request.containsKey("client_id")))
            return errorResponse(out, ERROR_INVALID_CLIENT , request.get("state"), "Invalid client_id provided.");

        if (!OAuth2AuthPage.SECRET_CODE.equals(request.get("code")))
            return errorResponse(out, ERROR_INVALID_GRANT, request.get("state"), "Invalid code value provided.");

        String grantType = request.get("grant_type");

        switch (grantType) {
            case "authorization_code":
                jsonRes.set("refresh_token", REFRESH_ACCESS_TOKEN);
                break;
            default:
                return errorResponse(out, ERROR_UNSUPPORTED_GRANT_TYPE, request.get("state"), "Unsupported grant_type: " + request.containsKey("grant_type"));
        }

        jsonRes.set("access_token", ACCESS_TOKEN);
        jsonRes.set("token_type", "bearer");
        jsonRes.set("expires_in", SECONDS_IN_DAY);
        //jsonRes.set("scope", SECONDS_IN_DAY);
        if (request.containsKey("state")) jsonRes.set("state", request.get("state"));

        return jsonRes;
    }

    private static DataNode errorResponse(HttpPrintStream out, String error, String state, String description) {
        out.setResponseStatusCode(400);

        DataNode jsonErr = new DataNode(DataNode.DataType.Map);
        jsonErr.set("error", error);
        if (description != null) jsonErr.set("error_description", description);
        //if (uri != null)         jsonErr.set("error_uri", uri);
        if (state != null)       jsonErr.set("state", state);

        return jsonErr;
    }
}
