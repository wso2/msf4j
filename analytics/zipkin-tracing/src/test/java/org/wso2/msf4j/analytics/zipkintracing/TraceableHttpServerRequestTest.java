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

import com.github.kristofa.brave.http.HttpServerRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.Request;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.HttpMethod;

/**
 * Class for testing TraceableHttpServerRequest.
 */
public class TraceableHttpServerRequestTest extends Assert {

    private Request request;
    private HttpServerRequest httpServerRequest;

    @BeforeClass
    public void setUp() throws IOException {
        HttpCarbonMessage httpCarbonMessage = new HttpCarbonMessage(
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpMethod.GET, "msf4j"));
        httpCarbonMessage.setHeader("testK", "testV");
        request = new Request(httpCarbonMessage);
        request.setProperty("TO", "msf4j");
        request.setProperty("HTTP_METHOD", HttpMethod.GET);
        httpServerRequest = new TraceableHttpServerRequest(request);
    }

    @Test
    public void testGetHeader() {
        assertEquals(httpServerRequest.getHttpHeaderValue("testK"), "testV");
    }

    @Test
    public void testGetUrl() {
        assertEquals(httpServerRequest.getUri(), URI.create("msf4j"));
    }

    @Test
    public void testGetHttpMethod() {
        assertEquals(httpServerRequest.getHttpMethod(), HttpMethod.GET);
    }
}
