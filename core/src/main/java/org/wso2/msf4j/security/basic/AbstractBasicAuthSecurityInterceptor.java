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

package org.wso2.msf4j.security.basic;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * AbstractBasicAuthSecurityInterceptor hides Netty based header processing and provide authenticate() method to plug-in
 * custom authentication logic.
 *
 * @since 1.1.0
 */
public abstract class AbstractBasicAuthSecurityInterceptor implements RequestInterceptor {

    private static final String AUTH_TYPE_BASIC = "Basic";
    public static final String CHARSET_UTF_8 = "UTF-8";
    private static final int AUTH_TYPE_BASIC_LENGTH = AUTH_TYPE_BASIC.length();

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        String authHeader = request.getHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            String authType = authHeader.substring(0, AUTH_TYPE_BASIC_LENGTH);
            String authEncoded = authHeader.substring(AUTH_TYPE_BASIC_LENGTH).trim();
            if (AUTH_TYPE_BASIC.equals(authType) && !authEncoded.isEmpty()) {
                byte[] decodedByte = authEncoded.getBytes(Charset.forName(CHARSET_UTF_8));
                String authDecoded = new String(Base64.getDecoder().decode(decodedByte),
                        Charset.forName(CHARSET_UTF_8));
                String[] authParts = authDecoded.split(":");
                String username = authParts[0];
                String password = authParts[1];
                if (authenticate(username, password)) {
                    return true;
                }
            }

        }
        response.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
        response.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE_BASIC);
        return false;
    }

    protected abstract boolean authenticate(String username, String password);
}
