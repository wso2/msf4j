/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss.internal.router;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.MicroservicesRunner;
import org.wso2.carbon.mss.internal.MicroservicesRegistry;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Tests handler interceptor.
 */
public class HandlerInterceptorTest2 extends BaseHandlerInterceptorTest {
    private static final TestInterceptor interceptor1 = new TestInterceptor();
    private static final TestInterceptor interceptor2 = new TestInterceptor();

    private static final Logger log = LoggerFactory.getLogger(HandlerInterceptorTest2.class);
    private static final MicroservicesRunner microservicesRunner = new MicroservicesRunner();
    private static final TestHandler testHandler = new TestHandler();

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT;

    @BeforeClass
    public static void setup() throws Exception {
        microservicesRunner
                .deploy(testHandler)
                .addInterceptor(interceptor1)
                .addInterceptor(interceptor2)
                .start();
        baseURI = URI.create(String.format("http://%s:%d", hostname, port));
        log.info("Waiting for server start..");
        TimeUnit.SECONDS.sleep(Constants.SERVER_START_WAIT);
    }

    @AfterClass
    public static void teardown() throws Exception {
        microservicesRunner.stop();
        // MicroservicesRegistry is singleton
        MicroservicesRegistry.getInstance().removeInterceptor(interceptor1);
        MicroservicesRegistry.getInstance().removeInterceptor(interceptor2);
        MicroservicesRegistry.getInstance().removeHttpService(testHandler);
        log.info("Waiting for server shutdown..");
        TimeUnit.SECONDS.sleep(Constants.SERVER_STOP_WAIT);
    }

    @Before
    public void reset() {
        interceptor1.reset();
        interceptor2.reset();
    }

    @Test
    public void testPreException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PreException");
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertEquals(1, interceptor1.getNumPreCalls());

        // The second pre-call should not have happened due to exception in the first pre-call
        // None of the post calls should have happened.
        Assert.assertEquals(0, interceptor1.getNumPostCalls());
        Assert.assertEquals(0, interceptor2.getNumPreCalls());
        Assert.assertEquals(0, interceptor2.getNumPostCalls());
    }

    @Test
    public void testPostException() throws Exception {
        int status = doGet("/test/v1/resource", "X-Request-Type", "PostException");
        Assert.assertEquals(HttpResponseStatus.OK.code(), status);

        Assert.assertEquals(1, interceptor1.getNumPreCalls());
        Assert.assertEquals(1, interceptor1.getNumPostCalls());

        Assert.assertEquals(1, interceptor2.getNumPreCalls());
        Assert.assertEquals(1, interceptor2.getNumPostCalls());
    }

    @Test
    public void testUnknownPath() throws Exception {
        int status = doGet("/unknown/path/test/v1/resource");
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), status);

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertEquals(0, interceptor1.getNumPreCalls());
        Assert.assertEquals(0, interceptor1.getNumPostCalls());

        Assert.assertEquals(0, interceptor2.getNumPreCalls());
        Assert.assertEquals(0, interceptor2.getNumPostCalls());
    }
}
