/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.msf4j.spring;

import org.springframework.context.ConfigurableApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.msf4j.InterceptorTestBase;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.interceptor.HighPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.HighPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityGlobalRequestInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityGlobalResponseInterceptor;
import org.wso2.msf4j.interceptor.PriorityDataHolder;
import org.wso2.msf4j.interceptor.TestInterceptorDeprecated;
import org.wso2.msf4j.interceptor.TestRequestInterceptor;
import org.wso2.msf4j.interceptor.TestResponseInterceptor;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.spring.service.second.TestMicroServiceWithDynamicPath;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.testng.Assert.assertEquals;

/**
 * Class for testing interceptors with spring.
 *
 * @since 2.4.2
 */
public class SpringInterceptorTest extends InterceptorTestBase {

    private static final int port = Constants.PORT;
    private List<ConfigurableApplicationContext> configurableApplicationContexts = new ArrayList<>();

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format(Locale.ENGLISH, "http://%s:%d", Constants.HOSTNAME, port));
        MSF4JSpringApplication msf4JSpringApplication = new MSF4JSpringApplication(SpringHttpServerTest.class);
        configurableApplicationContexts.add(msf4JSpringApplication.run(true, "--http.port=8090"));
        configurableApplicationContexts.add(MSF4JSpringApplication.run(SecondService.class, "--http.port=8091"));
        msf4JSpringApplication.addService(configurableApplicationContexts.get(1), TestMicroServiceWithDynamicPath.class,
                "/DynamicPath")
                .addGlobalRequestInterceptor(new HighPriorityGlobalRequestInterceptor(),
                        new MediumPriorityGlobalRequestInterceptor(), new LowPriorityGlobalRequestInterceptor())
                .addInterceptor(new TestInterceptorDeprecated())
                .addGlobalResponseInterceptor(new HighPriorityGlobalResponseInterceptor(),
                        new MediumPriorityGlobalResponseInterceptor(), new LowPriorityGlobalResponseInterceptor());
    }

    @AfterClass
    public void tearDown() throws Exception {
        configurableApplicationContexts.forEach(ConfigurableApplicationContext::close);
    }

    /**
     * Test the order and the execution of the interceptors.
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
    public void springInterceptorTest() throws Exception {
        String path = "/DynamicPath/hello/MyMicro-Service";
        String response = doGetAndGetResponseString(path, false,
                Collections.unmodifiableMap(Collections.emptyMap()));
        assertEquals(response, "Hello MyMicro-Service");
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
                                      "[SPRING HTTP METHOD]" +
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
        AssertJUnit.assertEquals(executionOrderString, PriorityDataHolder.getPriorityOrder());
    }

    @BeforeMethod
    public void reset() {
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
}
