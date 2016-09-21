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
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.interceptor.TestInterceptor;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests handler interceptors.
 */
public class InterceptorTest extends InterceptorTestBase {
    private final TestInterceptor interceptor1 = new TestInterceptor();
    private final TestInterceptor interceptor2 = new TestInterceptor();

    private final TestMicroservice testMicroservice = new TestMicroservice();

    private static final int port = Constants.PORT + 2;

    private MicroservicesRunner microservicesRunner;

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner.
                deploy(testMicroservice).
                addInterceptor(interceptor1).
                addInterceptor(interceptor2)
                .start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());
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
}
