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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.filter.HighPriorityRequestFilter;
import org.wso2.msf4j.filter.HighPriorityResponseFilter;
import org.wso2.msf4j.filter.LowPriorityRequestFilter;
import org.wso2.msf4j.filter.LowPriorityResponseFilter;
import org.wso2.msf4j.filter.MediumPriorityRequestFilter;
import org.wso2.msf4j.filter.MediumPriorityResponseFilter;
import org.wso2.msf4j.filter.PriorityDataHolder;
import org.wso2.msf4j.filter.TestGlobalRequestFilter;
import org.wso2.msf4j.filter.TestGlobalResponseFilter;
import org.wso2.msf4j.filter.TestRequestFilter;
import org.wso2.msf4j.filter.TestResponseFilter;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.internal.router.MicroserviceMetadata;
import org.wso2.msf4j.pojo.ResponseDataHolder;
import org.wso2.msf4j.service.FilterTestMicroService;
import org.wso2.msf4j.service.PriorityFilterTestMicroService;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Class for testing filters.
 */
public class FilterTest extends FilterTestBase {

    // TODO: Use same tests for osgi mode
    private static final int port = Constants.PORT;
    private MicroservicesRunner microservicesRunner;
    private final FilterTestMicroService filterTestMicroService = new FilterTestMicroService();
    private final PriorityFilterTestMicroService priorityFilterTestMicroService = new PriorityFilterTestMicroService();
    private final String executionOrderString = "org.wso2.msf4j.filter.HighPriorityRequestFilter" +
            "org.wso2.msf4j.filter.MediumPriorityRequestFilterorg.wso2.msf4j.filter.LowPriorityRequestFilter" +
            "org.wso2.msf4j.filter.HighPriorityResponseFilterorg.wso2.msf4j.filter.MediumPriorityResponseFilter" +
            "org.wso2.msf4j.filter.LowPriorityResponseFilter";

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner
                .deploy(filterTestMicroService)
                .deploy(priorityFilterTestMicroService)
                .registerRequestFilter(TestRequestFilter.class)
                .registerResponseFilter(TestResponseFilter.class)
                .registerRequestFilter(TestGlobalRequestFilter.class)
                .registerResponseFilter(TestGlobalResponseFilter.class)
                .registerResponseFilter(HighPriorityResponseFilter.class)
                .registerRequestFilter(HighPriorityRequestFilter.class)
                .registerResponseFilter(MediumPriorityResponseFilter.class)
                .registerRequestFilter(MediumPriorityRequestFilter.class)
                .registerRequestFilter(LowPriorityRequestFilter.class)
                .registerResponseFilter(LowPriorityResponseFilter.class)
                .start();
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
    }

    @AfterClass
    public void tearDown() throws Exception {
        microservicesRunner.stop();
    }

    @BeforeMethod
    public void reset() {
        TestRequestFilter.reset();
        TestResponseFilter.reset();
        TestGlobalRequestFilter.reset();
        TestGlobalResponseFilter.reset();
    }

    /**
     * Check for correct list of used annotations in multiple micro services.
     *
     * @throws Exception on any exception
     */
    @Test
    public void testUsedAnnotationsOfServices() throws Exception {
        MicroservicesRegistryImpl microServicesRegistry = microservicesRunner.getMsRegistry();
        MicroserviceMetadata microServiceMetadata = microServicesRegistry.getMetadata();

        // Get the services list
        Field f = microServicesRegistry.getClass().getDeclaredField("services"); // NoSuchFieldException
        f.setAccessible(true);
        Map<String, Object> services = (Map<String, Object>) f.get(microServicesRegistry); // IllegalAccessException

        Set<Class<?>> usedRequestFilterAnnotations = microServiceMetadata
                .scanRequestFilterAnnotations(Collections.unmodifiableCollection(services.values()));
        Set<Class<?>> usedResponseFilterAnnotations = microServiceMetadata
                .scanResponseFilterAnnotations(Collections.unmodifiableCollection(services.values()));

        // Check class count
        assertEquals(4, usedRequestFilterAnnotations.size());
        assertEquals(4, usedResponseFilterAnnotations.size());

        // Check for used annotations
        assertEquals(true, usedRequestFilterAnnotations.contains(TestRequestFilter.class));
        assertEquals(true, usedRequestFilterAnnotations.contains(HighPriorityRequestFilter.class));
        assertEquals(true, usedRequestFilterAnnotations.contains(LowPriorityRequestFilter.class));
        assertEquals(true, usedRequestFilterAnnotations.contains(MediumPriorityRequestFilter.class));
        assertEquals(true, usedResponseFilterAnnotations.contains(TestResponseFilter.class));
        assertEquals(true, usedResponseFilterAnnotations.contains(HighPriorityResponseFilter.class));
        assertEquals(true, usedResponseFilterAnnotations.contains(MediumPriorityResponseFilter.class));
        assertEquals(true, usedResponseFilterAnnotations.contains(LowPriorityResponseFilter.class));
    }

    /**
     * Test to check if global filters are taken correctly.
     *
     * @throws Exception on any exception
     */
    @Test
    public void globalFilterTest() throws Exception {
        MicroservicesRegistryImpl microServicesRegistry = microservicesRunner.getMsRegistry();

        Set<Class<?>> globalRequestClassSet = microServicesRegistry.getGlobalRequestFilterClassSet();
        Set<Class<?>> globalResponseClassSet = microServicesRegistry.getGlobalResponseFilterClassSet();

        assertEquals(1, globalRequestClassSet.size());
        assertEquals(1, globalResponseClassSet.size());

        assertEquals(true, globalRequestClassSet.contains(TestGlobalRequestFilter.class));
        assertEquals(true, globalResponseClassSet.contains(TestGlobalResponseFilter.class));
    }

    /**
     * Test whether resource and sub-resource filters are called.
     *
     * @throws Exception on any exception
     */
    @Test
    public void requestFilterTest() throws Exception {
        ResponseDataHolder responseDataHolder = doGet(microServiceBaseUrl + "requestFilterTest");

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);

        assertEquals(Response.Status.OK.getStatusCode(), responseDataHolder.getStatusCode());
        assertEquals(2, TestRequestFilter.getFilterCalls()); // Resource and sub-resource filter calls
        assertEquals(1, TestGlobalRequestFilter.getFilterCalls()); // Global filter calls
        assertEquals("Request filter test", responseDataHolder.getResponseBody());
    }

    /**
     * Test whether resource and sub-resource filters are called.
     *
     * @throws Exception on any exception
     */
    @Test
    public void responseFilterTest() throws Exception {
        ResponseDataHolder responseDataHolder = doGet(microServiceBaseUrl + "responseFilterTest");

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);

        assertEquals(Response.Status.OK.getStatusCode(), responseDataHolder.getStatusCode());
        assertEquals(2, TestResponseFilter.getFilterCalls()); // Resource and sub-resource filter calls
        assertEquals(1, TestGlobalResponseFilter.getFilterCalls()); // Global filter calls
        assertEquals("Response filter test", responseDataHolder.getResponseBody());
    }

    /**
     * Test the order of the filters.
     * Order of the filter executions should be
     * HighPriorityRequestFilter, MediumPriorityRequestFilter, LowPriorityRequestFilter
     * HighPriorityResponseFilter, MediumPriorityResponseFilter, LowPriorityResponseFilter
     * <p>
     * Priorities for request filters will be in ascending order (lower the highest)
     * while priorities for responses will be in descending order (higher the highest)
     *
     * @throws Exception on any exception
     */
    @Test
    public void priorityTest() throws Exception {
        ResponseDataHolder responseDataHolder = doGet(priorityMicroServiceBaseUrl + "priorityTest");

        // Wait for any post handlers to be called
        TimeUnit.MILLISECONDS.sleep(100);

        assertEquals(Response.Status.OK.getStatusCode(), responseDataHolder.getStatusCode());
        assertEquals("Priority filter test", responseDataHolder.getResponseBody());
        assertEquals(executionOrderString, PriorityDataHolder.getPriorityOrder());
    }
}
