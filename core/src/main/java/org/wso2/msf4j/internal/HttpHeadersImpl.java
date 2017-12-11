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
package org.wso2.msf4j.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This is JAX-RS HttpHeaders implementation for msf4j.
 * This also wraps netty httpHeaders.
 *
 * @since 2.5.0
 */
public class HttpHeadersImpl implements HttpHeaders {

    private static final String DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String GMT_TIMEZONE = "GMT";
    private final io.netty.handler.codec.http.HttpHeaders nettyHttpHeaders;
    private static final Logger log = LoggerFactory.getLogger(HttpHeadersImpl.class);

    public HttpHeadersImpl(io.netty.handler.codec.http.HttpHeaders httpHeaders) {
        nettyHttpHeaders = httpHeaders;
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return nettyHttpHeaders.getAll(name);
    }

    @Override
    public String getHeaderString(String name) {
        List<String> headerValues = nettyHttpHeaders.getAll(name);
        if (headerValues == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headerValues.size(); i++) {
            String value = headerValues.get(i);
            if (value == null || value.isEmpty()) {
                continue;
            }
            sb.append(value);
            if (i + 1 < headerValues.size()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        MultivaluedHashMap<String, String> newHeaders =
                new MultivaluedHashMap<>();
        for (Map.Entry<String, String> headerEntry : nettyHttpHeaders.entries()) {
            if (headerEntry != null) {
                newHeaders.add(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        return newHeaders;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        List<String> values = nettyHttpHeaders.getAll(HttpHeaders.ACCEPT);
        if (values == null || values.isEmpty() || values.get(0) == null) {
            return Collections.singletonList(MediaType.WILDCARD_TYPE);
        }
        List<MediaType> mediaTypes = new LinkedList<>();
        for (String value : values) {
            mediaTypes.add(MediaType.valueOf(value));
        }
        return mediaTypes;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        // Accept-Language: da
        // Accept-Language: en-gb;q=0.8
        List<String> values = nettyHttpHeaders.getAll(HttpHeaders.ACCEPT_LANGUAGE);
        if (values.isEmpty()) {
            return Collections.singletonList(new Locale("*"));
        }

        List<Locale> localeValues = new ArrayList<>();
        Map<Locale, Float> prefs = new HashMap<>();
        // derive preferences from Accept-Language and sort languages according to the preferences.
        for (String value : values) {
            String[] pair = value != null ? value.split(";") : new String[0];
            Locale locale = new Locale(pair[0].trim());
            localeValues.add(locale);
            if (pair.length > 1) {
                String[] pair2 = pair[1] != null ? pair[1].split("=") : new String[0];
                if (pair2.length > 1) {
                    prefs.put(locale, getLanguageQualityFactor(pair2[1].trim()));
                } else {
                    prefs.put(locale, 1F);
                }
            } else {
                prefs.put(locale, 1F);
            }
        }

        if (localeValues.size() <= 1) {
            return localeValues;
        }
        localeValues.sort(new AcceptLanguageComparator(prefs));
        return localeValues;
    }

    private float getLanguageQualityFactor(String q) {
        if (q == null) {
            return 1;
        }
        if (q.charAt(0) == '.') {
            q = '0' + q;
        }
        try {
            return Float.parseFloat(q);
        } catch (NumberFormatException ignored) {
            // ignored parsing exception and return default value.
        }
        return 1;
    }

    private static class AcceptLanguageComparator implements Comparator<Locale>, Serializable {
        private Map<Locale, Float> preferences;
        private static final long serialVersionUID = 6006269076155338045L;

        AcceptLanguageComparator(Map<Locale, Float> prefs) {
            this.preferences = prefs;
        }

        public int compare(Locale lang1, Locale lang2) {
            float p1 = preferences.get(lang1);
            float p2 = preferences.get(lang2);
            return Float.compare(p1, p2) * -1;
        }
    }

    @Override
    public MediaType getMediaType() {
        String value = nettyHttpHeaders.get(HttpHeaders.CONTENT_TYPE);
        return value == null ? null : MediaType.valueOf(value);
    }

    @Override
    public Locale getLanguage() {
        String value = nettyHttpHeaders.get(HttpHeaders.CONTENT_LANGUAGE);
        return value == null ? null : new Locale(value.trim());
    }

    @Override
    public Map<String, Cookie> getCookies() {
        List<String> values = nettyHttpHeaders.getAll(HttpHeaders.COOKIE);
        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Cookie> cookieMap = new HashMap<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            Cookie cookie = Cookie.valueOf(value);
            cookieMap.put(cookie.getName(), cookie);
        }
        return cookieMap;
    }

    @Override
    public Date getDate() {
        String value = nettyHttpHeaders.get(HttpHeaders.DATE);
        if (value == null || value.isEmpty()) {
            return null;
        }
        // Preferred date format in internet standard is Sun, 06 Nov 1994 08:49:37 GMT
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.ENGLISH);
        //All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT)
        dateFormat.setTimeZone(TimeZone.getTimeZone(GMT_TIMEZONE));
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            log.error("Error while parsing the Date value. Hence return null", e);
            return null;
        }
    }

    @Override
    public int getLength() {
        String value = nettyHttpHeaders.get(HttpHeaders.CONTENT_LENGTH);
        if (value == null || value.isEmpty()) {
            return -1;
        }
        int length = Integer.parseInt(value);
        return length >= 0 ? length : -1;
    }
}
