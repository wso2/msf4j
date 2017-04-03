/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.msf4j;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.interceptor.HighPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.HighPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.PriorityDataHolder;
import org.wso2.msf4j.interceptor.TestBreakRequestInterceptor;
import org.wso2.msf4j.interceptor.TestBreakResponseInterceptor;
import org.wso2.msf4j.interceptor.TestExceptionBreakRequestInterceptor;
import org.wso2.msf4j.interceptor.TestInterceptorDeprecated;
import org.wso2.msf4j.interceptor.TestRequestInterceptor;
import org.wso2.msf4j.interceptor.TestResponseInterceptor;
import org.wso2.msf4j.service.InterceptorTestMicroService;
import org.wso2.msf4j.service.PriorityInterceptorTestMicroService;
import org.wso2.msf4j.service.sub.Player;
import org.wso2.msf4j.service.sub.Team;

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Class for testing interceptors.
 * <p>
 * Please note that the deprecated interceptors are also tested along the way.
 */
public class InterceptorTest extends InterceptorTestBase {

    private static final int port = Constants.PORT + 6;
    private final String microServiceBaseUrl = "/test/interceptorTest/";
    private MicroservicesRunner microservicesRunner;
    private final InterceptorTestMicroService interceptorTestMicroService = new InterceptorTestMicroService();
    private final PriorityInterceptorTestMicroService priorityInterceptorTestMicroService =
            new PriorityInterceptorTestMicroService();

    @BeforeClass
    public void setup() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("netty-transports-3.yml");
        if (resource == null) {
            Assert.fail("netty-transports-3.yml not found");
        }
        System.setProperty("transports.netty.conf", resource.getPath());
        microservicesRunner = new MicroservicesRunner(port);

        // Global request interceptors are registered in the priority order of new interceptors -> old interceptors
        // Global response interceptors are registered in the priority order of old interceptors -> new interceptors
        microservicesRunner
                .deploy(interceptorTestMicroService)
                .deploy(priorityInterceptorTestMicroService)
                .addGlobalRequestInterceptor(new HighPriorityGlobalRequestInterceptor(),
                        new MediumPriorityGlobalRequestInterceptor(), new LowPriorityGlobalRequestInterceptor())
                .addInterceptor(new TestInterceptorDeprecated())
                .addGlobalResponseInterceptor(new HighPriorityGlobalResponseInterceptor(),
                        new MediumPriorityGlobalResponseInterceptor(), new LowPriorityGlobalResponseInterceptor())
                .start();
        baseURI = URI.create("http://" + Constants.HOSTNAME + ":" + port);
    }

    @AfterClass
    public void tearDown() throws Exception {
        microservicesRunner.stop();
    }

    @BeforeMethod
    public void reset() {
        PriorityDataHolder.setPriorityOrder("");

        // Reset request interceptors
        TestRequestInterceptor.reset();

        // Reset response interceptors
        TestResponseInterceptor.reset();

        // Reset global request interceptors
        HighPriorityGlobalRequestInterceptor.reset();
        MediumPriorityGlobalRequestInterceptor.reset();
        LowPriorityGlobalRequestInterceptor.reset();

        // Reset global response interceptors
        HighPriorityGlobalResponseInterceptor.reset();
        MediumPriorityGlobalResponseInterceptor.reset();
        LowPriorityGlobalResponseInterceptor.reset();

        // Reset deprecated interceptor
        TestInterceptorDeprecated.reset();
    }

    /**
     * Test whether resource interceptors are called.
     *
     * @throws Exception on any exception
     */
    @Test
    public void interceptionTest() throws Exception {
        // No sub resource
        Team team = doGetAndGetResponseObject(microServiceBaseUrl + "subResourceLocatorTest/SL/",
                false, Team.class, Collections.unmodifiableMap(Collections.emptyMap()));

        // Assert response
        assertEquals("Cricket", team.getTeamType());
        assertEquals("SL", team.getCountryId());

        // Assert interceptor calls
        // Request interceptors
        assertEquals(1, HighPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPreCallInterceptorCallsCount()); // Global interceptors

        assertEquals(2, TestRequestInterceptor.getFilterCalls()); // Resource level interceptor

        // Response interceptors
        assertEquals(1, HighPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPostCallInterceptorCallsCount()); // Global interceptors

        assertEquals(2, TestResponseInterceptor.getFilterCalls()); // Resource level interceptor
    }

    /**
     * Test whether sub-resource interceptors are called.
     *
     * @throws Exception on any exception
     */
    @Test
    public void subResourceInterceptionTest() throws Exception {
        // 1 sub resource
        String rawData = "countryName=SriLanka&type=Batsman";
        Player player = doPostAndGetResponseObject(microServiceBaseUrl + "subResourceLocatorTest/SL/" +
                        "interceptorTest/99/", rawData, false, Player.class,
                Collections.unmodifiableMap(Collections.emptyMap()));

        // Assert response
        assertEquals("player_1", player.getName());
        assertEquals(99, player.getPlayerId());
        assertEquals("SriLanka", player.getCountryName());
        assertEquals(30, player.getAge());
        assertEquals("Batsman", player.getType());

        // Assert interceptor calls
        // Request interceptors
        assertEquals(1, HighPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPreCallInterceptorCallsCount()); // Global interceptors

        assertEquals(3, TestRequestInterceptor.getFilterCalls()); // Resource level interceptor

        // Response interceptors
        assertEquals(1, HighPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPostCallInterceptorCallsCount()); // Global interceptors

        assertEquals(3, TestResponseInterceptor.getFilterCalls()); // Resource level interceptor

        String executionOrderString = "HighPriorityGlobalRequestInterceptor" +
                "MediumPriorityGlobalRequestInterceptor" +
                "LowPriorityGlobalRequestInterceptor" +
                "TestInterceptorDeprecated - [PRE CALL]" +
                "TestRequestInterceptor" +
                "TestRequestInterceptor" +
                "[HTTP RESOURCE METHOD]" +
                "TestRequestInterceptor" +
                "[HTTP SUB RESOURCE METHOD]" +
                "TestResponseInterceptor" +
                "TestResponseInterceptor" +
                "TestResponseInterceptor" +
                "TestInterceptorDeprecated - [POST CALL]" +
                "HighPriorityGlobalResponseInterceptor" +
                "MediumPriorityGlobalResponseInterceptor" +
                "LowPriorityGlobalResponseInterceptor";
        assertEquals(executionOrderString, PriorityDataHolder.getPriorityOrder());
    }

    /**
     * Test the order of the filters.
     * Order of the execution should be
     * <p>
     * 1) Global request interceptors according to the priority order (way developer writes them)
     * HighPriorityGlobalRequestInterceptor -> MediumPriorityGlobalRequestInterceptor ->
     * LowPriorityGlobalRequestInterceptor -> TestInterceptorDeprecated - [PRE CALL]
     * <p>
     * 2) Resource level request interceptors according to the priority order (way developer writes them)
     * HighPriorityRRequestInterceptor -> MediumPriorityRRequestInterceptor -> LowPriorityRRequestInterceptor
     * <p>
     * 3) Sub-resource level request interceptors according to the priority order (way developer writes them)
     * HighPrioritySRRequestInterceptor -> MediumPrioritySRRequestInterceptor -> LowPrioritySRRequestInterceptor
     * <p>
     * 4) HTTP method
     * <p>
     * 5) Sub-resource level response interceptors according to the priority order (way developer writes them)
     * HighPrioritySRResponseInterceptor -> MediumPrioritySRResponseInterceptor -> LowPrioritySRResponseInterceptor
     * <p>
     * 6) Resource level response interceptors according to the priority order (way developer writes them)
     * HighPriorityRResponseInterceptor -> MediumPriorityRResponseInterceptor -> LowPriorityRResponseInterceptor
     * <p>
     * 7) Global response interceptors according to the priority order (way developer writes them)
     * TestInterceptorDeprecated - [POST CALL] -> HighPriorityGlobalResponseInterceptor ->
     * MediumPriorityGlobalResponseInterceptor -> LowPriorityGlobalResponseInterceptor
     *
     * @throws Exception on any exception
     */
    @Test
    public void priorityTest() throws Exception {
        String response = doGetAndGetResponseString("/test/priorityInterceptorTest/priorityTest", false,
                Collections.unmodifiableMap(Collections.emptyMap()));
        assertEquals("Priority interceptor test", response);
        String executionOrderString = "HighPriorityGlobalRequestInterceptor" +
                "MediumPriorityGlobalRequestInterceptor" +
                "LowPriorityGlobalRequestInterceptor" +
                "TestInterceptorDeprecated - [PRE CALL]" +
                "HighPriorityClassRequestInterceptor" +
                "MediumPriorityClassRequestInterceptor" +
                "LowPriorityClassRequestInterceptor" +
                "HighPriorityMethodRequestInterceptor" +
                "MediumPriorityMethodRequestInterceptor" +
                "LowPriorityMethodRequestInterceptor" +
                "[HTTP METHOD]" +
                "HighPriorityMethodResponseInterceptor" +
                "MediumPriorityMethodResponseInterceptor" +
                "LowPriorityMethodResponseInterceptor" +
                "HighPriorityClassResponseInterceptor" +
                "MediumPriorityClassResponseInterceptor" +
                "LowPriorityClassResponseInterceptor" +
                "TestInterceptorDeprecated - [POST CALL]" +
                "HighPriorityGlobalResponseInterceptor" +
                "MediumPriorityGlobalResponseInterceptor" +
                "LowPriorityGlobalResponseInterceptor";
        assertEquals(executionOrderString, PriorityDataHolder.getPriorityOrder());
    }

    /**
     * Test for interception break on exception.
     */
    @Test
    public void interceptorFlowBreakOnExceptionTest() {
        try {
            doGetAndGetResponseString(microServiceBaseUrl + "interceptorBreakOnExceptionTest", false,
                    Collections.unmodifiableMap(Collections.emptyMap()));
            Assert.fail(); // Fail test if exception is not thrown
        } catch (Exception e) {
            assertEquals(e.getClass().getName(), "java.io.IOException");
            assertEquals(e.getMessage(), "Server returned HTTP response code: 500 for URL: " +
                    "http://localhost:8096/test/interceptorTest/interceptorBreakOnExceptionTest");

            assertEquals(1, HighPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
            assertEquals(1, MediumPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
            assertEquals(1, LowPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
            assertEquals(1, TestInterceptorDeprecated.getPreCallInterceptorCallsCount()); // Global interceptors
            assertEquals(1, TestRequestInterceptor.getFilterCalls()); // Resource and sub-resource interceptors
            assertEquals(1, TestExceptionBreakRequestInterceptor.getFilterCalls()); // Faulty interceptor

            // Response interceptors
            assertEquals(0, TestResponseInterceptor.getFilterCalls()); // Resource and sub-resource interceptors
            assertEquals(0, TestInterceptorDeprecated.getPostCallInterceptorCallsCount()); // Global interceptors
            assertEquals(0, HighPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
            assertEquals(0, MediumPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
            assertEquals(0, LowPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        }
    }

    /**
     * Test for interception break manually by user.
     */
    @Test
    public void requestInterceptorFlowBreakByUserTest() throws Exception {
        String response =
                doGetAndGetResponseString(microServiceBaseUrl + "requestInterceptorBreakByUserTest",
                        false, Collections.unmodifiableMap(Collections.emptyMap()));
        assertEquals(response, "");
        // Request interceptors
        assertEquals(1, HighPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPreCallInterceptorCallsCount()); // Global interceptors
        assertEquals(1, TestRequestInterceptor.getFilterCalls()); // Resource and sub-resource interceptors
        assertEquals(1, TestBreakRequestInterceptor.getFilterCalls()); // Resource and sub-resource interceptors

        // Response interceptors
        assertEquals(0, TestResponseInterceptor.getFilterCalls()); // Resource and sub-resource interceptors
        assertEquals(0, TestInterceptorDeprecated.getPostCallInterceptorCallsCount()); // Global interceptors
        assertEquals(0, HighPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(0, MediumPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(0, LowPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors

    }

    /**
     * Test for interception break manually by user.
     *
     * @throws Exception on any exception
     */
    @Test
    public void responseInterceptorFlowBreakByUserTest() throws Exception {
        String response =
                doGetAndGetResponseString(microServiceBaseUrl + "responseInterceptorBreakByUserTest",
                        false, Collections.unmodifiableMap(Collections.emptyMap()));
        assertEquals(response, "");

        // Request interceptors
        assertEquals(1, HighPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, MediumPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, LowPriorityGlobalRequestInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(1, TestInterceptorDeprecated.getPreCallInterceptorCallsCount()); // Global interceptors
        assertEquals(2, TestRequestInterceptor.getFilterCalls()); // Resource and sub-resource interceptors

        // Response interceptors
        assertEquals(1, TestBreakResponseInterceptor.getFilterCalls()); // Break interception flow interceptor
        assertEquals(0, TestResponseInterceptor.getFilterCalls()); // Resource and sub-resource interceptors
        assertEquals(0, TestInterceptorDeprecated.getPostCallInterceptorCallsCount()); // Global interceptors
        assertEquals(0, HighPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(0, MediumPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
        assertEquals(0, LowPriorityGlobalResponseInterceptor.getFilterCalls()); // Global interceptors
    }
}
