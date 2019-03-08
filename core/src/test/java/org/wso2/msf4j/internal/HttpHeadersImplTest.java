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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class HttpHeadersImplTest {
    private javax.ws.rs.core.HttpHeaders httpHeaders1;
    private javax.ws.rs.core.HttpHeaders httpHeaders2;

    @BeforeMethod
    public void setUp() throws Exception {
        HttpCarbonMessage httpCarbonMessage = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpMethod.GET, "msf4j"));
        httpCarbonMessage.getHeaders().add("testA", "test1");
        httpCarbonMessage.getHeaders().add("testA", "test2");
        httpCarbonMessage.setHeader("Accept", "application/json");
        httpCarbonMessage.setHeader("Content-Type", "text/html");
        httpCarbonMessage.setHeader("Content-Language", "en");
        httpCarbonMessage.setHeader("Content-Length", "1024");
        httpCarbonMessage.setHeader("Date", "Sun, 06 Nov 1994 08:49:37 GMT");
        httpCarbonMessage.getHeaders().add("Accept-Language", "da");
        httpCarbonMessage.getHeaders().add("Accept-Language", "en-gb;q=0.8");
        httpCarbonMessage.getHeaders().add("Accept-Language", "en;q=0.7");
        httpCarbonMessage.getHeaders().add("Cookie", "JSESSIONID=3508015E4EF0ECA8C4B761FCC4BC1718");
        httpHeaders1 = new HttpHeadersImpl(httpCarbonMessage.getHeaders());

        HttpCarbonMessage httpCarbonMessage2 = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpMethod.GET, "msf4j"));
        httpHeaders2 = new HttpHeadersImpl(httpCarbonMessage2.getHeaders());
    }

    @Test
    public void testGetRequestHeader() throws Exception {
        List<String> headerValues = httpHeaders1.getRequestHeader("testA");
        assertEquals(headerValues.size(), 2);
        assertTrue(headerValues.contains("test1"));
        assertTrue(headerValues.contains("test2"));
    }

    @Test
    public void testGetHeaderString() throws Exception {
        String headerValue = httpHeaders1.getHeaderString("testA");
        assertEquals(headerValue, "test1,test2");

        headerValue = httpHeaders1.getHeaderString("Accept");
        assertEquals(headerValue, "application/json");
    }

    @Test
    public void testGetRequestHeaders() throws Exception {
        MultivaluedMap<String, String> headersMap = httpHeaders1.getRequestHeaders();
        List<String> headerValues = headersMap.get("testA");
        assertEquals(headerValues.size(), 2);
        assertTrue(headerValues.contains("test1"));
        assertTrue(headerValues.contains("test2"));

        headerValues = headersMap.get("Accept");
        assertEquals(headerValues.size(), 1);
        assertTrue(headerValues.contains("application/json"));
    }

    @Test
    public void testGetAcceptableMediaTypes() throws Exception {
        List<MediaType> mediaTypes = httpHeaders1.getAcceptableMediaTypes();
        assertEquals(mediaTypes.size(), 1);
        assertEquals(mediaTypes.get(0).getType(), "application");
        assertEquals(mediaTypes.get(0).getSubtype(), "json");
        assertEquals(mediaTypes.get(0).toString(), "application/json");

        mediaTypes = httpHeaders2.getAcceptableMediaTypes();
        assertEquals(mediaTypes.size(), 1);
        assertEquals(mediaTypes.get(0).getType(), "*");
        assertEquals(mediaTypes.get(0).getSubtype(), "*");
        assertEquals(mediaTypes.get(0).toString(), "*/*");
    }

    @Test
    public void testGetAcceptableLanguages() throws Exception {
        List<Locale> locales = httpHeaders1.getAcceptableLanguages();
        assertEquals(locales.size(), 3);
        assertEquals(locales.get(0).getLanguage(), "da");
        assertEquals(locales.get(1).getLanguage(), "en-gb");
        assertEquals(locales.get(2).getLanguage(), "en");

        locales = httpHeaders2.getAcceptableLanguages();
        assertEquals(locales.size(), 1);
        assertEquals(locales.get(0).getLanguage(), "*");
    }

    @Test
    public void testGetMediaType() throws Exception {
        MediaType mediaType = httpHeaders1.getMediaType();
        assertNotNull(mediaType);
        assertEquals(mediaType.getType(), "text");
        assertEquals(mediaType.getSubtype(), "html");
        assertEquals(mediaType.toString(), "text/html");

        MediaType mediaType2 = httpHeaders2.getMediaType();
        assertNull(mediaType2);
    }

    @Test
    public void testGetLanguage() throws Exception {
        Locale locale = httpHeaders1.getLanguage();
        assertNotNull(locale);
        assertEquals(locale.getLanguage(), "en");

        Locale locale2 = httpHeaders2.getLanguage();
        assertNull(locale2);
    }

    @Test
    public void testGetCookies() throws Exception {
        Map<String, Cookie> cookieMap = httpHeaders1.getCookies();
        assertNotNull(cookieMap);
        assertEquals(cookieMap.size(), 1);
        assertTrue(cookieMap.containsKey("JSESSIONID"));
        assertEquals(cookieMap.get("JSESSIONID").getValue(), "3508015E4EF0ECA8C4B761FCC4BC1718");

        Map<String, Cookie> cookieMap2 = httpHeaders2.getCookies();
        assertEquals(cookieMap2.size(), 0);
    }

    @Test
    public void testGetDate() throws Exception {
        Date date = httpHeaders1.getDate();
        assertNotNull(date);
        assertEquals(date.getTime(), 784111777000L);

        Date date2 = httpHeaders2.getDate();
        assertNull(date2);
    }

    @Test
    public void testGetLength() throws Exception {
        int length = httpHeaders1.getLength();
        assertEquals(length, 1024);

        int length2 = httpHeaders2.getLength();
        assertEquals(length2, -1);
    }
}
