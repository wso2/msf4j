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
package org.wso2.msf4j.service;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.HighPriorityClassRequestInterceptor;
import org.wso2.msf4j.interceptor.HighPriorityClassResponseInterceptor;
import org.wso2.msf4j.interceptor.HighPriorityMethodRequestInterceptor;
import org.wso2.msf4j.interceptor.HighPriorityMethodResponseInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityClassRequestInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityClassResponseInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityMethodRequestInterceptor;
import org.wso2.msf4j.interceptor.LowPriorityMethodResponseInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityClassRequestInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityClassResponseInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityMethodRequestInterceptor;
import org.wso2.msf4j.interceptor.MediumPriorityMethodResponseInterceptor;
import org.wso2.msf4j.interceptor.PriorityDataHolder;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Micro Service to test priority of interceptors.
 */
@Path("/test/priorityInterceptorTest")
@RequestInterceptor({
        HighPriorityClassRequestInterceptor.class,
        MediumPriorityClassRequestInterceptor.class,
        LowPriorityClassRequestInterceptor.class
})
@ResponseInterceptor({
        HighPriorityClassResponseInterceptor.class,
        MediumPriorityClassResponseInterceptor.class,
        LowPriorityClassResponseInterceptor.class
})
public class PriorityInterceptorTestMicroService implements Microservice {

    @GET
    @Path("/priorityTest")
    @RequestInterceptor({
            HighPriorityMethodRequestInterceptor.class,
            MediumPriorityMethodRequestInterceptor.class,
            LowPriorityMethodRequestInterceptor.class
    })
    @ResponseInterceptor({
            HighPriorityMethodResponseInterceptor.class,
            MediumPriorityMethodResponseInterceptor.class,
            LowPriorityMethodResponseInterceptor.class
    })
    public String priorityInterceptorTest() {
        PriorityDataHolder.setPriorityOrder(PriorityDataHolder.getPriorityOrder() + "[HTTP METHOD]");
        return "Priority interceptor test";
    }
}
