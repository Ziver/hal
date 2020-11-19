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

import se.hal.plugin.assistant.google.SmartHomeImpl;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.net.http.HttpURL;

import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This endpoint is the first step in the OAuth 2 procedure.
 * The purpose of this page is get authorization from the user to share a resource.
 *
 * RFC 6749: Chapter 4.1.2:
 * https://tools.ietf.org/html/rfc6749
 */
public class OAuth2AuthPage implements HttpPage {
    public static final String ENDPOINT_URL = "api/assistant/google/auth/authorize";

    /** The request is missing a required parameter, includes an invalid parameter value, includes a parameter
     more than once, or is otherwise malformed. **/
    protected static final String ERROR_INVALID_REQUEST = "invalid_request";
    /** The client is not authorized to request an authorization code using this method. **/
    protected static final String ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
    /** The resource owner or authorization server denied the request. **/
    protected static final String ERROR_ACCESS_DENIED = "access_denied";
    /** The authorization server does not support obtaining an authorization code using this method. **/
    protected static final String ERROR_UNSUPPORTED_RESP_TYPE = "unsupported_response_type";
    /** The requested scope is invalid, unknown, or malformed. **/
    protected static final String ERROR_INVALID_SCOPE = "invalid_scope";
    /** The authorization server encountered an unexpected condition that prevented it from fulfilling the request.
     (This error code is needed because a 500 Internal Server Error HTTP status code cannot be returned to the client
     via an HTTP redirect.) **/
    protected static final String ERROR_SERVER_ERROR = "server_error";
    /** The authorization server is currently unable to handle the request due to a temporary overloading or maintenance
     of the server.  (This error code is needed because a 503 Service Unavailable HTTP status code cannot be returned
     to the client via an HTTP redirect.) **/
    protected static final String ERROR_TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";

    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String RESPONSE_TYPE_PASSWORD = "password";
    private static final String RESPONSE_TYPE_CREDENTIALS = "client_credentials";

    protected static final String SECRET_CODE = "SUPER-SECURE-CODE";


    private String clientId;


    public OAuth2AuthPage(SmartHomeImpl smartHome) {}
    public OAuth2AuthPage(SmartHomeImpl smartHome, String clientId) {
        this.clientId = clientId;
    }


    @Override
    public void respond(
            HttpPrintStream out,
            HttpHeader headers,
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws MalformedURLException {

        if (!request.containsKey("redirect_uri")) {
            errorResponse(out, "Bad Request, missing property: redirect_uri");
            return;
        }

        if (!request.containsKey("client_id") || clientId != null && clientId.equals(request.containsKey("client_id"))) {
            errorResponse(out, "Bad Request, missing or invalid client_id property.");
            return;
        }

        HttpURL url = new HttpURL(URLDecoder.decode(request.get("redirect_uri"), StandardCharsets.UTF_8));

        if (!"HTTPS".equalsIgnoreCase(url.getProtocol())) {
            errorResponse(out, "Bad redirect protocol: " + url.getProtocol());
            return;
        }

        switch (request.get("response_type")) {
            case RESPONSE_TYPE_CODE:
                url.setParameter("state", request.get("state"));
                url.setParameter("code", SECRET_CODE);
                break;
            case RESPONSE_TYPE_PASSWORD:
            case RESPONSE_TYPE_CREDENTIALS:
            default:
                errorRedirect(out, url, ERROR_INVALID_REQUEST, request.get("state"),
                        "unsupported response_type: " + request.get("response_type"));
                return;
        }

        // Setup the redirect

        redirect(out, url);
    }


    private static void errorResponse(HttpPrintStream out, String description) {
        out.setResponseStatusCode(400);
        out.println(description);
    }

    private static void errorRedirect(HttpPrintStream out, HttpURL url, String error, String state, String description) {
        out.setHeader(HttpHeader.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded");
        url.setParameter("error", error);
        if (description != null) url.setParameter("error_description", description);
        //if (uri != null)         url.setParameter("error_uri", uri);
        if (state != null)       url.setParameter("state", state);

        redirect(out, url);
    }

    private static void redirect(HttpPrintStream out, HttpURL url) {
        out.setResponseStatusCode(302);
        out.setHeader(HttpHeader.HEADER_LOCATION, url.toString());
    }
}
