/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.exception.MappedException;
import org.wso2.msf4j.exception.MappedException2;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import org.wso2.msf4j.interceptor.TestInterceptor;
import org.wso2.msf4j.interceptor.TestInterceptorDeprecated;
import org.wso2.msf4j.service.SecondService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MicroservicesRegistryTest {

    private MicroservicesRunner microservicesRunner = new MicroservicesRunner();
    private SecondService service = new SecondService();
    private TestExceptionMapper exceptionMapper1 = new TestExceptionMapper();
    private TestExceptionMapper2 exceptionMapper2 = new TestExceptionMapper2();
    private TestInterceptor interceptor1 = new TestInterceptor();
    private TestInterceptorDeprecated interceptor2 = new TestInterceptorDeprecated();

    @BeforeClass
    public void setup() {
        microservicesRunner.getMsRegistry().addService(service);
        microservicesRunner.getMsRegistry().addInterceptor(interceptor1, interceptor2);
        microservicesRunner.getMsRegistry().addExceptionMapper(exceptionMapper1, exceptionMapper2);
    }

    @Test
    public void testMicroservicesRegistry() {
        MicroservicesRegistryImpl msRegistry = microservicesRunner.getMsRegistry();
        Optional<Map.Entry<String, Object>> serviceWithBasePath = msRegistry.getServiceWithBasePath("/SecondService");
        assertTrue(serviceWithBasePath.isPresent());
        assertEquals(msRegistry.getHttpServices().size(), 1);
        assertEquals(msRegistry.getServiceCount(), 1);
        assertTrue(msRegistry.getHttpServices().contains(service));
        msRegistry.removeService(service);
        serviceWithBasePath = msRegistry.getServiceWithBasePath("/SecondService");
        assertFalse(serviceWithBasePath.isPresent());

        List<RequestInterceptor> globalRequestInterceptorList = msRegistry.getGlobalRequestInterceptorList();
        assertEquals(globalRequestInterceptorList.size(), 2);
        assertEquals(globalRequestInterceptorList.get(0), interceptor1);
        List<ResponseInterceptor> globalResponseInterceptorList = msRegistry.getGlobalResponseInterceptorList();
        assertEquals(globalResponseInterceptorList.size(), 2);
        assertEquals(globalResponseInterceptorList.get(0), interceptor1);
        msRegistry.removeInterceptor(interceptor1);
        assertEquals(msRegistry.getGlobalRequestInterceptorList().size(), 1);
        assertEquals(msRegistry.getGlobalResponseInterceptorList().size(), 1);
        msRegistry.removeGlobalRequestInterceptor(interceptor2);
        assertEquals(msRegistry.getGlobalRequestInterceptorList().size(), 0);
        assertEquals(msRegistry.getGlobalResponseInterceptorList().size(), 1);
        msRegistry.removeGlobalResponseInterceptor(interceptor2);
        assertEquals(msRegistry.getGlobalRequestInterceptorList().size(), 0);
        assertEquals(msRegistry.getGlobalResponseInterceptorList().size(), 0);

        assertTrue(msRegistry.getExceptionMapper(new MappedException()).isPresent());
        assertTrue(msRegistry.getExceptionMapper(new MappedException2()).isPresent());
        msRegistry.removeExceptionMapper(exceptionMapper1);
        //TODO this assertion get fail when enable coverage. Need to check why
        assertFalse(msRegistry.getExceptionMapper(new MappedException()).isPresent());
        assertTrue(msRegistry.getExceptionMapper(new MappedException2()).isPresent());
    }
}
