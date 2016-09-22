/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.msf4j.security.oauth2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.security.MSF4JSecurityException;
import org.wso2.msf4j.security.SecurityErrorCode;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.HttpMethod;

/**
 * Act as a security gateway for resources secured with Oauth2.
 * <p>
 * Verify Oauth2 access token in Authorization Bearer HTTP header and allow access to the resource accordingly.
 *
 * @since 1.0.0
 */
public class OAuth2SecurityInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(OAuth2SecurityInterceptor.class);

    private static final String AUTHORIZATION_HTTP_HEADER = "Authorization";
    private static final String AUTH_TYPE_OAUTH2 = "OAuth2";
    private static final String BEARER_PREFIX = "bearer";
    private static final String AUTH_SERVER_URL_KEY = "AUTH_SERVER_URL";
    private static final String AUTH_SERVER_URL;
    private static final String TRUST_STORE = "TRUST_STORE";
    private static final String TRUST_STORE_PASSWORD = "TRUST_STORE_PASSWORD";

    static {
        AUTH_SERVER_URL = SystemVariableUtil.getValue(AUTH_SERVER_URL_KEY, null);
        if (AUTH_SERVER_URL == null) {
            throw new RuntimeException(AUTH_SERVER_URL_KEY + " is not specified.");
        }
        String trustStore = SystemVariableUtil.getValue(TRUST_STORE, null);
        String trustStorePassword = SystemVariableUtil.getValue(TRUST_STORE_PASSWORD, null);
        if (trustStore != null && !trustStore.isEmpty() &&
                trustStorePassword != null && !trustStorePassword.isEmpty()) {
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
    }

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws Exception {
        SecurityErrorCode errorCode;

        try {
            Headers headers = request.getHeaders();
            if (headers != null && headers.contains(AUTHORIZATION_HTTP_HEADER)) {
                String authHeader = headers.get(AUTHORIZATION_HTTP_HEADER);
                return validateToken(authHeader);
            } else {
                throw new MSF4JSecurityException(SecurityErrorCode.AUTHENTICATION_FAILURE,
                        "Missing Authorization header is the request.`");
            }
        } catch (MSF4JSecurityException e) {
            errorCode = e.getErrorCode();
            log.error(e.getMessage() + " Requested Path: " + request.getUri());
        }

        handleSecurityError(errorCode, responder);
        return false;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {

    }

    /**
     * Extract the accessToken from the give Authorization header value and validates the accessToken
     * with an external key manager.
     *
     * @param authHeader Authorization Bearer header which contains the access token
     * @return true if the token is a valid token
     */
    private boolean validateToken(String authHeader) throws MSF4JSecurityException {
        // 1. Check whether this token is bearer token, if not return false
        String accessToken = extractAccessToken(authHeader);

        // 2. Send a request to key server's introspect endpoint to validate this token
        String responseStr = getValidatedTokenResponse(accessToken);
        Map<String, String> responseData = getResponseDataMap(responseStr);

        //TODO handle NPE

        // 3. Process the response and return true if the token is valid.
        if (!Boolean.parseBoolean(responseData.get(IntrospectionResponse.ACTIVE))) {
            throw new MSF4JSecurityException(SecurityErrorCode.AUTHENTICATION_FAILURE,
                    "Invalid Access token.");
        }

        return true;
    }

    /**
     * @param authHeader Authorization Bearer header which contains the access token
     * @return access token
     */
    private String extractAccessToken(String authHeader) throws MSF4JSecurityException {
        authHeader = authHeader.trim();
        if (authHeader.toLowerCase(Locale.US).startsWith(BEARER_PREFIX)) {
            // Split the auth header to get the access token.
            // Value should be in this format ("Bearer" 1*SP b64token)
            String[] authHeaderParts = authHeader.split(" ");
            if (authHeaderParts.length == 2) {
                return authHeaderParts[1];
            }
        }

        throw new MSF4JSecurityException(SecurityErrorCode.INVALID_AUTHORIZATION_HEADER,
                "Invalid Authorization header: " + authHeader);
    }

    /**
     * Validated the given accessToken with an external key server.
     *
     * @param accessToken AccessToken to be validated.
     * @return the response from the key manager server.
     */
    private String getValidatedTokenResponse(String accessToken) throws MSF4JSecurityException {
        URL url;
        try {
            url = new URL(AUTH_SERVER_URL);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(HttpMethod.POST);
            urlConn.getOutputStream()
                   .write(("token=" + accessToken + "&token_type_hint=" + BEARER_PREFIX).getBytes(Charsets.UTF_8));
            return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
        } catch (java.io.IOException e) {
            log.error("Error invoking Authorization Server", e);
            throw new MSF4JSecurityException(SecurityErrorCode.GENERIC_ERROR, "Error invoking Authorization Server", e);
        }
    }

    /**
     * @param responseStr validated token response string returned from the key server.
     * @return a Map of key, value pairs available the response String.
     */
    private Map<String, String> getResponseDataMap(String responseStr) {
        Gson gson = new Gson();
        Type typeOfMapOfStrings = new ExtendedTypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(responseStr, typeOfMapOfStrings);
    }

    /**
     * This class extends the {@link TypeToken}.
     * Created due to the findbug issue when passing anonymous inner class.
     *
     * @param <T> Generic type
     */
    private static class ExtendedTypeToken<T> extends TypeToken {
    }

    /**
     * @param errorCode Security error code
     * @param responder HttpResponder instance which is used send error messages back to the client
     */
    private void handleSecurityError(SecurityErrorCode errorCode, Response responder)
            throws Exception {
        if (errorCode == SecurityErrorCode.AUTHENTICATION_FAILURE ||
                errorCode == SecurityErrorCode.INVALID_AUTHORIZATION_HEADER) {
            responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
            responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE_OAUTH2);
            responder.send();
        } else if (errorCode == SecurityErrorCode.AUTHORIZATION_FAILURE) {
            responder.setStatus(javax.ws.rs.core.Response.Status.FORBIDDEN.getStatusCode());
            responder.send();
        } else {
            responder.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            responder.send();
        }

    }


}
