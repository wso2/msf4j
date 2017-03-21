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
import org.wso2.msf4j.interceptor.PriorityDataHolder;
import org.wso2.msf4j.interceptor.TestBreakRequestInterceptor;
import org.wso2.msf4j.interceptor.TestBreakResponseInterceptor;
import org.wso2.msf4j.interceptor.TestExceptionBreakRequestInterceptor;
import org.wso2.msf4j.interceptor.TestRequestInterceptor;
import org.wso2.msf4j.interceptor.TestResponseInterceptor;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;
import org.wso2.msf4j.service.sub.Team;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Micro Service to test interceptors.
 */
@Path("/test/interceptorTest")
@RequestInterceptor(TestRequestInterceptor.class)
@ResponseInterceptor(TestResponseInterceptor.class)
public class InterceptorTestMicroService implements Microservice {

    @GET
    @Path("/interceptorBreakOnExceptionTest")
    @RequestInterceptor({TestExceptionBreakRequestInterceptor.class, TestRequestInterceptor.class})
    public String interceptorBreakOnExceptionTest() {
        return "Exception break interceptor test";
    }

    @GET
    @Path("/requestInterceptorBreakByUserTest")
    @RequestInterceptor({TestBreakRequestInterceptor.class, TestRequestInterceptor.class})
    public String requestInterceptorBreakByUserTest() {
        return "Manual break request interceptor test";
    }

    @GET
    @Path("/responseInterceptorBreakByUserTest")
    @RequestInterceptor(TestRequestInterceptor.class)
    @ResponseInterceptor({TestBreakResponseInterceptor.class, TestResponseInterceptor.class})
    public String responseInterceptorBreakByUserTest() {
        return "Manual break response interceptor test";
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/subResourceLocatorTest/{countryId}")
    @RequestInterceptor(TestRequestInterceptor.class)
    @ResponseInterceptor(TestResponseInterceptor.class)
    public Team subResourceLocatorTest(@PathParam("countryId") String countryId) {
        PriorityDataHolder.setPriorityOrder(PriorityDataHolder.getPriorityOrder() + "[HTTP RESOURCE METHOD]");
        return new Team(countryId);
    }
}
