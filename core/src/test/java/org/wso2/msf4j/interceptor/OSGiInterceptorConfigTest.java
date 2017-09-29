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
package org.wso2.msf4j.interceptor;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class OSGiInterceptorConfigTest {

    private OSGiInterceptorConfig osGiInterceptorConfig;
    private RequestInterceptor requestInterceptor1;
    private RequestInterceptor requestInterceptor2;
    private ResponseInterceptor responseInterceptor1;
    private ResponseInterceptor responseInterceptor2;

    @BeforeClass
    public void setup() {
        osGiInterceptorConfig = new OSGiInterceptorConfig();
        requestInterceptor1 = (request, response) -> true;
        requestInterceptor2 = (request, response) -> false;
        responseInterceptor1 = (request, response) -> false;
        responseInterceptor2 = (request, response) -> true;

        osGiInterceptorConfig.addGlobalRequestInterceptors(requestInterceptor1, requestInterceptor2);
        osGiInterceptorConfig.addGlobalResponseInterceptors(responseInterceptor1, responseInterceptor2);
    }

    @Test
    public void testOSGiInterceptorConfig() {
        RequestInterceptor[] globalRequestInterceptorArray = osGiInterceptorConfig.getGlobalRequestInterceptorArray();
        assertEquals(2, globalRequestInterceptorArray.length);
        ResponseInterceptor[] globalResponseInterceptorArray =
                osGiInterceptorConfig.getGlobalResponseInterceptorArray();
        assertEquals(2, globalResponseInterceptorArray.length);

        assertEquals(requestInterceptor1, globalRequestInterceptorArray[0]);
        assertEquals(requestInterceptor2, globalRequestInterceptorArray[1]);
        assertEquals(responseInterceptor1, globalResponseInterceptorArray[0]);
        assertEquals(responseInterceptor2, globalResponseInterceptorArray[1]);
    }
}
