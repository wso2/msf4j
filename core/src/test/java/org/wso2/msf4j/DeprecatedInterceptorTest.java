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

package org.wso2.msf4j;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.interceptor.TestInterceptor;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests handler interceptor.
 */
public class DeprecatedInterceptorTest {
    private final TestInterceptor interceptor1 = new TestInterceptor();
    private final TestInterceptor interceptor2 = new TestInterceptor();
    private final TestMicroservice testMicroservice = new TestMicroservice();
    private static final int port = Constants.PORT + 7;
    private MicroservicesRunner microservicesRunner;
    private static URI baseURI;

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner
                .deploy(testMicroservice)
                .addInterceptor(interceptor1)
                .addInterceptor(interceptor2)
                .start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());
        microservicesRunner.deploy("/DynamicPath2", new TestMicroServiceWithDynamicPath());
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    @BeforeMethod
    public void reset() {
        interceptor1.reset();
        interceptor2.reset();
    }

    @Test
    public void testPreInterceptorReject() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "Reject");
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        assertEquals(1, interceptor1.getNumPreCalls());

        // The second pre-call should not have happened due to rejection by the first pre-call
        // None of the post calls should have happened.
        assertEquals(0, interceptor1.getNumPostCalls());
        assertEquals(0, interceptor2.getNumPreCalls());
        assertEquals(0, interceptor2.getNumPostCalls());
    }

    // TODO Fix  WMS-82
    @Test
    public void testPreException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PreException");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        assertEquals(1, interceptor1.getNumPreCalls());

        // The second pre-call should not have happened due to exception in the first pre-call
        // None of the post calls should have happened.
        assertEquals(0, interceptor1.getNumPostCalls());
        assertEquals(0, interceptor2.getNumPreCalls());
        assertEquals(0, interceptor2.getNumPostCalls());
    }

    @Test
    public void testPostException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PostException");
        assertEquals(Response.Status.OK.getStatusCode(), status);

        assertEquals(1, interceptor1.getNumPreCalls());
        assertEquals(1, interceptor1.getNumPostCalls());

        assertEquals(1, interceptor2.getNumPreCalls());
        assertEquals(1, interceptor2.getNumPostCalls());
    }

    @Test
    public void testUnknownPath() throws Exception {
        int status = doGet("/unknown/path/test/v1/resource");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        assertEquals(0, interceptor1.getNumPreCalls());
        assertEquals(0, interceptor1.getNumPostCalls());

        assertEquals(0, interceptor2.getNumPreCalls());
        assertEquals(0, interceptor2.getNumPostCalls());
    }

    private int doGet(String resource) throws Exception {
        return doGet(resource, Collections.unmodifiableMap(Collections.emptyMap()));
    }

    private int doGet(String resource, String key, String value, String... keyValues) throws Exception {
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put(key, value);

        for (int i = 0; i < keyValues.length; i += 2) {
            headerMap.put(keyValues[i], keyValues[i + 1]);
        }
        return doGet(resource, headerMap);
    }

    private int doGet(String resource, Map<String, String> headers) throws Exception {
        URL url = baseURI.resolve(resource).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            return urlConn.getResponseCode();
        } finally {
            urlConn.disconnect();
        }
    }
}
