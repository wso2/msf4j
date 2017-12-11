/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpClientRequest;
import feign.Request;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.ws.rs.HttpMethod;

/**
 * Class for testing TraceableHttpClient.
 */
public class TraceableHttpClientRequestTest extends Assert {

    private Request request;
    private HttpClientRequest httpRequest;

    @BeforeClass
    public void setUp() throws MalformedURLException {
        request = Request.create(HttpMethod.GET, URI.create("msf4j").toString(), new HashMap<>(), null,
                                 Charset.defaultCharset());
        httpRequest = new TraceableHttpClientRequest(request);
    }

    @Test
    public void testAddHeader() {
        httpRequest.addHeader("testK", "testV");
        assertTrue(request.headers().get("testK").contains("testV"));
    }

    @Test
    public void testGetUri() {
        assertEquals(httpRequest.getUri(), URI.create("msf4j"));
    }

    @Test
    public void testGetHttpMethod() {
        assertEquals(httpRequest.getHttpMethod(), HttpMethod.GET);
    }

}
