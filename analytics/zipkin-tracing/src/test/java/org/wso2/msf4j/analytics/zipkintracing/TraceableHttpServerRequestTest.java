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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.Request;

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
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        request = new Request(carbonMessage);
        request.getHeaders().set("testK", "testV");
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
