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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * Provides conversions from Cookie to String and String to Cookie.
 */
public class CookieHeaderProvider implements RuntimeDelegate.HeaderDelegate<Cookie> {

    private static final String DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String GMT_TIMEZONE = "GMT";
    private static final String VERSION = "Version";
    private static final String PATH = "Path";
    private static final String DOMAIN = "Domain";
    private static final String SECURE = "Secure";
    private static final String EXPIRES = "Expires";
    private static final String COMMENT = "Comment";
    private static final String HTTP_ONLY = "HttpOnly";
    private static final String MAX_AGE = "MaxAge";
    private static final Logger log = LoggerFactory.getLogger(CookieHeaderProvider.class);
    // Preferred date format in internet standard is Sun, 06 Nov 1994 08:49:37 GMT
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);

    @Override
    public Cookie fromString(String cookieValue) {
        if (cookieValue == null) {
            throw new IllegalArgumentException("Cookie value can not be null");
        }

        int version = NewCookie.DEFAULT_VERSION;
        int maxAge = NewCookie.DEFAULT_MAX_AGE;
        String name = null;
        String value = null;
        String path = null;
        String domain = null;
        String comment = null;
        Date expiry = null;
        boolean secure = false;
        boolean httpOnly = false;

        String[] parts = cookieValue.split(";");
        for (String part : parts) {
            String token = part.trim();
            if (token.startsWith(VERSION)) {
                version = Integer.parseInt(token.substring(VERSION.length() + 1));
            } else if (token.startsWith(PATH)) {
                path = token.substring(PATH.length() + 1);
            } else if (token.startsWith(DOMAIN)) {
                domain = token.substring(DOMAIN.length() + 1);
            } else if (token.startsWith(SECURE)) {
                secure = Boolean.TRUE;
            } else if (token.startsWith(HTTP_ONLY)) {
                httpOnly = Boolean.TRUE;
            } else if (token.startsWith(COMMENT)) {
                comment = token.substring(COMMENT.length() + 1);
            } else if (token.startsWith(MAX_AGE)) {
                maxAge = Integer.parseInt(token.substring(MAX_AGE.length() + 1));
            } else if (token.startsWith(EXPIRES)) {
                try {
                    //All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone(GMT_TIMEZONE));
                    expiry = dateFormat.parse(token.substring(EXPIRES.length() + 1));
                } catch (ParseException e) {
                    log.error("Error while parsing the Date value. Hence return null", e);
                }
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

        return new NewCookie(name, value, path, domain, version, comment, maxAge, expiry, secure, httpOnly);
    }

    @Override
    public String toString(Cookie cookie) {
        StringBuilder sb = new StringBuilder();

        if (cookie.getVersion() != Cookie.DEFAULT_VERSION) {
            sb.append(VERSION).append('=').append(cookie.getVersion()).append(';');
        }
        sb.append(cookie.getName()).append('=').append(cookie.getValue());
        if (cookie.getPath() != null) {
            sb.append(';').append(PATH).append('=').append(cookie.getPath());
        }
        if (cookie.getDomain() != null) {
            sb.append(';').append(DOMAIN).append('=').append(cookie.getDomain());
        }
        if (cookie instanceof NewCookie) {
            NewCookie newCookie = (NewCookie) cookie;
            if (newCookie.getMaxAge() != NewCookie.DEFAULT_MAX_AGE) {
                sb.append(';').append(MAX_AGE).append('=').append(newCookie.getMaxAge());
            }
            if (newCookie.getComment() != null) {
                sb.append(';').append(COMMENT).append('=').append(newCookie.getComment());
            }
            if (newCookie.getExpiry() != null) {
                //All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT)
                dateFormat.setTimeZone(TimeZone.getTimeZone(GMT_TIMEZONE));
                sb.append(';').append(EXPIRES).append('=').append(dateFormat.format(newCookie.getExpiry()));
            }
            if (newCookie.isSecure()) {
                sb.append(';').append(SECURE);
            }
            if (newCookie.isHttpOnly()) {
                sb.append(';').append(HTTP_ONLY);
            }
        }
        return sb.toString();
    }
}
