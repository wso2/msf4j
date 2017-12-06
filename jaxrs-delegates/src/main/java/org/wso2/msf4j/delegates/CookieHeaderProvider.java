/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.delegates;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate;

public class CookieHeaderProvider implements RuntimeDelegate.HeaderDelegate<Cookie> {

    private static final String VERSION = "$Version";
    private static final String PATH = "$Path";
    private static final String DOMAIN = "$Domain";

    @Override
    public Cookie fromString(String cookieValue) {
        if (cookieValue == null) {
            throw new IllegalArgumentException("Cookie value can not be null");
        }

        int version = 0;
        String name = null;
        String value = null;
        String path = null;
        String domain = null;

        String[] parts = cookieValue.split(";");
        for (String part : parts) {
            String token = part.trim();
            if (token.startsWith(VERSION)) {
                version = Integer.parseInt(token.substring(VERSION.length() + 1));
            } else if (token.startsWith(PATH)) {
                path = token.substring(PATH.length() + 1);
            } else if (token.startsWith(DOMAIN)) {
                domain = token.substring(DOMAIN.length() + 1);
            } else {
                int i = token.indexOf('=');
                if (i != -1) {
                    name = token.substring(0, i);
                    value = i == token.length()  + 1 ? "" : token.substring(i + 1);
                }
            }
        }

        if (name == null) {
            throw new IllegalArgumentException("Cookie is malformed : " + cookieValue);
        }

        return new Cookie(name, value, path, domain, version);
    }

    @Override
    public String toString(Cookie cookie) {
        StringBuilder sb = new StringBuilder();

        if (cookie.getVersion() != 0) {
            sb.append(VERSION).append('=').append(cookie.getVersion()).append(';');
        }
        sb.append(cookie.getName()).append('=').append(cookie.getValue());
        if (cookie.getPath() != null) {
            sb.append(';').append(PATH).append('=').append(cookie.getPath());
        }
        if (cookie.getDomain() != null) {
            sb.append(';').append(DOMAIN).append('=').append(cookie.getDomain());
        }
        return sb.toString();
    }
}
